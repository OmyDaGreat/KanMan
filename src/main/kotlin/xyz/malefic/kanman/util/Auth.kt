package xyz.malefic.kanman.util

import at.favre.lib.crypto.bcrypt.BCrypt
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.with
import org.http4k.lens.RequestKey
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.AuthTokenEntity
import xyz.malefic.kanman.data.AuthTokens
import xyz.malefic.kanman.data.TokenResponseModel
import xyz.malefic.kanman.data.TokenType
import xyz.malefic.kanman.data.UserEntity
import xyz.malefic.kanman.data.UserRequestModel
import xyz.malefic.kanman.data.Users
import xyz.malefic.kanman.data.error
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.data.toResponseModel
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlin.uuid.Uuid

private const val ACCESS_TOKEN_TTL_MILLIS = 15 * 60 * 1000L
private const val REFRESH_TOKEN_TTL_MILLIS = 30 * 24 * 60 * 60 * 1000L

private val secureRandom = SecureRandom()
private val bcrypt = BCrypt.withDefaults()
private val verifier = BCrypt.verifyer()

private val authenticatedUserId = RequestKey.required<Uuid>("authenticated-user-id")

val auth: Filter =
    Filter { next ->
        { request ->
            val token =
                request
                    .header("Authorization")
                    ?.takeIf { it.startsWith("Bearer ") }
                    ?.removePrefix("Bearer ")
                    ?.trim()
            if (token.isNullOrBlank()) {
                Response(UNAUTHORIZED).with(errorLens of "Missing bearer token".error)
            } else {
                val userId = transaction { findValidToken(token, TokenType.ACCESS)?.user?.id?.value }
                if (userId == null) {
                    Response(UNAUTHORIZED).with(errorLens of "Invalid or expired token".error)
                } else {
                    next(request.with(authenticatedUserId of userId))
                }
            }
        }
    }

fun hashPassword(password: String): String = bcrypt.hashToString(12, password.toCharArray())

fun verifyPassword(
    password: String,
    hashedPassword: String,
): Boolean = verifier.verify(password.toCharArray(), hashedPassword).verified

fun getTokensFromLogin(user: UserRequestModel) =
    transaction {
        UserEntity
            .find { Users.username eq user.username }
            .firstOrNull()
            ?.takeIf { verifyPassword(user.password, it.hashedPassword) }
            ?.let { issueTokenPair(it) }
    }

fun refreshTokens(refreshToken: String) =
    transaction {
        val token = findValidToken(refreshToken, TokenType.REFRESH) ?: return@transaction null
        token.revokedAt = nowMs()
        issueTokenPair(token.user)
    }

@Suppress("UnusedReceiverParameter")
private fun JdbcTransaction.issueTokenPair(user: UserEntity): TokenResponseModel {
    val accessToken = generateToken()
    val refreshToken = generateToken()
    val accessExpiration = nowMs() + ACCESS_TOKEN_TTL_MILLIS
    val refreshExpiration = nowMs() + REFRESH_TOKEN_TTL_MILLIS

    AuthTokenEntity.new {
        this.user = user
        tokenType = TokenType.ACCESS
        tokenHash = hashToken(accessToken)
        expiresAt = accessExpiration
        revokedAt = null
    }

    AuthTokenEntity.new {
        this.user = user
        tokenType = TokenType.REFRESH
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

fun currentUser(request: Request) =
    transaction {
        UserEntity.findById(authenticatedUserId(request))?.toResponseModel()
    }

@Suppress("UnusedReceiverParameter")
private fun JdbcTransaction.findValidToken(
    token: String,
    kind: TokenType,
) = AuthTokenEntity
    .find { AuthTokens.tokenHash eq hashToken(token) }
    .firstOrNull()
    ?.takeIf { it.tokenType == kind && it.revokedAt == null && it.expiresAt > nowMs() }

private fun generateToken(): String {
    val bytes = ByteArray(32)
    secureRandom.nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

private fun hashToken(token: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
