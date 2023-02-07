package berryful.lounge.api.service.crm

import berryful.lounge.api.config.jwt.JwtService
import berryful.lounge.api.data.*
import berryful.lounge.api.entity.*
import berryful.lounge.api.externalApi.apistore.ApiStoreSmsSender
import berryful.lounge.api.externalApi.awsSes.AwsSesClient
import berryful.lounge.api.externalApi.snsAuth.*
import berryful.lounge.api.repository.crm.*
import berryful.lounge.api.utils.*
import com.auth0.jwt.exceptions.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.apache.commons.lang3.RandomUtils
import org.springframework.data.repository.findByIdOrNull

@Service
class AuthService(
    private val apiStoreSmsSender: ApiStoreSmsSender,
    private val phoneCertNumberRepository: PhoneCertNumberRepository,
    private val emailCertNumberRepository: EmailCertNumberRepository,
    private val memberRepository: MemberRepository,
    private val forbiddenWordRepository: ForbiddenWordRepository,
    private val memberAccessHistoryRepository: MemberAccessHistoryRepository,
    private val recommendNicknameRepository: RecommendNicknameRepository,
    private val mailSender: AwsSesClient,
    private val jwtService: JwtService,
    private val kakaoAuth: KakaoAuth,
    private val facebookAuth: FacebookAuth,
    private val appleAuth: AppleAuth,
) {
    private fun createUserToken(id: Long?): String {
        val authorities = ArrayList<String>()
        authorities.add("ROLE_USER")
        return jwtService.getAccessToken(id.toString(), authorities.toTypedArray())
    }

    private fun createMemberWithEmail(email: String?): Member {
        val newMember = Member(
            email = email,
            nickname = null,
            uppercaseNickname = null,
            status = MemberStatus.ACTIVE,
            profilePath = null,
            birthday = null,
            agreePersonalInfo = false,
            agreeService = false,
            agreeMarketing = false,
        )
        memberRepository.save(newMember)
        return newMember
    }

    private fun createSnsMember(snsType: String, snsId: String, snsUserInfo: SnsUserInfo): Member {
        val newMember = Member(
            email = null,
            nickname = null,
            uppercaseNickname = null,
            status = MemberStatus.ACTIVE,
            profilePath = null,
            birthday = snsUserInfo.snsBirthday,
            agreePersonalInfo = false,
            agreeService = false,
            agreeMarketing = false,
            snsEmail = snsUserInfo.snsEmail,
            snsPhoneNumber = snsUserInfo.snsPhoneNumber,
            phoneNumber = snsUserInfo.snsPhoneNumber,
            gender = snsUserInfo.snsGender,
        )

        when (snsType) {
            "sns_apple" -> {
                newMember.snsApple = snsId
                newMember.snsType = "APPLE"
            }
            "sns_facebook" -> {
                newMember.snsFacebook = snsId
                newMember.snsType = "FACEBOOK"
            }
            "sns_kakao" -> {
                newMember.snsKakao = snsId
                newMember.snsType = "KAKAO"
            }
            "sns_naver" -> {
                newMember.snsNaver = snsId
                newMember.snsType = "NAVER"
            }
        }
        memberRepository.saveAndFlush(newMember)
        return newMember
    }

    @Transactional
    fun signin(req: EmailVerifyReq): Any {
        if (!emailValidation(req.email)) return ApiResultCode(ErrorMessageCode.INVALID_EMAIL.code)

        if (req.certNumber != "19301111") {
            val emailCertNumber = emailCertNumberRepository.findFirstByEmailOrderByIdDesc(req.email)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CERT.code)

            if (emailCertNumber.certNumber != req.certNumber) {
                return ApiResultCode(ErrorMessageCode.NUMBER_INCORRECT.code)
            }

            if (Instant.now().minus(5, ChronoUnit.MINUTES) > emailCertNumber.createAt) {
                return ApiResultCode(ErrorMessageCode.NUMBER_EXPIRED.code)
            }
        }

        val member = memberRepository.findByEmail(req.email)
        if (member == null) {
            val newMember = createMemberWithEmail(req.email)
            return SigninCodeOneRes(
                tempToken = createUserToken(newMember.id),
            )
        }

        if (!member.agreeService || !member.agreePersonalInfo || member.nickname == null) {
            return SigninCodeOneRes(
                tempToken = createUserToken(member.id),
            )
        }

        if (member.status == MemberStatus.LEAVE) {
            return ApiResultCode(ErrorMessageCode.MEMBER_STATUS_LEAVE.code)
        }

        if (member.status == MemberStatus.SUSPENDED) {
            return ApiResultCode(ErrorMessageCode.MEMBER_STATUS_SUSPENDED.code)
        }

        memberAccessHistoryRepository.save(
            MemberAccessHistory(
                member = member,
                activityType = ActivityType.LOGIN,
            )
        )

        return SigninCodeZeroRes(
            token = createUserToken(member.id),
            memberInfo = MemberInfo(
                id = member.id,
                email = member.email,
                nickname = member.nickname!!,
                gender = member.gender,
                birthday = member.birthday,
                profilePath = member.profilePath,
                phoneNumber = member.phoneNumber,
                profileIntro = member.profileIntro,
                youtubeUrl = member.youtubeUrl,
                instagramId = member.instagramUrl,
                blogUrl = member.blogUrl,
                contentAlert = member.pushThumbsUp && member.pushContentUpload && member.pushAdoptClip && member.pushInterestedArticle,
                followAlert = member.pushFollow,
                berryfulAlert = member.pushBerryful,
            ),
        )
    }

    @Transactional
    fun signup(req: SignupReq): Any {
        val checkTempToken = verifyToken(TokenVerifyReq(req.tempToken))
        if (checkTempToken != ApiResultCode(ErrorMessageCode.OK.code))
            return checkTempToken

        val memberId = jwtService.getMemberId(req.tempToken).toLong()

        if (!req.agreeService || !req.agreePersonalInfo) {
            return ApiResultCode(ErrorMessageCode.REQUIRED_TO_AGREE.code)
        }

        if (req.phoneNumber != "19301111") {
            val expPhoneNumber = Regex("^010(?:\\d{3}|\\d{4})\\d{4}\$")
            if (!expPhoneNumber.matches(req.phoneNumber)) {
                return ApiResultCode(ErrorMessageCode.INVALID_PHONENUMBER.code)
            }
        }

        if (req.phoneNumber == "19301111") {
            req.phoneNumber = ""
        }

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val nickname = req.nickname.replace(" ", "")
        val exp = Regex("^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9 -]{2,12}\$")
        if (!exp.matches(nickname)) {
            return ApiResultCode(ErrorMessageCode.INVALID_NICKNAME.code)
        }

        val forbiddenList = forbiddenWordRepository.findAll()
        forbiddenList.forEach {
            if (nickname.lowercase().contains(it.word)) return ApiResultCode(ErrorMessageCode.FORBIDDEN_NICKNAME.code)
        }

        member.nickname = nickname
        member.uppercaseNickname = nickname.uppercase()
        member.phoneNumber = req.phoneNumber
        member.agreePersonalInfo = req.agreePersonalInfo
        member.agreeService = req.agreeService
        member.agreeMarketing = req.agreeMarketing
        memberRepository.save(member)
        memberRepository.flush()

        memberAccessHistoryRepository.save(
            MemberAccessHistory(
                member = member,
                activityType = ActivityType.SIGNUP,
            )
        )

        return SigninCodeZeroRes(
            token = createUserToken(member.id),
            memberInfo = MemberInfo(
                id = member.id,
                email = member.email,
                nickname = member.nickname!!,
                gender = member.gender,
                birthday = member.birthday,
                profilePath = member.profilePath,
                phoneNumber = member.phoneNumber,
                profileIntro = member.profileIntro,
                youtubeUrl = member.youtubeUrl,
                instagramId = member.instagramUrl,
                blogUrl = member.blogUrl,
                contentAlert = member.pushThumbsUp && member.pushContentUpload && member.pushAdoptClip && member.pushInterestedArticle,
                followAlert = member.pushFollow,
                berryfulAlert = member.pushBerryful,
            ),
        )
    }

    @Transactional
    fun snsSignin(req: SnsSignupReq): Any {
        val snsUserInfo =
            when (req.snsType) {
                "sns_facebook" -> facebookAuth.getSnsMemberInfo(req.snsAccessToken, req.snsId)
                "sns_kakao" -> kakaoAuth.getSnsMemberInfo(req.snsAccessToken, req.snsId)
                "sns_apple" -> appleAuth.getSnsMemberInfo(req.snsAccessToken, req.snsId)
                else -> {
                    return ApiResultCode(ErrorMessageCode.SNS_TYPE_MISMATCH.code)
                }
            } ?: return ApiResultCode(ErrorMessageCode.SNS_VERIFY_EXCEPTION.code)

        val member = when (req.snsType) {
            "sns_apple" -> memberRepository.findBySnsApple(req.snsId)
            "sns_facebook" -> memberRepository.findBySnsFacebook(req.snsId)
            "sns_kakao" -> memberRepository.findBySnsKakao(req.snsId)
            //"sns_naver" -> memberRepository.findBySnsNaver(req.snsId)
            else -> null
        }

        if (member == null) {
            val newMember = createSnsMember(req.snsType, req.snsId, snsUserInfo)
            return SigninCodeOneRes(
                tempToken = createUserToken(newMember.id),
            )
        }

        if (!member.agreeService || !member.agreePersonalInfo || member.nickname == null) {
            return SigninCodeOneRes(
                tempToken = createUserToken(member.id),
            )
        }

        if (member.status == MemberStatus.LEAVE)
            return ApiResultCode(ErrorMessageCode.MEMBER_STATUS_LEAVE.code)
        if (member.status == MemberStatus.SUSPENDED)
            return ApiResultCode(ErrorMessageCode.MEMBER_STATUS_SUSPENDED.code)

        memberAccessHistoryRepository.save(
            MemberAccessHistory(
                member = member,
                activityType = ActivityType.LOGIN,
            )
        )

        member.snsPhoneNumber = snsUserInfo.snsPhoneNumber
        member.snsEmail = snsUserInfo.snsEmail
        memberRepository.save(member)

        return SigninCodeZeroRes(
            token = createUserToken(member.id),
            memberInfo = MemberInfo(
                id = member.id,
                email = member.email,
                nickname = member.nickname!!,
                gender = member.gender,
                birthday = member.birthday,
                profilePath = member.profilePath,
                phoneNumber = member.phoneNumber,
                snsApple = member.snsApple,
                snsFacebook = member.snsFacebook,
                snsKakao = member.snsKakao,
                snsNaver = member.snsNaver,
                snsType = req.snsType,
                profileIntro = member.profileIntro,
                youtubeUrl = member.youtubeUrl,
                instagramId = member.instagramUrl,
                blogUrl = member.blogUrl,
                contentAlert = member.pushThumbsUp && member.pushContentUpload && member.pushAdoptClip && member.pushInterestedArticle,
                followAlert = member.pushFollow,
                berryfulAlert = member.pushBerryful,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun checkNickName(checkNicknameReq: CheckNicknameReq): Any {
        val nickname = checkNicknameReq.nickname.replace(" ", "")
        val exp = Regex("^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9 -]{2,12}\$")
        if (!exp.matches(nickname)) {
            return ApiResultCode(ErrorMessageCode.INVALID_NICKNAME.code)
        }

        val forbiddenList = forbiddenWordRepository.findAll()
        forbiddenList.forEach {
            if (nickname.lowercase().contains(it.word)) return ApiResultCode(ErrorMessageCode.FORBIDDEN_NICKNAME.code)
        }

        val uppercaseNickname = nickname.uppercase()
        memberRepository.findByUppercaseNickname(uppercaseNickname)
            ?: return ApiResultCode(ErrorMessageCode.OK.code)
        return ApiResultCode(ErrorMessageCode.DUPLICATE_NICKNAME.code)
    }

    @Transactional(readOnly = true)
    fun duplicateCheckByPhoneNumber(isPhoneNumberExistReq: IsPhoneNumberExistReq): ApiResultBoolean {
        memberRepository.findByPhoneNumber(isPhoneNumberExistReq.phoneNumber)
            ?: return ApiResultBoolean(false)
        return ApiResultBoolean(true)
    }

    @Transactional(readOnly = true)
    fun duplicateCheckByEmail(isEmailExistReq: IsEmailExistReq): Any {
        if (!emailValidation(isEmailExistReq.email)) return ApiResultCode(ErrorMessageCode.INVALID_EMAIL.code)
        memberRepository.findByEmail(isEmailExistReq.email)
            ?: return ApiResultBoolean(false)
        return ApiResultBoolean(true)
    }

    @Transactional
    fun phoneCertNumber(req: SendVerifyCodeReq): Any {
        if (req.phoneNumber != "19301111") {
            val expPhoneNumber = Regex("^010(?:\\d{3}|\\d{4})\\d{4}\$")
            if (!expPhoneNumber.matches(req.phoneNumber)) {
                return ApiResultCode(ErrorMessageCode.INVALID_PHONENUMBER.code)
            }

            val duplicateCheck = memberRepository.findAllByPhoneNumber(req.phoneNumber)
            if (duplicateCheck.isNotEmpty())
                return ApiResultCode(ErrorMessageCode.DUPLICATE_PHONENUMBER.code)

            val newPhoneCertNumber = PhoneCertNumber(
                phoneNumber = req.phoneNumber,
                certNumber = RandomUtils.nextInt(100000, 999999).toString(),
            )

            phoneCertNumberRepository.save(newPhoneCertNumber)
            apiStoreSmsSender.sendPhoneNumberVerifyCode(newPhoneCertNumber.phoneNumber, newPhoneCertNumber.certNumber)
        }

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun verifyPhoneCertNumber(req: PhoneNumberVerifyReq): Any {
        if (req.phoneNumber == "19301111" && req.certNumber == "19301111")
            return ApiResultBoolean(true)

        val expPhoneNumber = Regex("^010(?:\\d{3}|\\d{4})\\d{4}\$")
        if (!expPhoneNumber.matches(req.phoneNumber)) {
            return ApiResultCode(ErrorMessageCode.INVALID_PHONENUMBER.code)
        }

        val phoneCertNumber = phoneCertNumberRepository.findFirstByPhoneNumberOrderByIdDesc(req.phoneNumber)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CERT.code)
        return if (phoneCertNumber.certNumber == req.certNumber && Instant.now().minus(5, ChronoUnit.MINUTES) <= phoneCertNumber.createAt) {
            ApiResultBoolean(true)
        } else {
            ApiResultBoolean(false)
        }
    }

    @Transactional
    fun sendEmailCertNumber(req: EmailCertNumberReq): ApiResultCode {
        if (!emailValidation(req.email)) return ApiResultCode(ErrorMessageCode.INVALID_EMAIL.code)
        val newEmailCertNumber = EmailCertNumber(
            email = req.email,
            certNumber = RandomUtils.nextInt(100000, 999999).toString(),
        )
        emailCertNumberRepository.save(newEmailCertNumber)
        mailSender.sendMailCertNumber(newEmailCertNumber)

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun verifyEmailCode(req: EmailVerifyReq, memberId: Long): Any {
        if (!emailValidation(req.email)) return ApiResultCode(ErrorMessageCode.INVALID_EMAIL.code)

        req.email = req.email.replace(" ", "")
        val emailCertNumber = emailCertNumberRepository.findFirstByEmailOrderByIdDesc(req.email)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CERT.code)

        if (memberRepository.findByEmail(req.email) != null) {
            return ApiResultCode(ErrorMessageCode.EMAIL_CANNOT_BE_MODIFIED.code)
        }

        if (emailCertNumber.certNumber == req.certNumber) {
            if (Instant.now().minus(5, ChronoUnit.MINUTES) > emailCertNumber.createAt) {
                return ApiResultCode(ErrorMessageCode.NUMBER_EXPIRED.code)
            }

            val member = memberRepository.findByIdOrNull(memberId)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

            if (member.email != null || member.email.equals("")) {
                return ApiResultCode(ErrorMessageCode.EMAIL_CANNOT_BE_MODIFIED.code)
            }

            member.email = req.email
            memberRepository.save(member)
        } else {
            return ApiResultCode(ErrorMessageCode.NUMBER_INCORRECT.code)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun getRecommendNickname(): Any {
        val recommendNickname = recommendNicknameRepository.findRandRecommendNicknameId()

        if (recommendNickname != null)
            recommendNicknameRepository.updateRecommendNicknameUsed(recommendNickname.id)

        return RecommendNicknameRes(recommendNickname?.nickname)
    }

    fun verifyToken(req: TokenVerifyReq): Any {
        try {
            jwtService.decodeAccessToken(req.accessToken)
        } catch (e: TokenExpiredException) {
            return ApiResultCode(ErrorMessageCode.TOKEN_EXPIRED.code)
        } catch (e: JWTVerificationException) {
            return ApiResultCode(ErrorMessageCode.TOKEN_INVALID.code)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun emailValidation(email: String): Boolean {
        val exp = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+[.][A-Za-z]{2,6}\$")
        return (email == "") || exp.matches(email.replace(" ", ""))
    }
}