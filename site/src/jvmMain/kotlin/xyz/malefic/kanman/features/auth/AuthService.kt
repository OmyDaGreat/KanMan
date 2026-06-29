package xyz.malefic.kanman.features.auth

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import arrow.core.raise.either
import at.favre.lib.crypto.bcrypt.BCrypt
import co.touchlab.kermit.Logger
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.db.AuthTokenEntity
import xyz.malefic.kanman.data.db.AuthTokens
import xyz.malefic.kanman.data.db.BoardEntity
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.db.Users
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Auth.AccountLocked
import xyz.malefic.kanman.data.model.Issue.Auth.InvalidCredentials
import xyz.malefic.kanman.data.model.Issue.Auth.InvalidToken
import xyz.malefic.kanman.data.model.Issue.Auth.MissingToken
import xyz.malefic.kanman.data.model.Issue.User
import xyz.malefic.kanman.data.model.Issue.Validation.BadRequest
import xyz.malefic.kanman.data.model.TokenModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.data.model.UserResponseModel
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

fun Response.withRefreshCookie(token: String?) =
    cookie(
        Cookie(
            "refresh_token",
            token ?: "",
            token?.let { REFRESH_TOKEN_TTL_MILLIS / 1000 },
            path = "/api",
            httpOnly = true,
            secure = true,
            sameSite = SameSite.Strict,
        ),
    )

fun hashPassword(password: String): String = bcrypt.hashToString(12, password.toCharArray())

private fun verifyPassword(
    pw: String,
    hash: String,
) = verifier.verify(pw.toCharArray(), hash).verified

private fun hash(text: String) = MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

private fun generateSecret(bytes: Int = 32) = ByteArray(bytes).also { secureRandom.nextBytes(it) }.let { base64.encode(it) }

private fun UserEntity.createAccessToken() =
    JWT
        .create()
        .withSubject(id.value.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_TTL_MILLIS))
        .sign(jwtAlgorithm)

context(_: Raise<Issue>)
fun verifyAccessToken(token: String) =
    ensureNotNull(
        Either
            .catch { jwtVerifier.verify(token).subject }
            .getOrElse { raise(it.message?.let { message -> InvalidToken(message) } ?: InvalidToken()) }
            ?.let { Uuid.parseOrNull(it) },
    ) { InvalidToken() }

context(_: JdbcTransaction)
fun UserEntity.issueTokenPair(): TokenModel {
    val accessToken = createAccessToken()
    val secret = generateSecret()

    val entity =
        AuthTokenEntity.new {
            this.user = this@issueTokenPair
            this.secretHash = hash(secret)
            this.expiresAt = System.currentTimeMillis() + REFRESH_TOKEN_TTL_MILLIS
        }

    return TokenModel(
        accessToken = accessToken,
        refreshToken = "${entity.id.value}:$secret",
        expiresIn = ACCESS_TOKEN_TTL_MILLIS / 1000,
    )
}

context(_: Raise<Issue>)
fun refreshTokens(refreshToken: String) =
    transaction {
        val (idPart, secret) = ensureNotNull(refreshToken.split(":").takeIf { it.size == 2 }) { BadRequest("Invalid refresh token format") }
        val id = ensureNotNull(Uuid.parseOrNull(idPart)) { BadRequest("Invalid refresh token ID") }
        val token = ensureNotNull(AuthTokenEntity.findById(id)) { InvalidToken("Refresh token not found") }
        val now = System.currentTimeMillis()

        if (token.expiresAt < now || token.secretHash != hash(secret)) {
            token.revokedAt = now
            raise(InvalidToken("Refresh token expired or invalid"))
        }

        token.revokedAt?.let { revokedAt ->
            ensure(revokedAt + REFRESH_GRACE_PERIOD_MILLIS > now) { InvalidToken("Refresh token already revoked") }
        }

        token.revokedAt = now
        token.user.issueTokenPair()
    }

fun janitor() =
    transaction {
        val now = System.currentTimeMillis()
        val graceLimit = now - REFRESH_GRACE_PERIOD_MILLIS
        AuthTokenEntity
            .find {
                (AuthTokens.expiresAt less now) or
                    (AuthTokens.revokedAt.isNotNull() and (AuthTokens.revokedAt less graceLimit))
            }.forEach { it.delete() }
    }

context(_: Raise<Issue>)
fun revokeRefreshToken(refreshToken: String) =
    transaction {
        val id = Uuid.parseOrNull(refreshToken.substringBefore(":")) ?: return@transaction

        AuthTokenEntity.findById(id)?.apply {
            if (revokedAt == null) revokedAt = System.currentTimeMillis()
        }
    }

context(_: Raise<Issue>)
fun getTokensFromLogin(user: UserRequestModel) =
    transaction {
        val userEntity = ensureNotNull(UserEntity.find { Users.username eq user.username }.firstOrNull()) { InvalidCredentials() }
        val now = System.currentTimeMillis()

        ensure(userEntity.lockUntil < now) { AccountLocked(userEntity.lockUntil) }

        ensure(verifyPassword(user.password, userEntity.hashedPassword)) {
            userEntity.failedAttempts += 1
            if (userEntity.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                userEntity.lockUntil = now + LOCKOUT_DURATION_MILLIS
            }
            InvalidCredentials()
        }

        userEntity.failedAttempts = 0
        userEntity.lockUntil = 0
        userEntity.issueTokenPair()
    }

context(_: Raise<Issue>)
fun getUserFromAccessToken(accessToken: String) =
    transaction {
        ensureNotNull(
            UserEntity
                .find { Users.id eq verifyAccessToken(accessToken) }
                .with(UserEntity::boards, BoardEntity::owner)
                .firstOrNull(),
        ) { InvalidToken() }.toResponseModel()
    }

context(_: JdbcTransaction)
private fun UserEntity.revokeAllRefreshTokens() =
    AuthTokenEntity.find { AuthTokens.user eq id }.forEach { it.revokedAt = it.revokedAt ?: System.currentTimeMillis() }

context(_: JdbcTransaction)
val UserResponseModel.entity
    get() = UserEntity.findById(id) ?: throw IllegalArgumentException("User with ID $id not found")

context(_: Raise<Issue>)
fun UserRequestModel.create() =
    transaction {
        ensure(UserEntity.find { Users.username eq username }.empty()) { User.AlreadyExists() }
        UserEntity
            .new {
                this.username = this@create.username
                this.hashedPassword = hashPassword(this@create.password)
            }.issueTokenPair()
    }

context(_: Raise<Issue>)
fun authenticate(request: Request) =
    getUserFromAccessToken(
        request
            .header("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")
            ?.trim()
            ?: request.query("token")
            ?: raise(MissingToken()),
    )

fun authenticateOptional(request: Request): UserResponseModel? =
    request
        .header("Authorization")
        ?.takeIf { it.startsWith("Bearer ") }
        ?.removePrefix("Bearer ")
        ?.trim()
        ?.let { token -> either { getUserFromAccessToken(token) }.getOrElse { null } }

context(_: Raise<Issue>)
fun getUserSummary(username: String) =
    transaction { ensureNotNull(UserEntity.find { Users.username eq username }.firstOrNull()) { User.NotFound() }.toSummaryModel() }

context(_: Raise<Issue>)
fun getUserSummary(id: Uuid) = transaction { ensureNotNull(UserEntity.findById(id)) { User.NotFound() }.toSummaryModel() }
