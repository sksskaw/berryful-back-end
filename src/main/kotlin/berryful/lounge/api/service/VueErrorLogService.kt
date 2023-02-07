package berryful.lounge.api.service

import berryful.lounge.api.data.VueErrorLogReq
import berryful.lounge.api.entity.VueErrorLog
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.crm.VueErrorLogRepository
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class VueErrorLogService(
    private val vueErrorLogRepository: VueErrorLogRepository,
    private val memberRepository: MemberRepository,
) {
    fun vueErrorLoging(req: VueErrorLogReq) {
        vueErrorLogRepository.save(
            VueErrorLog(
                memberId = req.memberId,
                module = req.module,
                errorCode = req.errorCode,
                errorMsg = req.errorMsg,
            )
        )
    }
}