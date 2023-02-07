package berryful.lounge.api.config.jwt

import berryful.lounge.api.config.SecurityConfig
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JwtFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {
    private val AUTHORIZATION_HEADER = "Authorization"
    private val BEARER_PREFIX = "Bearer "

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val url = request.requestURL.toString()
        url.lowercase(Locale.getDefault())
        val localApiPath = url.replace("http://localhost:8080", "")
        val devApiPath = url.replace("", "")
        val prodApiPath = url.replace("", "")

        val passUrl = SecurityConfig.EXCLUDED_PATHS
        val localPassUrl = passUrl.contains(localApiPath)
        val devPassUrl = passUrl.contains(devApiPath)
        val prodPassUrl = passUrl.contains(prodApiPath)

        if (localPassUrl || devPassUrl || prodPassUrl) {
            filterChain.doFilter(request, response)
            return
        }

        // 1. Request Header 에서 토큰을 꺼냄
        val jwt = resolveToken(request)

        // 2. validateToken 으로 토큰 유효성 검사
        // 정상 토큰이면 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장
        if (StringUtils.hasText(jwt)) {
            val authentication = jwt?.let {
                val objectMapper = ObjectMapper()
                response.contentType = "application/json"

                try{
                    jwtService.getAuthentication(it)
                } catch (e: TokenExpiredException) {
                    val jsonString = objectMapper.writeValueAsString(ApiResultCode(ErrorMessageCode.TOKEN_EXPIRED.code))
                    response.writer.print(jsonString)
                    response.writer.flush()
                    return
                } catch (e: JWTVerificationException) {
                    val jsonString = objectMapper.writeValueAsString(ApiResultCode(ErrorMessageCode.TOKEN_INVALID.code))
                    response.writer.print(jsonString)
                    response.writer.flush()
                    return
                }

            }
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    // Request Header 에서 토큰 정보를 꺼내오기
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7)
        }
        return null
    }
}