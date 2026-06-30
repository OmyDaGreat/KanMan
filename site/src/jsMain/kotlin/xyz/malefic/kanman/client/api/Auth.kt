package xyz.malefic.kanman.client.api

import xyz.malefic.kanman.client.api.util.post
import xyz.malefic.kanman.shared.data.model.TokenResponseModel
import xyz.malefic.kanman.shared.data.model.UserRequestModel

suspend fun register(user: UserRequestModel) = post<_, TokenResponseModel>("register", user)

suspend fun login(user: UserRequestModel) = post<_, TokenResponseModel>("login", user)

suspend fun logout() = post("logout")

suspend fun refresh() = post<TokenResponseModel>("token/refresh")
