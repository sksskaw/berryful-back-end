package berryful.lounge.api.externalApi.snsAuth

import berryful.lounge.api.data.SnsUserInfo
import berryful.lounge.api.utils.Log
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class KakaoAuth(
    private val snsProperties: SnsProperties,
    private val restTemplate: RestTemplate
) : SnsAuthAdapter {
    override fun verifyAccessToken(accessToken: String, snsId: String): Boolean {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)
        val httpEntity = HttpEntity<Map<*, *>>(headers)

        val kakaoUserInfo: KakaoUserInfo
        try {
            kakaoUserInfo = restTemplate.exchange(
                "${snsProperties.kakaoEndpoint}/v2/user/me",
                HttpMethod.GET,
                httpEntity,
                KakaoUserInfo::class.java
            ).body ?: return false
        } catch (e: Exception) {
            Log.out("kakao verifyAccessToken Exception: ", "$e")
            return false
        }

        Log.out("kakao verifyAccessToken", "$kakaoUserInfo")
        return snsId == kakaoUserInfo.id
    }

    override fun getSnsMemberInfo(accessToken: String, snsId: String): SnsUserInfo? {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)
        val httpEntity = HttpEntity<Map<*, *>>(headers)

        val kakaoUserInfo: KakaoUserInfo
        try {
            kakaoUserInfo = restTemplate.exchange(
                "${snsProperties.kakaoEndpoint}/v2/user/me",
                HttpMethod.GET,
                httpEntity,
                KakaoUserInfo::class.java
            ).body ?: return null
        } catch (e: Exception) {
            Log.out("kakao verifyAccessToken Exception: ", "$e")
            return null
        }
        Log.out("kakao verifyAccessToken", "$kakaoUserInfo")

        return SnsUserInfo(
            snsPhoneNumber = kakaoUserInfo.kakaoAccount.phone_number,
            snsEmail = kakaoUserInfo.kakaoAccount.email,
            snsGender = formatGender(kakaoUserInfo.kakaoAccount.gender),
            snsBirthday = formatBirthday(kakaoUserInfo.kakaoAccount.birthyear, kakaoUserInfo.kakaoAccount.birthday)
        )
    }

    fun formatBirthday(birthyear: String?, birthday: String?): String {
        val year = birthyear?:"xxxx"
        val day = StringBuffer(birthday?:"xxxx").insert(2,".").toString()
        return "$year.$day"
    }

    fun formatGender(gender: String?): String? {
        if (gender == "male") return "M"
        if (gender == "female") return "F"
        return null
    }
}

data class KakaoUserInfo(
    val id: String,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount,
)

data class KakaoAccount(
    var email: String? = null,
    var gender: String? = null,
    var birthday: String? = null,
    var birthyear: String? = null,
    var phone_number: String? = null,
)