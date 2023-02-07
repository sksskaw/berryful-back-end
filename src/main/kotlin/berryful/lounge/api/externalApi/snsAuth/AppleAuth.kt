package berryful.lounge.api.externalApi.snsAuth

import berryful.lounge.api.data.SnsUserInfo
import berryful.lounge.api.utils.Log
import com.auth0.jwt.JWT
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component

@Component
class AppleAuth : SnsAuthAdapter {
    override fun verifyAccessToken(accessToken: String, snsId: String): Boolean {
        val jwt = JWT.decode(accessToken)
        Log.out("AppleAuth verifyAccessToken", jwt.subject)
        return snsId == jwt.subject
    }

    override fun getSnsMemberInfo(accessToken: String, snsId: String): SnsUserInfo? {
        val jwt = JWT.decode(accessToken)
        val appleUserInfo = AppleUserInfo(
            id = jwt.subject,
            email = jwt.claims["email"].toString(),
        )
        Log.out("AppleAuth verifyAccessToken", "$appleUserInfo")


        return SnsUserInfo(
            snsEmail = appleUserInfo.email
        )
    }
}

data class AppleUserInfo(
    val id: String? = null,
    val email: String? = null,
)