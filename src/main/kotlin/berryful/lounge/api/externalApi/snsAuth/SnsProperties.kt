package berryful.lounge.api.externalApi.snsAuth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SnsProperties {

    @Value("\${sns.facebook.endpoint}")
    lateinit var facebookEndpoint: String

    @Value("\${sns.kakao.endpoint}")
    lateinit var kakaoEndpoint: String
}