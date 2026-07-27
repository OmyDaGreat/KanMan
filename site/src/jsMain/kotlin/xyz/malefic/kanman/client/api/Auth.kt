package xyz.malefic.kanman.client.api

import xyz.malefic.kanman.client.api.util.post
import xyz.malefic.kanman.shared.data.model.TokenResponseModel
import xyz.malefic.kanman.shared.data.model.UserCreateModel
import xyz.malefic.kanman.shared.data.model.UserLoginModel

suspend fun String.strength() = post<_, Pair<Int, String?>>("auth/password/strength", this)

suspend fun register(user: UserCreateModel) = post<_, TokenResponseModel>("auth/register", user)

suspend fun login(user: UserLoginModel) = post<_, TokenResponseModel>("auth/login", user)

suspend fun logout() = post("auth/logout")

suspend fun refresh() = post<TokenResponseModel>("auth/token/refresh")
