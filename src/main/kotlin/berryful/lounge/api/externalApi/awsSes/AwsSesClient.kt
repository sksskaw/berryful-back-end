package berryful.lounge.api.externalApi.awsSes

import berryful.lounge.api.entity.EmailCertNumber
import berryful.lounge.api.utils.Log
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.*
import org.springframework.stereotype.Component

@Component
class AwsSesClient {
    fun sendMailCertNumber(emailCertNumber: EmailCertNumber) {
        Log.out("MailSendService.sendMailCertNumber()", "email = ${emailCertNumber.email}")
        val from = "dev.beautalk@gmail.com"
        val to = emailCertNumber.email
        val subject = "[베리풀] 이메일 인증 번호 안내"

        val htmlBody = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "</head>\n" +
                "<body style=\"width: 390px;\n" +
                "  height: 844px;\n" +
                "  margin: 0 auto;\">\n" +
                "<br><br><br>\n" +
                "<div style=\"margin-left:30px;\n" +
                "  margin-right:30px;\n" +
                "  \n" +
                "  font-style: normal;\n" +
                "  font-weight: 500;\n" +
                "  font-size: 18px;\n" +
                "  line-height: 25px;\n" +
                "  letter-spacing: -0.02em;\">[베리풀] 이메일 인증 번호 안내</div>\n" +
                "<hr style=\"margin-left: 30px; width: 330px; border: 1px solid #F2F2F2;\">\n" +
                "<br><br>\n" +
                "<div style=\"margin-left: 132px;\">\n" +
                "<img height=\"80\" width=\"126.2\" src='https://berryful-mail-static.s3.ap-northeast-2.amazonaws.com/mail_logo.png'>\n" +
                "</div>\n" +
                "<br><br>\n" +
                "<div style=\"width: 270px;\n" +
                "  height: 40px;\n" +
                "  margin: 0 auto;\n" +
                "  margin-left: 60px;\n" +
                "  background-color: #EB0B45;\n" +
                "  display: flex;\n" +
                "  justify-content: center;\n" +
                "  align-items: center;\n" +
                "  border-radius: 20px;\n" +
                "  \n" +
                "  font-style: normal;\n" +
                "  font-weight: 600;\n" +
                "  font-size: 16px;\n" +
                "  letter-spacing: -0.02em;\n" +
                "  color: #FFFFFF;\">\n" +
                "<p>인증번호 : ${emailCertNumber.certNumber}</p>\n" +
                "</div>\n" +
                "<br>\n" +
                "<p style=\"width: 330px;\n" +
                "  height: 48px;\n" +
                "  margin-left:30px;\n" +
                "  margin-right:30px;\n" +
                "  \n" +
                "  font-style: normal;\n" +
                "  font-weight: 500;\n" +
                "  font-size: 14px;\n" +
                "  line-height: 24px;\n" +
                "  letter-spacing: -0.02em;\n" +
                "  color: #000000;\">상단의 인증번호를 입력창에 입력하시면 인증절차가 완료되어 가입을 진행하실 수 있습니다.</p>\n" +
                "<br><hr style=\"margin-left: 30px; width: 330px; border: 1px solid #F2F2F2;\"><br>\n" +
                "<p style=\"width: 330px;\n" +
                "  height: 133px;\n" +
                "  margin-left:35px;\n" +
                "  margin-right:25px;\n" +
                "  \n" +
                "  font-style: normal;\n" +
                "  font-weight: 400;\n" +
                "  font-size: 12px;\n" +
                "  line-height: 19px;\n" +
                "  letter-spacing: -0.02em;\n" +
                "  color: #8F8F8F;\">\n" +
                "(주)버즈비터즈<br>\n" +
                "서울시 용산구 한강대로 366 트윈시티남산 패스트파이브 7층 701호<br>\n" +
                "전화:02-6093-1066 | 이메일:admin@c-lnk.com<br>\n" +
                "사업자등록번호:869-81-01066<br>\n" +
                "통신판매업신고번호:2018-서울마포-1497호<br>\n" +
                "<br>\n" +
                "<span style=\"color: red;\">※ 본 메일은 이메일 인증을 위한 발신 전용 메일입니다.</span>\n" +
                "</p>\n" +
                "</body>\n" +
                "</html>\n"

        val client = AmazonSimpleEmailServiceClientBuilder.standard()
            .withRegion(Regions.AP_NORTHEAST_2).build()
        val request = SendEmailRequest()
            .withDestination(
                Destination().withToAddresses(to)
            )
            .withMessage(
                Message()
                    .withBody(
                        Body().withHtml(Content().withCharset("UTF-8").withData(htmlBody))
                    )
                    .withSubject(
                        Content().withCharset("UTF-8").withData(subject)
                    )
            )
            .withSource(from)
        client.sendEmail(request)
        Log.out("MailSendService.sendMailCertNumber", "Email sent!")
    }
}