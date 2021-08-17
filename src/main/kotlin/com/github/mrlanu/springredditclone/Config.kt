package com.github.mrlanu.springredditclone

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@EnableWebSecurity
class SecurityConfig(private val userService: UserService, private val env: Environment, val passwordEncoder: PasswordEncoder) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        http.authorizeRequests()
            .antMatchers("/api/auth/**")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .addFilter(getAuthenticationFilter())
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder)
    }

    private fun getAuthenticationFilter(): AuthenticationFilter {
        val authFilter = AuthenticationFilter(userService, env, authenticationManager())
        authFilter.setFilterProcessesUrl("/api/auth/login")
        return authFilter
    }

}

class AuthenticationFilter(val userService: UserService, val env: Environment, authM: AuthenticationManager) :
    UsernamePasswordAuthenticationFilter(authM) {
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val loginReq = mapper.readValue(request.inputStream,
            Class.forName("com.github.mrlanu.springredditclone.LoginRequest")) as LoginRequest
        return authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(loginReq.email, loginReq.password, ArrayList()))
    }

    override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse,
                                          chain: FilterChain?, authResult: Authentication) {
        val user = authResult.principal as User

        val algorithm = Algorithm.HMAC256("secret".toByteArray())
        val accessToken = JWT.create()
            .withSubject(user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 10 * 60 *1000))
            .withIssuer(request.requestURL.toString())
            .withClaim("roles", user.authorities.map { grantedAuthority -> grantedAuthority.authority }.toMutableList())
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withSubject(user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 30 * 60 *1000))
            .withIssuer(request.requestURL.toString())
            .sign(algorithm)

        response.setHeader("access_token", accessToken)
        response.setHeader("refresh_token", refreshToken)


    }
}
