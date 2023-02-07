package berryful.lounge.api.controller.crm

import berryful.lounge.api.data.VueErrorLogReq
import berryful.lounge.api.service.VueErrorLogService
import berryful.lounge.api.utils.Log
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 *  vue 에러 로깅 api 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/crm/v2")
class VueErrorLogController(
    private val vueErrorLogService: VueErrorLogService
) {
    /**
     * vue 에러 로깅 API 입니다.
     * @param vueErrorLogReq 에러 정보 입니다.
     */
    @PostMapping("/vue/error/loging")
    fun vueErrorLoging(@RequestBody vueErrorLogReq: VueErrorLogReq) {
        Log.out("VueErrorLogController.vueErrorLoging()", "$vueErrorLogReq")
        vueErrorLogService.vueErrorLoging(vueErrorLogReq)
    }
}