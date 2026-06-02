package xyz.malefic.kanman.data.transaction

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.kanman.data.UserEntity
import xyz.malefic.kanman.data.UserRequestModel
import xyz.malefic.kanman.data.toResponseModel
import xyz.malefic.kanman.util.hashPassword

fun createUser(user: UserRequestModel) =
    transaction {
        UserEntity.new {
            this.username = user.username
            this.hashedPassword = hashPassword(user.password)
        }
    }.toResponseModel()
