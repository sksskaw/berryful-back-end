package berryful.lounge.api.config

import berryful.lounge.api.config.jwt.JwtService
import berryful.lounge.api.config.jwt.JwtAccessDeniedHandler
import berryful.lounge.api.config.jwt.JwtAuthenticationEntryPoint
import berryful.lounge.api.config.jwt.JwtSecurityConfig
import berryful.lounge.api.utils.Log
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configurable
@EnableWebSecurity
class SecurityConfig(
    private val corsConfig: CorsConfig,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val jwtService: JwtService,
) {
    companion object {
        val EXCLUDED_PATHS = arrayOf(
            "/crm/v2/signin",
            "/crm/v2/snsSignin",
            "/crm/v2/signup",

            "/crm/v2/isPhoneNumberExist",
            "/crm/v2/isEmailExist",
            "/crm/v2/checkNickname",

            "/crm/v2/phoneCertNumber",
            "/crm/v2/verifyPhoneCertNumber",
            "/crm/v2/emailCertNumber",
            "/crm/v2/verifyEmailCertNumber",

            "/crm/v2/verifyToken",
            "/crm/v2/device",
            "/crm/v2/member/app/open",
            "/crm/v2/checkVersion",

            "/lounge/v2/clip/encoding/complete",
            "/lounge/v2/clipTimeline",

            "/lounge/v2/search",

            // test
            "/crm/v2/push/test",
            "/crm/v2/push/reset/test",

            "/crm/v2/vue/error/loging",
            "/analyze/content",
            "/learning/model/{id}",
        )

        val EXCLUDED_GET_PATHS = arrayOf(
            "/lounge/v2/popular/postClips",
            "/lounge/v2/postList",
            "/lounge/v2/post/{id}",
            "/lounge/v2/categoryList",
            "/lounge/v2/hot/post",
            "/lounge/v2/best/pick/post",

            "/lounge/v2/clipList",
            "/lounge/v2/clipList/{postId}",
            "/lounge/v2/clip/{id}",
            "/lounge/v2/popular/clipList",

            "/lounge/v2/commentList/{clipId}",
            "/lounge/v2/replyList/{commentId}",
            "/lounge/v2/count/post/{id}",
            "/lounge/v2/count/clip/{id}",

            "/lounge/v2/search/autocomplete/keywords",
            "/lounge/v2/search/autocomplete/tags",

            "/lounge/v2/challenge/bannerList",
            "/lounge/v2/challengeList",
            "/lounge/v2/challenge/{challengeId}",

            "/lounge/v2/recommend/clips",

            "/crm/v2/memberInfo/{id}",
            "/crm/v2/member/uploadedList/{id}",
            "/crm/v2/member/following/{id}",
            "/crm/v2/member/followers/{id}",

            "/crm/v2/recommend/nickname",

            "/broadcasts",
            "/broadcast/{id}",
            "/sale/items/{broadcastId}",
        )
    }

    @Throws(Exception::class)
    @Bean
    fun filterChain(http: HttpSecurity) : SecurityFilterChain {
        http
            .addFilter(corsConfig.corsFilter())
            .cors().and()
            .csrf().disable()

            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .formLogin().disable()
            .httpBasic().disable()
            .authorizeRequests()
            .antMatchers(*EXCLUDED_PATHS).permitAll()
            .antMatchers(HttpMethod.GET, *EXCLUDED_GET_PATHS).permitAll()
            .antMatchers("/**", "/**")
            .access("hasRole('ROLE_USER')")
            .and()
            .apply(JwtSecurityConfig(jwtService))
        return http.build()
    }
}