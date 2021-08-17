import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.mrlanu.springredditclone.LoginRequest
import com.github.mrlanu.springredditclone.TokensResponse
import com.github.mrlanu.springredditclone.UserService
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.util.MimeTypeUtils
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
                                          chain: FilterChain?, authResult: Authentication
    ) {
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

        /*response.setHeader("access_token", accessToken)
        response.setHeader("refresh_token", refreshToken)*/

        val tokens = TokensResponse(accessToken, refreshToken)
        response.contentType = MimeTypeUtils.APPLICATION_JSON_VALUE
        ObjectMapper().registerModule(KotlinModule()).writeValue(response.outputStream, tokens)

    }
}
