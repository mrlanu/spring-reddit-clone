package com.github.mrlanu.springredditclone

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.parserBuilder
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Instant
import java.util.Date.from


@Service
class JwtProvider{

    private var keyStore: KeyStore = KeyStore.getInstance("JKS")

    val jwtExpirationInMillis: Long? = 90

    init {
        val resourceAsStream = javaClass.getResourceAsStream("/spring.jks")
        keyStore.load(resourceAsStream, "secret".toCharArray())
    }

    fun generateToken(authentication: Authentication): String {
        val principal = authentication.principal as User
        return Jwts.builder()
            .setSubject(principal.username)
            .setIssuedAt(from(Instant.now()))
            .signWith(getPrivateKey())
            .setExpiration(from(jwtExpirationInMillis?.let { Instant.now().plusMillis(it) }))
            .compact()
    }

    private fun getPrivateKey(): PrivateKey {
        var tes = keyStore?.getKey("springblog", "secret".toCharArray()) as PrivateKey
        return tes
    }

    private fun getPublicKey(): PublicKey = keyStore?.getCertificate("springblog")?.publicKey as PublicKey

    fun validateToken(jwt: String?): Boolean {
        parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(jwt)
        return true
    }

    fun getUsernameFromJwt(token: String?): String? {
        val claims: Claims = parserBuilder()
            .setSigningKey(getPublicKey())
            .build()
            .parseClaimsJws(token)
            .body
        return claims.subject
    }

}
