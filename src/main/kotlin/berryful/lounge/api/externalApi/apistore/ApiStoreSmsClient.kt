package berryful.lounge.api.externalApi.apistore

import berryful.lounge.api.utils.Log
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * 참고 문서 : https://www.apistore.co.kr/api/apiViewPrice.do?service_seq=151
 */
@Component
class ApiStoreSmsClient @Autowired constructor(
    private val props: ApiStoreAttributes,
    private val restTemplate: RestTemplate
) {
    /**
     * 알림문자 발송
     */
    fun sendSms(req: SendSmsReq): SendSmsRes? {
        return try {
            val headers = getHttpHeaders()
            headers["x-waple-authorization"] = props.apiKey
            headers["Content-Type"] = "application/x-www-form-urlencoded; charset=UTF-8"
            val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
            parameters.add("dest_phone", req.destPhone)
            parameters.add("msg_body", req.msgBody)
            parameters.add("send_phone", props.smsSendPhone)
            req.subject?.also { parameters.add("subject", it) }
            req.destName?.also { parameters.add("dest_name", it) }
            req.sendTime?.also { parameters.add("send_time", it) }
            req.sendName?.also { parameters.add("send_name", it) }

            val entity = HttpEntity(parameters, headers)
            Log.out("ApiStoreClient.sendSms() props.appId : ", props.appId)
            //Log.out("ApiStoreClient.sendSms() props.apiKey : ", "${props.apiKey}")

            val url = props.apiEndpoint + "ppurio/1/message/sms/${props.appId}"

            restTemplate.postForEntity(url, entity, SendSmsRes::class.java).body!!
        } catch (error: Exception) {
            Log.out("ApiStoreClient.sendSms().error", "$error")
            null
        }
    }

    private fun getHttpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }
    }
}

data class SendSmsReq(
    /** 발송시간(없을 경우 즉시 발송) */
    var sendTime: String? = null,
    /** 수신자 이름 */
    var destName: String? = null,
    /** 수신할 핸드폰 번호 */
    var destPhone: String,

    /** 발신자 이름 */
    var sendName: String? = null,
    /** 발신자 전화번호 */
    var sendPhone: String? = null,
    /** 메시지 제목 */
    var subject: String? = null,
    /** 전송할 메세지 */
    var msgBody: String,
)

data class SendSmsRes(
    @JsonProperty("result_code")
    var resultCode: String = "",
    @JsonProperty("result_message")
    var resultMessage: String = "",
    var cmid: String = "",
)