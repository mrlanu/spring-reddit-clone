package com.github.mrlanu.springredditclone

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.Async
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService (val passwordEncoder : PasswordEncoder,
                   val userRepository: UserRepository,
                   val verificationTokenRepository: VerificationTokenRepository,
                   val mailService: MailService){

    @Transactional
    fun signup(regReq: RegisterRequest){
        val user = User(regReq.username,
            passwordEncoder.encode(regReq.password),
            regReq.email, LocalDateTime.now(), false)
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
        val t = verificationTokenRepository.findByToken(token) ?: return null
        activateAccount(t)
        return "isActivated"
    }

    @Transactional
    fun activateAccount(token: VerificationToken){
        val user = userRepository.findByIdOrNull(token.user.userId)
        if (user != null) {
            user.enabled = true
            userRepository.save(user)
        }else println("User not found")
    }
}

@Service
class MailService (val mailContentBuilder: MailContentBuilder, val mailSender: JavaMailSender){

    @Async
    fun sendEmail(notificationEmail: NotificationEmail){
        val messagePreparator = MimeMessagePreparator { mimeMessage ->
            val messageHelper = MimeMessageHelper(mimeMessage)
            messageHelper.setFrom("mrlanu@yahoo.com")
            messageHelper.setTo(notificationEmail.recipient)
            messageHelper.setSubject(notificationEmail.subject)
            messageHelper.setText(mailContentBuilder.build(notificationEmail.body))
        }

        mailSender.send(messagePreparator)
    }

}

@Service
class MailContentBuilder(val templateEngine: TemplateEngine) {
    fun build(message: String): String {
        val context = Context()
        context.setVariable("message", message)
        return templateEngine.process("mailTemplate", context)
    }
}
