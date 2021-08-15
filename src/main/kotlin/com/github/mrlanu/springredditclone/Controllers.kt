package com.github.mrlanu.springredditclone

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(val authService: AuthService) {

    @PostMapping("/signup")
    fun signup(@RequestBody regRequest : RegisterRequest): ResponseEntity<String> {
        authService.signup(regRequest)
        return ResponseEntity("User registration successful.", HttpStatus.OK)
    }

    @GetMapping("/verify/{token}")
    fun verifyAccount(@PathVariable token: String): ResponseEntity<String>{
        authService.verifyAccount(token) ?:
            return ResponseEntity("This token does not exist", HttpStatus.NOT_EXTENDED)

        return ResponseEntity("Account verified successfully.", HttpStatus.OK)
    }
}
