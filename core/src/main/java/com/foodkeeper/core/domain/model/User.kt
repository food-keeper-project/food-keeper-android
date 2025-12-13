package com.foodkeeper.core.domain.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val password: String,
)
