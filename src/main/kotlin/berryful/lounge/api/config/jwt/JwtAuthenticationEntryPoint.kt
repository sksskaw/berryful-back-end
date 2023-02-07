package berryful.lounge.api.config.jwt

import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint{
    @Throws(IOException::class)
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse, authException: AuthenticationException?) {
        val objectMapper = ObjectMapper()
        response.contentType = "application/json"
        val jsonString = objectMapper.writeValueAsString(ApiResultCode(ErrorMessageCode.TOKEN_INVALID.code))
        response.writer.print(jsonString)
        response.writer.flush()
    }
}