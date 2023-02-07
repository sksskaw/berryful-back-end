package berryful.lounge.api.data

data class SendVerifyCodeReq(
    var phoneNumber: String,
)

data class PhoneNumberVerifyReq(
    var phoneNumber: String,
    var certNumber: String,
)

data class IsPhoneNumberExistReq(
    var phoneNumber: String,
)

data class CheckNicknameReq(
    var nickname: String,
)

data class IsEmailExistReq(
    var email: String,
)

data class EmailCertNumberReq(
    var email: String,
)

data class EmailVerifyReq(
    var email: String,
    var certNumber: String,
)

data class TokenVerifyReq(
    var accessToken: String,
)

data class SnsUserInfo(
    var snsPhoneNumber: String? = null,
    val snsEmail: String? = null,
    var snsGender: String? = null,
    var snsBirthday: String? = null,
)

data class RecommendNicknameRes(
    var nickname: String?,
)

interface RecommendNicknameWithId{
    var id: Long
    var nickname: String
}