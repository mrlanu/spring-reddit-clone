package com.github.mrlanu.springredditclone

class RegisterRequest (
    var email: String,
    var username: String,
    var password: String)

class LoginRequest (
    var email: String,
    var password: String)

class TokensResponse(
    val accessToken: String,
    val refreshToken: String
)

class ErrorResponse(
    val message: String
)

class UserResponseDTO(
    val userId: String,
    val username: String,
    val email: String
)

class SubredditDto(
    var id: Long? = null,
    val name: String,
    val description: String,
    val numberOfPosts: Int? = 0
)

