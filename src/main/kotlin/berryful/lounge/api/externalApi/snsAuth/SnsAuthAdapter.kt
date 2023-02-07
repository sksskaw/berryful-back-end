package berryful.lounge.api.externalApi.snsAuth

interface SnsAuthAdapter {
    fun verifyAccessToken(accessToken: String, snsId: String): Boolean = false
    fun getSnsMemberInfo(accessToken: String, snsId: String): Any?
}