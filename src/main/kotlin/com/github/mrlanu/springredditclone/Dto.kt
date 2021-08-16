package com.github.mrlanu.springredditclone

import java.time.Instant




class RegisterRequest (
    var email: String,
    var username: String,
    var password: String)

class LoginRequest (
    var username: String,
    var password: String)

class AuthenticationResponse (
    var authenticationToken: String,
    var username: String)
