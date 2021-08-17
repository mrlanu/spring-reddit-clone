package com.github.mrlanu.springredditclone

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.Async
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.util.*

@Service
class UserService (val passwordEncoder : PasswordEncoder,
                   val userRepository: UserRepository,
                   val roleRepository: RoleRepository,
                   val verificationTokenRepository: VerificationTokenRepository,
                   val mailService: MailService): UserDetailsService {

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


    @Transactional
    fun signup(regReq: RegisterRequest){
        val user = User(
            username = regReq.username,
            password = passwordEncoder.encode(regReq.password),
            email = regReq.email,
            created = LocalDateTime.now(),
            enabled = false)

        val roleUser = roleRepository.save(Role("user"))
        user.roles.add(roleUser)
        userRepository.save(user)
        val token = generateVerificationToken(user)

        mailService.sendEmail(
            NotificationEmail("Please activate your account", user.email,
                "Thank you for signing up to Spring Reddit, " +
                        "please click on the below url to activate your account : " +
                        "http://localhost:8080/api/auth/accountVerification/" + token))
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
}
