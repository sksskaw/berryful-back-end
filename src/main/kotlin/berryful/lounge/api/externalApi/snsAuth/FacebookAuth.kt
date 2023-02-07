package berryful.lounge.api.externalApi.snsAuth

import berryful.lounge.api.data.SnsUserInfo
import berryful.lounge.api.utils.Log
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class FacebookAuth(
    private val snsProperties: SnsProperties,
    private val restTemplate: RestTemplate
) : SnsAuthAdapter {
    override fun verifyAccessToken(accessToken: String, snsId: String): Boolean {
        val facebookUserInfo = restTemplate.getForObject(
            "${snsProperties.facebookEndpoint}/v12.0/me?access_token=${accessToken}&fields=id,name,birthday,email,gender",
            FacebookUserInfo::class.java
        ) ?: return false

        Log.out("facebook resAppToken: ", "$facebookUserInfo")
        return snsId == facebookUserInfo.id
    }

    override fun getSnsMemberInfo(accessToken: String, snsId: String): SnsUserInfo? {
        val facebookUserInfo = restTemplate.getForObject(
            "${snsProperties.facebookEndpoint}/v12.0/me?access_token=${accessToken}&fields=id,name,birthday,email,gender",
            FacebookUserInfo::class.java
        ) ?: return null
        Log.out("facebook resAppToken: ", "$facebookUserInfo")

        return SnsUserInfo(
            snsEmail = facebookUserInfo.email,
            snsGender = formatGender(facebookUserInfo.gender),
            snsBirthday = formatBirthday(facebookUserInfo.birthday)
        )
    }

    fun formatBirthday(birthday: String?): String? {
        birthday ?: return null
        val birthdayStr = birthday.replace("/", "")
        val year = StringBuffer(birthdayStr.substring(4)).toString()
        val day = StringBuffer(birthdayStr.substring(0, 4)).insert(2, ".").toString()
        return "$year.$day"
    }

    fun formatGender(gender: String?): String? {
        if (gender == "male") return "M"
        if (gender == "female") return "F"
        return null
    }
}

data class FacebookUserInfo(
    val id: String,
    val name: String? = null,
    val birthday: String? = null,
    val email: String? = null,
    val gender: String? = null,
)