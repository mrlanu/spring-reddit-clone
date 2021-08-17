package com.github.mrlanu.springredditclone

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

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
