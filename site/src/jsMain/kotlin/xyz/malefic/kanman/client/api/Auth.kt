package xyz.malefic.kanman.client.api

import xyz.malefic.kanman.client.api.util.post
import xyz.malefic.kanman.shared.data.model.TokenResponseModel
import xyz.malefic.kanman.shared.data.model.UserRequestModel

suspend fun String.strength() = post<_, Pair<Int, String?>>("auth/password/strength", this)

suspend fun register(user: UserRequestModel) = post<_, TokenResponseModel>("auth/register", user)

suspend fun login(user: UserRequestModel) = post<_, TokenResponseModel>("auth/login", user)

suspend fun logout() = post("auth/logout")

suspend fun refresh() = post<TokenResponseModel>("auth/token/refresh")
