package com.github.mrlanu.springredditclone

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.util.MimeTypeUtils
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

class CustomAuthenticationFilter(authM: AuthenticationManager) :
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
        val tokens = Utils.generateTokens(user)
        response.contentType = MimeTypeUtils.APPLICATION_JSON_VALUE
        ObjectMapper().registerModule(KotlinModule()).writeValue(response.outputStream, tokens)

    }
}

class CustomAuthorizationFilter: OncePerRequestFilter(){
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if(request.requestURL.equals("/api/auth/login")) {
            filterChain.doFilter(request, response)
        } else {
            val authorizationHeader: String? = request.getHeader(AUTHORIZATION)
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                try {
                    val unpackedToken = Utils.unpackToken(authorizationHeader)
                    val authToken = UsernamePasswordAuthenticationToken(unpackedToken.username, null, unpackedToken.authorities)
                    SecurityContextHolder.getContext().authentication = authToken
                    filterChain.doFilter(request, response)
                }catch (exception: Exception){
                    exception.message?.let { Utils.responseError(response, it) }
                }
                }else{
                    filterChain.doFilter(request, response)
                }
            }
        }
}
