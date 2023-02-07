package berryful.lounge.api.externalApi.apistore

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class ApiStoreSmsSender @Autowired constructor(
    private val apiSmsAlimSender: ApiStoreSmsClient
) {
    fun sendPhoneNumberVerifyCode(phoneNumber: String, certCode: String) {
        val req = SendSmsReq(
            destPhone = phoneNumber,
            msgBody = "[베리풀] 휴대폰 인증 [${certCode}]를 인증번호 창에 입력해주세요!",
            subject = "베리풀 핸드폰 인증 입니다."
        )
        apiSmsAlimSender.sendSms(req)
    }
}