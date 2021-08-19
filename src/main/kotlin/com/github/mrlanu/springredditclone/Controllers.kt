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
    fun getAllUsers(): ResponseEntity<List<UserResponseDTO>> {
        val users = userService.findAllUsers()
        return ResponseEntity(users, HttpStatus.OK)
    }

    @GetMapping("/users/{userId}")
    fun getUserById(@PathVariable userId: String): ResponseEntity<UserResponseDTO>{
        val user = userService.getUserById(userId)
        return ResponseEntity<UserResponseDTO>(user, HttpStatus.OK)
    }

    @GetMapping("/auth/refresh")
    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse) = userService.refreshToken(request, response)

}

@RestController
@RequestMapping("/api/subreddits")
class SubredditsController(val subredditService: SubredditService){

    @PostMapping
    fun createSubreddit(@RequestBody subreddit: SubredditDto): ResponseEntity<SubredditDto>{
        val result = subredditService.createSubreddit(subreddit)
        return ResponseEntity(result, HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllSubreddits(): ResponseEntity<List<SubredditDto>> {
      val result = subredditService.getAll()
      return ResponseEntity(result, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getSubredditById(@PathVariable id: Long): ResponseEntity<SubredditDto>{
        val result = subredditService.getById(id)
        return ResponseEntity(result, HttpStatus.OK)
    }
}
