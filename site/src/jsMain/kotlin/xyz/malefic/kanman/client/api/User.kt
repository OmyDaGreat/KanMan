package xyz.malefic.kanman.client.api

import xyz.malefic.kanman.client.api.util.getAuth
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import xyz.malefic.kanman.shared.data.model.UserSummaryModel

suspend fun user() = getAuth<UserResponseModel>("me")

suspend fun user(username: String) = getAuth<UserSummaryModel>("users/$username")
