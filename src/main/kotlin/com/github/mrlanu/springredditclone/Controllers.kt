package com.github.mrlanu.springredditclone

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api")
class AuthController(val userService: UserService) {

    @PostMapping("/auth/signup")
    fun signup(@RequestBody regRequest : RegisterRequest): ResponseEntity<String> {
        userService.signup(regRequest)
        return ResponseEntity("User registration successful.", HttpStatus.OK)
    }

    @GetMapping("/auth/verify/{token}")
    fun verifyAccount(@PathVariable token: String): ResponseEntity<String>{
        userService.verifyAccount(token) ?:
            return ResponseEntity("This token does not exist", HttpStatus.NOT_EXTENDED)

        return ResponseEntity("Account verified successfully.", HttpStatus.OK)
    }

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.findAllUsers()
        return ResponseEntity(users, HttpStatus.OK)
    }

    @GetMapping("/users/{userId}")
    fun getUserById(@PathVariable userId: String): ResponseEntity<UserResponseDTO>{

        val user: User = userService.getUserById(userId)?:
        throw ResourceNotFoundException("User with id: $userId has not been founded")

        val responseDTO = UserResponseDTO(
            userId = user.publicId,
            username = user.username,
            email = user.email)

        return ResponseEntity<UserResponseDTO>(responseDTO, HttpStatus.OK)
    }

    @GetMapping("/auth/refresh")
    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse) = userService.refreshToken(request, response)

}
