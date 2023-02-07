package berryful.lounge.api.controller.crm

import berryful.lounge.api.data.*
import berryful.lounge.api.service.crm.AuthService
import berryful.lounge.api.utils.Log
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  회원가입, 회원인증 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/crm/v2")
class AuthController(
    private val authService: AuthService
) {
    /**
     * 회원가입 API 입니다.
     * @param signupReq 회원가입할 회원 정보입니다.
     * @return 가입된 회원의 token, memberInfo 입니다.
     */
    @PostMapping("/signup")
    fun signup(@RequestBody signupReq: SignupReq): ResponseEntity<Any> {
        Log.out("AuthController.signup()", "$signupReq")
        return ResponseEntity
            .ok()
            .body(authService.signup(signupReq))
    }

    /**
     * 로그인 이메일 인증 번호 검증 API 입니다.
     * @param emailVerifyReq 이메일 검증을 위한 이메일, 인증번호 입니다.
     * @return 요청 결과에 따른 resultCode, 토큰값, 회원정보 입니다.
     */
    @PostMapping("/signin")
    fun signin(@RequestBody emailVerifyReq: EmailVerifyReq): ResponseEntity<Any> {
        Log.out("AuthController.signin()", "$emailVerifyReq")
        return ResponseEntity
            .ok()
            .body(authService.signin(emailVerifyReq))
    }

    /**
     * 간편 로그인 API 입니다.
     * @param snsSignupReq 간편 로그인을 요청한 회원 정보 입니다.
     * @return 로그인한 회원정보와 토큰값입니다.
     */
    @PostMapping("/snsSignin")
    fun snsSignin(@RequestBody snsSignupReq: SnsSignupReq): ResponseEntity<Any> {
        Log.out("AuthController.snsSignin()", "$snsSignupReq")
        return ResponseEntity
            .ok()
            .body(authService.snsSignin(snsSignupReq))
    }

    /**
     * 휴대폰 번호 중복 검사 API 입니다.
     * @param isPhoneNumberExistReq 중복 검사를 진행할 휴대폰 번호입니다.
     * @return 검사 결과에 따른 Boolean 입니다.
     */
    @PostMapping("/isPhoneNumberExist")
    fun isPhoneNumberExist(@RequestBody isPhoneNumberExistReq: IsPhoneNumberExistReq): ResponseEntity<Any> {
        Log.out("AuthController.isPhoneNumberExist()", "$isPhoneNumberExistReq")
        return ResponseEntity
            .ok()
            .body(authService.duplicateCheckByPhoneNumber(isPhoneNumberExistReq))
    }

    /**
     * 닉네임 중복 검사 API 입니다.
     * @param checkNicknameReq 중복 검사를 진행할 닉네임입니다.
     * @return 검사 결과에 따른 Boolean 입니다.
     */
    @PostMapping("/checkNickname")
    fun checkNickname(@RequestBody checkNicknameReq: CheckNicknameReq): ResponseEntity<Any> {
        Log.out("AuthController.checkNickname()", "$checkNicknameReq")
        return ResponseEntity
            .ok()
            .body(authService.checkNickName(checkNicknameReq))
    }

    /**
     * 이메일 중복 검사 API 입니다.
     * @param isEmailExistReq 중복 검사를 진행할 닉네임입니다.
     * @return 검사 결과에 따른 Boolean 입니다.
     */
    @PostMapping("/isEmailExist")
    fun isEmailExist(@RequestBody isEmailExistReq: IsEmailExistReq): ResponseEntity<Any> {
        Log.out("AuthController.isEmailExist()", "$isEmailExistReq")
        return ResponseEntity
            .ok()
            .body(authService.duplicateCheckByEmail(isEmailExistReq))
    }

    /**
     * 휴대폰 인증번호 요청 API 입니다.
     * @param sendVerifyCodeReq 인증번호를 요청한 휴대폰 번호입니다.
     * @return DB에 생성된 인증번호 id 입니다.
     */
    @PostMapping("/phoneCertNumber")
    fun phoneCertNumber(@RequestBody sendVerifyCodeReq: SendVerifyCodeReq): ResponseEntity<Any> {
        Log.out("AuthController.phoneCertNumber()", "$sendVerifyCodeReq")
        return ResponseEntity
            .ok()
            .body(authService.phoneCertNumber(sendVerifyCodeReq))
    }

    /**
     * 휴대폰 인증번호 검증 API 입니다.
     * @param phoneNumberVerifyReq 휴대폰 번호와 인증번호입니다.
     * @return 인증 결과에 따른 Boolean 입니다.
     */
    @PostMapping("/verifyPhoneCertNumber")
    fun verifyPhoneCertNumber(@RequestBody phoneNumberVerifyReq: PhoneNumberVerifyReq): ResponseEntity<Any> {
        Log.out("AuthController.verifyPhoneCertNumber()", "$phoneNumberVerifyReq")
        return ResponseEntity
            .ok()
            .body(authService.verifyPhoneCertNumber(phoneNumberVerifyReq))
    }

    /**
     * 이메일 인증 번호 요청 API 입니다.
     * @param emailCertNumberReq 이메일 인증 번호를 요청한 이메일입니다.
     * @return 요청 결과에 따른 메시지 입니다.
     */
    @PostMapping("/emailCertNumber")
    fun emailCertNumber(@RequestBody emailCertNumberReq: EmailCertNumberReq): ResponseEntity<Any> {
        Log.out("AuthController.emailCertNumber()", "$emailCertNumberReq")
        return ResponseEntity
            .ok()
            .body(authService.sendEmailCertNumber(emailCertNumberReq))
    }

    /**
     * 이메일 인증 번호 검증 API 입니다.
     * @param emailVerifyReq 이메일 검증을 위한 이메일, 인증번호 입니다.
     * @return 요청 결과에 따른 Boolean 입니다.
     */
    @PostMapping("/verifyEmailCertNumber")
    fun verifyEmailCertNumber(
        @RequestBody emailVerifyReq: EmailVerifyReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        Log.out("AuthController.verifyEmailCertNumber()", "$emailVerifyReq")
        val memberId = request.userPrincipal!!.name.toLong()
        return ResponseEntity
            .ok()
            .body(authService.verifyEmailCode(emailVerifyReq, memberId))
    }

    /**
     * 토큰 검증 API 입니다.
     * @param tokenVerifyReq 토큰 검증을 토큰 값 입니다.
     * @return 정상 토큰이면 요청한 토큰 리턴, 시간이 만료된 토큰이면 새로운 토큰을 리턴합니다.
     */
    @PostMapping("/verifyToken")
    fun verifyToken(@RequestBody tokenVerifyReq: TokenVerifyReq): ResponseEntity<Any> {
        Log.out("AuthController.verifyToken()", "$tokenVerifyReq")
        return ResponseEntity
            .ok()
            .body(authService.verifyToken(tokenVerifyReq))
    }

    /**
     * 닉네임 추천 API 입니다.
     */
    @GetMapping("/recommend/nickname")
    fun getRecommendNickname(): ResponseEntity<Any> {
        Log.out("AuthController.getRecommendNickname", "")

        return ResponseEntity
            .ok()
            .body(authService.getRecommendNickname())
    }
}