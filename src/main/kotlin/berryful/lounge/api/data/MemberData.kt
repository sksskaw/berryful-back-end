package berryful.lounge.api.data

import berryful.lounge.api.utils.ErrorMessageCode

data class SignupReq(
    var nickname: String,
    var phoneNumber: String,
    var agreePersonalInfo: Boolean,
    var agreeService: Boolean,
    var agreeMarketing: Boolean,
    var tempToken: String,
)

data class SnsSignupReq(
    var snsId: String,
    var snsType: String,
    var snsAccessToken: String,
)

data class SigninCodeZeroRes(
    val resultCode: Int = ErrorMessageCode.OK.code,
    val token: String,
    val memberInfo: MemberInfo
)

data class SigninCodeOneRes(
    val resultCode: Int = ErrorMessageCode.REQUEST_SIGNUP.code,
    val tempToken: String,
)

data class MemberInfo(
    val id: Long?,
    val email: String?,
    val nickname: String,
    val gender: String?,
    val birthday: String?,
    val profilePath: String?,
    val phoneNumber: String? = null,
    var snsApple: String? = null,
    var snsFacebook: String? = null,
    var snsKakao: String? = null,
    var snsNaver: String? = null,
    var snsType: String? = null,
    var profileIntro: String? = null,
    var youtubeUrl: String? = null,
    var instagramId: String? = null,
    var blogUrl: String? = null,
    var contentAlert: Boolean,
    var followAlert: Boolean,
    var berryfulAlert: Boolean,
)

data class MemberProfileInfo(
    val id: Long?,
    val nickname: String,
    val profilePath: String?,
    var profileIntro: String? = null,
    var youtubeUrl: String? = null,
    var instagramId: String? = null,
    var blogUrl: String? = null,
    var followerCount: Int? = 0,
    var followingCount: Int? = 0,
    var clipCount: Int? = 0,
    val follow: Boolean? = false,
)

data class UpdateMemberInfoReq(
    val updateData: String,
    val updateColumn: String,
)

data class UpdateMemberInfoRes(
    val resultCode: Int = 0,
    val email: String?,
    val phoneNumber: String?,
    val nickname: String,
    val profilePath: String?,
    val gender: String?,
    val birthday: String?,
    var profileIntro: String? = null,
    var youtubeUrl: String? = null,
    var instagramId: String? = null,
    var blogUrl: String? = null,
)

interface BlockedListRes {
    val nickname: String?
    val profilePath: String?
    val blockedMemberId: Long?
}

// Mixpanel User Properties
data class InitMemberRes(
    val notificationCheck: Boolean,
    val badgeCount: Int,
    val totalPostsCreated: Int,
    val totalClipsCreated: Int,
    val totalCommentsCreated: Int,
    val emailVerification: Boolean,
    val accountType: String,
    val notificationsEnabled: Boolean,
)

data class FollowReq(
    var action: String,
)

interface FollowingRes{
    val id: Long
    val nickname: String?
    val profilePath: String?
    val follow: Int?
}

interface FollowersRes{
    val id: Long
    val nickname: String?
    val profilePath: String?
    val follow: Int?
}

data class PushSettingReq(
    val alertType: String,
    val action: Boolean,
)

data class PushSettingRes(
    val contentAlert: Boolean,
    val followAlert: Boolean,
    val berryfulAlert: Boolean,
)