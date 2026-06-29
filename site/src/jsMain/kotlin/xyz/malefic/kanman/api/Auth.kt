package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.getAuth
import xyz.malefic.kanman.api.util.post
import xyz.malefic.kanman.data.model.TokenResponseModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.data.model.UserResponseModel

suspend fun register(user: UserRequestModel) = post<_, TokenResponseModel>("register", user)

suspend fun login(user: UserRequestModel) = post<_, TokenResponseModel>("login", user)

@IgnorableReturnValue
suspend fun logout() = post("logout")

suspend fun refresh() = post<TokenResponseModel>("token/refresh")

suspend fun user() = getAuth<UserResponseModel>("me")

suspend fun user(username: String) = getAuth<UserResponseModel>("users/$username")
