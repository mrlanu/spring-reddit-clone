package com.github.mrlanu.springredditclone

import AuthenticationFilter
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder


@EnableWebSecurity
class SecurityConfig(private val userService: UserService, private val env: Environment, val passwordEncoder: PasswordEncoder) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authorizeRequests().antMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
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
