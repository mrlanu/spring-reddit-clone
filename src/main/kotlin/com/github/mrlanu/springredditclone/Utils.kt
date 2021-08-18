package com.github.mrlanu.springredditclone

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.util.MimeTypeUtils
import java.util.*
import javax.servlet.http.HttpServletResponse

class Utils {
    companion object Token {
        fun generateTokens(user: User): TokensResponse {
            val algorithm = Algorithm.HMAC256("secret".toByteArray())
            val accessToken = JWT.create()
                .withSubject(user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 10 * 60 *1000))
                .withClaim("roles", user.authorities.map { grantedAuthority -> grantedAuthority.authority }.toMutableList())
                .sign(algorithm)

            val refreshToken = JWT.create()
                .withSubject(user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 30 * 60 *1000))
                .sign(algorithm)

            return TokensResponse(accessToken, refreshToken)
        }

        fun unpackToken(token: String): UnpackedToken {
            val clearToken = token.substring("Bearer ".length)
            val algorithm = Algorithm.HMAC256("secret".toByteArray())
            val verifier = JWT.require(algorithm).build()
            val decodedJWT = verifier.verify(clearToken)
            val username = decodedJWT.subject
            val roles = decodedJWT.getClaim("roles")?.asArray(String::class.java)
            val authorities = ArrayList<SimpleGrantedAuthority>()
            roles?.forEach { role -> authorities.add(SimpleGrantedAuthority(role)) }
            return UnpackedToken(username, authorities)
        }

        fun responseError(response: HttpServletResponse, message: String){
            response.setHeader("error", message)
            val error = ErrorResponse(message)
            response.contentType = MimeTypeUtils.APPLICATION_JSON_VALUE
            ObjectMapper().registerModule(KotlinModule()).writeValue(response.outputStream, error)
        }
    }
}

class UnpackedToken(
    val username: String,
    val authorities: List<SimpleGrantedAuthority>
)
