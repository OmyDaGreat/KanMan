package xyz.malefic.kanman.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import org.http4k.core.Request
import org.http4k.lens.RequestKey
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.db.AuthTokenEntity
import xyz.malefic.kanman.data.db.AuthTokens
import xyz.malefic.kanman.data.db.UserEntity
import xyz.malefic.kanman.data.db.Users
import xyz.malefic.kanman.data.model.TokenResponseModel
import xyz.malefic.kanman.data.model.TokenType.ACCESS
import xyz.malefic.kanman.data.model.TokenType.REFRESH
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.data.model.UserResponseModel
import xyz.malefic.kanman.util.nowMs
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

const val ACCESS_TOKEN_TTL_MILLIS = 15 * 60 * 1000L
const val REFRESH_TOKEN_TTL_MILLIS = 30 * 24 * 60 * 60 * 1000L

private val secureRandom = SecureRandom()
private val bcrypt = BCrypt.withDefaults()
private val verifier = BCrypt.verifyer()
private val base64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

val requestUser = RequestKey.required<Uuid>("authenticated-user-id")

fun hashPassword(password: String): String = bcrypt.hashToString(12, password.toCharArray())

fun verifyPassword(
    password: String,
    hashedPassword: String,
): Boolean = verifier.verify(password.toCharArray(), hashedPassword).verified

fun generateToken(): String {
    val bytes = ByteArray(32)
    secureRandom.nextBytes(bytes)
    return base64.encode(bytes)
}

fun hashToken(token: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(token.toByteArray())
        .joinToString("") { "%02x".format(it) }

fun currentUser(request: Request) =
    transaction {
        UserEntity.findById(requestUser(request))?.toResponseModel()
    }

fun getTokensFromLogin(user: UserRequestModel) =
    transaction {
        UserEntity
            .find { Users.username eq user.username }
            .firstOrNull()
            ?.takeIf { verifyPassword(user.password, it.hashedPassword) }
            ?.let { issueTokenPair(it) }
    }

fun revokeRefreshToken(token: String) =
    transaction {
        AuthTokenEntity.findSingleByAndUpdate(
            AuthTokens.tokenHash eq hashToken(token),
        ) { it.revokedAt = nowMs() }
    }

fun refreshTokens(refreshToken: String) =
    transaction {
        val token = revokeRefreshToken(refreshToken) ?: return@transaction null
        issueTokenPair(token.user)
    }

fun getUserFromAccessToken(accessToken: String) =
    transaction {
        AuthTokenEntity
            .find { AuthTokens.tokenHash eq hashToken(accessToken) }
            .firstOrNull()
            ?.takeIf { it.tokenType == ACCESS && it.revokedAt == null && it.expiresAt > nowMs() }
            ?.user
            ?.toResponseModel()
    }

context(_: JdbcTransaction)
private fun issueTokenPair(user: UserEntity): TokenResponseModel {
    val accessToken = generateToken()
    val refreshToken = generateToken()
    val accessExpiration = nowMs() + ACCESS_TOKEN_TTL_MILLIS
    val refreshExpiration = nowMs() + REFRESH_TOKEN_TTL_MILLIS

    AuthTokenEntity.new {
        this.user = user
        tokenType = ACCESS
        tokenHash = hashToken(accessToken)
        expiresAt = accessExpiration
        revokedAt = null
    }

    AuthTokenEntity.new {
        this.user = user
        tokenType = REFRESH
        tokenHash = hashToken(refreshToken)
        expiresAt = refreshExpiration
        revokedAt = null
    }

    return TokenResponseModel(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = ACCESS_TOKEN_TTL_MILLIS / 1000,
    )
}

context(_: JdbcTransaction)
val UserResponseModel.entity
    get() = UserEntity.findById(id) ?: throw IllegalArgumentException("User with ID $id not found")
