package xyz.malefic.kanman.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import co.touchlab.kermit.Logger
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.http4k.core.Request
import org.http4k.lens.RequestKey
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.db.AuthTokenEntity
import xyz.malefic.kanman.data.db.AuthTokens
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.db.Users
import xyz.malefic.kanman.data.model.TokenResponseModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.util.nowMs
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

const val ACCESS_TOKEN_TTL_MILLIS = 15 * 60 * 1000L
const val REFRESH_TOKEN_TTL_MILLIS = 30 * 24 * 60 * 60 * 1000L
const val REFRESH_GRACE_PERIOD_MILLIS = 30 * 1000L
const val LOCKOUT_DURATION_MILLIS = 15 * 60 * 1000L
const val MAX_FAILED_ATTEMPTS = 5

private val log = Logger.withTag("Auth")
private val secureRandom = SecureRandom()
private val bcrypt = BCrypt.withDefaults()
private val verifier = BCrypt.verifyer()
private val base64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

private val jwtAlgorithm =
    Algorithm.HMAC256(
        (System.getProperty("SECRET") ?: System.getenv("SECRET")).let { s ->
            when {
                s == null -> ByteArray(32).also { secureRandom.nextBytes(it) }.also { log.w { "SECRET is not set, using random value" } }
                s.length == 64 -> s.hexToByteArray()
                else -> s.toByteArray(Charsets.UTF_8)
            }.let { secret ->
                if (secret.size < 32) {
                    log.w { "SECRET is too short, using random value" }
                    ByteArray(32).also { secureRandom.nextBytes(it) }
                }
                secret
            }
        },
    )
private val jwtVerifier = JWT.require(jwtAlgorithm).build()

val requestUser = RequestKey.required<Uuid>("authenticated-user-id")

fun hashPassword(password: String): String = bcrypt.hashToString(12, password.toCharArray())

private fun verifyPassword(
    pw: String,
    hash: String,
): Boolean = verifier.verify(pw.toCharArray(), hash).verified

private fun hash(text: String): String =
    MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

private fun generateSecret(bytes: Int = 32): String = ByteArray(bytes).also { secureRandom.nextBytes(it) }.let { base64.encode(it) }

private fun createAccessToken(user: UserEntity): String =
    JWT
        .create()
        .withSubject(user.id.value.toString())
        .withExpiresAt(Date(nowMs() + ACCESS_TOKEN_TTL_MILLIS))
        .sign(jwtAlgorithm)

fun verifyAccessToken(token: String): Uuid? =
    try {
        Uuid.parse(jwtVerifier.verify(token).subject)
    } catch (_: Exception) {
        null
    }

context(_: JdbcTransaction)
fun issueTokenPair(user: UserEntity): TokenResponseModel {
    val accessToken = createAccessToken(user)
    val secret = generateSecret()

    val entity =
        AuthTokenEntity.new {
            this.user = user
            this.secretHash = hash(secret)
            this.expiresAt = nowMs() + REFRESH_TOKEN_TTL_MILLIS
        }

    return TokenResponseModel(
        accessToken = accessToken,
        refreshToken = "${entity.id.value}:$secret",
        expiresIn = ACCESS_TOKEN_TTL_MILLIS / 1000,
    )
}

fun refreshTokens(refreshToken: String): TokenResponseModel? =
    transaction {
        val (idPart, secret) = refreshToken.split(":").takeIf { it.size == 2 } ?: return@transaction null
        val id =
            try {
                Uuid.parse(idPart)
            } catch (_: Exception) {
                return@transaction null
            }

        val token = AuthTokenEntity.findById(id) ?: return@transaction null

        if (token.expiresAt < nowMs() || token.secretHash != hash(secret)) {
            token.revokedAt = nowMs()
            return@transaction null
        }

        token.revokedAt?.let { revokedAt ->
            if (nowMs() - revokedAt > REFRESH_GRACE_PERIOD_MILLIS) {
                return@transaction null
            }
        }

        token.revokedAt = nowMs()
        issueTokenPair(token.user)
    }

fun janitor() =
    transaction {
        val now = nowMs()
        val graceLimit = now - REFRESH_GRACE_PERIOD_MILLIS
        AuthTokenEntity
            .find {
                (AuthTokens.expiresAt less now) or
                    (AuthTokens.revokedAt.isNotNull() and (AuthTokens.revokedAt less graceLimit))
            }.forEach { it.delete() }
    }

fun revokeRefreshToken(refreshToken: String) =
    transaction {
        val idPart = refreshToken.substringBefore(":")
        val id =
            try {
                Uuid.parse(idPart)
            } catch (_: Exception) {
                return@transaction
            }
        AuthTokenEntity.findById(id)?.apply { revokedAt = nowMs() }
    }

fun currentUser(request: Request) = transaction { UserEntity.findById(requestUser(request))?.toResponseModel() }

fun getTokensFromLogin(user: UserRequestModel) =
    transaction {
        val userEntity = UserEntity.find { Users.username eq user.username }.firstOrNull() ?: return@transaction null
        val now = nowMs()

        if (userEntity.lockUntil > now) {
            return@transaction null
        }

        if (verifyPassword(user.password, userEntity.hashedPassword)) {
            userEntity.failedAttempts = 0
            userEntity.lockUntil = 0
            revokeAccessTokensForUser(userEntity)
            issueTokenPair(userEntity)
        } else {
            userEntity.failedAttempts += 1
            if (userEntity.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                userEntity.lockUntil = now + LOCKOUT_DURATION_MILLIS
            }
            null
        }
    }

fun getUserFromAccessToken(accessToken: String) =
    transaction { verifyAccessToken(accessToken)?.let { UserEntity.findById(it)?.toResponseModel() } }

context(_: JdbcTransaction)
private fun revokeAccessTokensForUser(user: UserEntity) =
    AuthTokenEntity.find { AuthTokens.user eq user.id }.forEach { it.revokedAt = it.revokedAt ?: nowMs() }

context(_: JdbcTransaction)
val UserResponseModel.entity
    get() = UserEntity.findById(id) ?: throw IllegalArgumentException("User with ID $id not found")

fun createUser(user: UserRequestModel) =
    transaction {
        UserEntity.new {
            this.username = user.username
            this.hashedPassword = hashPassword(user.password)
        }
    }.toResponseModel()
