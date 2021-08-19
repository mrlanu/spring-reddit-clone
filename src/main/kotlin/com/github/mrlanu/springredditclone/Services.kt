package com.github.mrlanu.springredditclone

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeTypeUtils
import org.springframework.web.server.ResponseStatusException
import java.security.Principal
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class UserService (val passwordEncoder : PasswordEncoder,
                   val userRepository: UserRepository,
                   val roleRepository: RoleRepository,
                   val verificationTokenRepository: VerificationTokenRepository,
                   val mailService: MailService): UserDetailsService {

    fun findAllUsers() = userRepository.findAll().map { user -> user.toUserDto() }

    override fun loadUserByUsername(email: String): UserDetails? =
        userRepository.findByEmail(email)?.let { user ->
            org.springframework.security.core.userdetails.User(
                user.email,
                user.password,
                user.enabled,
                user.enabled,
                user.enabled,
                user.enabled,
                AuthorityUtils.createAuthorityList(
                    *user.roles.map { r -> "ROLE_${r.roleName.uppercase()}" }.toTypedArray()
                )
            )
        }

    fun getCurrentUser(): User{
        val principal = SecurityContextHolder.getContext().authentication.principal as String
        return userRepository.findByEmail(principal) ?: throw RuntimeException("Error")
    }

    @Transactional
    fun signup(regReq: RegisterRequest){
        val user = User(
            username = regReq.username,
            password = passwordEncoder.encode(regReq.password),
            email = regReq.email,
            created = LocalDateTime.now(),
            enabled = false)

        val roleUser : Role = roleRepository.findByRoleName("user") ?:
            roleRepository.save(Role("user"))

        user.roles.add(roleUser)
        userRepository.save(user)
        val token = generateVerificationToken(user)

        mailService.sendEmail(
            NotificationEmail("Please activate your account", user.email,
                "Thank you for signing up to Spring Reddit, " +
                        "please click on the below url to activate your account : " +
                        "http://localhost:8080/api/auth/accountVerification/" + token))
    }

    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse){
        val authorizationHeader: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            try {
                val unpackedToken = Utils.unpackToken(authorizationHeader)
                val user = loadUserByUsername(unpackedToken.username)
                val tokens = Utils.generateTokens(user as org.springframework.security.core.userdetails.User)
                response.contentType = MimeTypeUtils.APPLICATION_JSON_VALUE
                ObjectMapper().registerModule(KotlinModule()).writeValue(response.outputStream, tokens)
            }catch (exception: Exception){
                exception.message?.let { Utils.responseError(response, it) }
            }
        }else Utils.responseError(response, "Refresh token is missing")
    }

    fun generateVerificationToken(user : User): String{
        val token = UUID.randomUUID().toString()
        val verificationToken = VerificationToken(token, user, LocalDateTime.now().plusDays(2))
        verificationTokenRepository.save(verificationToken)
        return token
    }

    fun verifyAccount(token: String): String? {
        val t = verificationTokenRepository.findByToken(token) ?:
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "This token does not exist")
        activateAccount(t)
        return "isActivated"
    }

    @Transactional
    fun activateAccount(token: VerificationToken){
        val user = userRepository.findByIdOrNull(token.user.userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")
        user.enabled = true
        userRepository.save(user)
    }

    fun getUserById(userId: String): UserResponseDTO {
        val result: User = userRepository.getUserByPublicId(userId)?:
                throw ResourceNotFoundException("User with id: $userId has not been founded")
        return result.toUserDto()
    }
}

@Service
class SubredditService(val subredditRepository: SubredditRepository){
    fun createSubreddit(subredditDto: SubredditDto): SubredditDto {
        val newSubreddit = subredditDto.toSubreddit()
        val savedSubreddit = subredditRepository.save(newSubreddit)
        return savedSubreddit.toSubredditDto()
    }

    fun getAll(): List<SubredditDto> {
        val subredditsList = subredditRepository.findAll()
        return subredditsList.map { s -> s.toSubredditDto() }
    }

    fun getById(id: Long): SubredditDto {
        val result = subredditRepository.findByIdOrNull(id) ?:
            throw ResourceNotFoundException("Subreddit with id: $id has not been founded")
        return result.toSubredditDto()
    }
}

@Service
class PostService(val postRepository: PostRepository,
                  val subredditRepository: SubredditRepository,
                  val authService: UserService){

    fun createPost(postRequest: PostRequest): PostResponse{
        val user = authService.getCurrentUser()
        val subreddit = subredditRepository.findByName(postRequest.subredditName)?:
            throw ResourceNotFoundException("Subreddit not found")
        val result = postRepository.save(postRequest.toPost(user, subreddit))
        return result.toPostResponse()
    }

    fun getAllPosts(): List<PostResponse> = postRepository.findAll().map { post -> post.toPostResponse() }

    fun getAllPostsBySubreddit(subredditId: Long): List<PostResponse> {
        val subreddit = subredditRepository.findByIdOrNull(subredditId) ?:
            throw ResourceNotFoundException("Subreddit not found")
        return postRepository.getAllBySubreddit(subreddit)?.map { post -> post.toPostResponse() } ?: listOf()
    }
}
