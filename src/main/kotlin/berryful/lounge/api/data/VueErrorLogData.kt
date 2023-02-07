package berryful.lounge.api.data

data class VueErrorLogReq(
    val memberId: Long? = 0L,
    val module: String?,
    val errorCode: Int?,
    val errorMsg: String?,
)