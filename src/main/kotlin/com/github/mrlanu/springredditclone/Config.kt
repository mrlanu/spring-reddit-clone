package com.github.mrlanu.springredditclone

import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@EnableWebSecurity
class SecurityConfig(private val userService: UserService, private val env: Environment, val passwordEncoder: PasswordEncoder) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authorizeRequests().antMatchers("/api/auth/**").permitAll()

        http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/users")
            .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

        http.authorizeRequests().anyRequest().authenticated()
        http.addFilter(getAuthenticationFilter())
        http.addFilterBefore(CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder)
    }

    private fun getAuthenticationFilter(): CustomAuthenticationFilter {
        val authFilter = CustomAuthenticationFilter(authenticationManager())
        authFilter.setFilterProcessesUrl("/api/auth/login")
        return authFilter
    }

}
