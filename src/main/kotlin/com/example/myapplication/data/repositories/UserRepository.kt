package com.example.myapplication.data.repositories

import com.example.myapplication.data.models.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    fun getUser(id: String): User {
        // Simulate fetching user from a data source
        return User(id, "John Doe", "john.doe@example.com")
    }

    fun getAllUsers(): List<User> {
        return listOf(
            User("1", "Alice Smith", "alice@example.com"),
            User("2", "Bob Johnson", "bob@example.com")
        )
    }
}