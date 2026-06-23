package xyz.malefic.kanman.api

import xyz.malefic.kanman.api.util.AuthSession
import xyz.malefic.kanman.api.util.getAuth
import xyz.malefic.kanman.api.util.post
import xyz.malefic.kanman.data.model.RefreshRequestModel.Companion.refresh
import xyz.malefic.kanman.data.model.TokenResponseModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.data.model.UserResponseModel

suspend fun register(user: UserRequestModel) = post<_, TokenResponseModel>("register", user)

suspend fun login(user: UserRequestModel) = post<_, TokenResponseModel>("login", user)

suspend fun logout() = AuthSession.refreshToken?.let { post<_>("logout", it.refresh) }

suspend fun refresh() = AuthSession.refreshToken?.let { post<_, TokenResponseModel>("token/refresh", it.refresh) }

suspend fun user() = getAuth<UserResponseModel>("me")

suspend fun user(username: String) = getAuth<UserResponseModel>("users/$username")
