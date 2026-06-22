package xyz.malefic.kanman.auth

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import at.favre.lib.crypto.bcrypt.BCrypt
import co.touchlab.kermit.Logger
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.http4k.core.Request
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
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Server.BadRequest
import xyz.malefic.kanman.data.model.Issue.Server.Conflict
import xyz.malefic.kanman.data.model.Issue.Server.Unauthorized
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

fun hashPassword(password: String): String = bcrypt.hashToString(12, password.toCharArray())

private fun verifyPassword(
    pw: String,
    hash: String,
) = verifier.verify(pw.toCharArray(), hash).verified

private fun hash(text: String) = MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

private fun generateSecret(bytes: Int = 32) = ByteArray(bytes).also { secureRandom.nextBytes(it) }.let { base64.encode(it) }

private fun createAccessToken(user: UserEntity) =
    JWT
        .create()
        .withSubject(user.id.value.toString())
        .withExpiresAt(Date(nowMs() + ACCESS_TOKEN_TTL_MILLIS))
        .sign(jwtAlgorithm)

context(r: Raise<Issue>)
fun verifyAccessToken(token: String) =
    r.ensureNotNull(Uuid.parseOrNull(jwtVerifier.verify(token).subject)) { Unauthorized("Invalid or expired token") }

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

context(r: Raise<Issue>)
fun refreshTokens(refreshToken: String) =
    transaction {
        val (idPart, secret) =
            r.ensureNotNull(
                refreshToken.split(":").takeIf { it.size == 2 },
            ) { BadRequest("Invalid refresh token format") }
        val id = r.ensureNotNull(Uuid.parseOrNull(idPart)) { BadRequest("Invalid refresh token ID") }
        val token = r.ensureNotNull(AuthTokenEntity.findById(id)) { Unauthorized("Refresh token not found") }
        val now = nowMs()

        if (token.expiresAt < now || token.secretHash != hash(secret)) {
            token.revokedAt = now
            r.raise(Unauthorized("Refresh token expired or invalid"))
        }

        token.revokedAt?.let { revokedAt ->
            r.ensure(revokedAt + REFRESH_GRACE_PERIOD_MILLIS > now) { Unauthorized("Refresh token already revoked") }
        }

        token.revokedAt = now
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

context(r: Raise<Issue>)
fun revokeRefreshToken(refreshToken: String) =
    transaction {
        val idPart = refreshToken.substringBefore(":")
        val id = r.ensureNotNull(Uuid.parseOrNull(idPart)) { BadRequest("Invalid refresh token ID") }

        AuthTokenEntity.findById(id)?.apply { revokedAt = nowMs() }
    }

context(r: Raise<Issue>)
fun getTokensFromLogin(user: UserRequestModel) =
    transaction {
        val userEntity =
            r.ensureNotNull(
                UserEntity.find { Users.username eq user.username }.firstOrNull(),
            ) { Unauthorized("Invalid username or password") }
        val now = nowMs()

        r.ensure(userEntity.lockUntil < now) { Unauthorized("Account locked. Try again later.") }

        r.ensure(verifyPassword(user.password, userEntity.hashedPassword)) {
            userEntity.failedAttempts += 1
            if (userEntity.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                userEntity.lockUntil = now + LOCKOUT_DURATION_MILLIS
            }
            Unauthorized("Invalid username or password")
        }

        userEntity.failedAttempts = 0
        userEntity.lockUntil = 0
        revokeAccessTokensForUser(userEntity)
        issueTokenPair(userEntity)
    }

context(r: Raise<Issue>)
fun getUserFromAccessToken(accessToken: String) =
    transaction {
        r
            .ensureNotNull(UserEntity.findById(verifyAccessToken(accessToken))) { Unauthorized("Invalid or expired access token") }
            .toResponseModel()
    }

context(_: JdbcTransaction)
private fun revokeAccessTokensForUser(user: UserEntity) =
    AuthTokenEntity.find { AuthTokens.user eq user.id }.forEach { it.revokedAt = it.revokedAt ?: nowMs() }

context(_: JdbcTransaction)
val UserResponseModel.entity
    get() = UserEntity.findById(id) ?: throw IllegalArgumentException("User with ID $id not found")

context(r: Raise<Issue>)
fun UserRequestModel.create() =
    transaction {
        if (UserEntity.find { Users.username eq username }.any()) {
            r.raise(Conflict("Username already taken"))
        }
        UserEntity.new {
            this.username = this@create.username
            this.hashedPassword = hashPassword(this@create.password)
        }
    }.toResponseModel()

context(r: Raise<Issue>)
fun authenticate(request: Request) =
    getUserFromAccessToken(
        r.ensureNotNull(
            request
                .header("Authorization")
                ?.takeIf { it.startsWith("Bearer ") }
                ?.removePrefix("Bearer ")
                ?.trim(),
        ) { Unauthorized("Missing bearer token") },
    )
