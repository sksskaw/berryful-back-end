package berryful.lounge.api.controller.crm

import berryful.lounge.api.data.CheckUpdateVersionReq
import berryful.lounge.api.data.DeviceReq
import berryful.lounge.api.service.crm.DeviceService
import berryful.lounge.api.utils.Log
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  회원 디바이스 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/crm/v2")
class DeviceController(
    private val deviceService: DeviceService,
) {
    /**
     * 회원 디바이스 생성 API 입니다.
     * @param deviceReq 요청된 회원 id클립 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PostMapping("/device")
    fun createDevice(@RequestBody deviceReq: DeviceReq, request: HttpServletRequest): ResponseEntity<Any> {
        Log.out("DeviceController.createDevice", "$deviceReq")
        return ResponseEntity
            .ok()
            .body(deviceService.createDevice(deviceReq))
    }

    /**
     * 회원 디바이스 삭제 API 입니다.
     * @param deviceReq 요청된 회원 id클립 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @DeleteMapping("/device")
    fun deleteDevice(@RequestBody deviceReq: DeviceReq, request: HttpServletRequest): ResponseEntity<Any> {
        Log.out("DeviceController.deleteDevice", "$deviceReq")
        return ResponseEntity
            .ok()
            .body(deviceService.deleteDevice(deviceReq))
    }

    /**
     * 푸시 전송 테스트 API 입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PostMapping("/push/test")
    fun pushTest(@RequestBody deviceReq: DeviceReq): ResponseEntity<Any> {
        Log.out("DeviceController.pushTest", "$deviceReq")
        return ResponseEntity
            .ok()
            .body(deviceService.pushTest(deviceReq))
    }

    /**
     * 푸시 전송 테스트 API 입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PostMapping("/push/reset/test")
    fun pushResetTest(@RequestBody deviceReq: DeviceReq): ResponseEntity<Any> {
        Log.out("DeviceController.pushResetTest", "$deviceReq")
        return ResponseEntity
            .ok()
            .body(deviceService.pushResetTest(deviceReq))
    }

    /**
     * 업데이트 체크, 앱 오픈 로깅 API 입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PostMapping("/member/app/open")
    fun appOpen(@RequestBody checkUpdateVersionReq: CheckUpdateVersionReq): ResponseEntity<Any> {
        Log.out("DeviceController.appOpen", "$checkUpdateVersionReq")
        return ResponseEntity
            .ok()
            .body(deviceService.appOpen(checkUpdateVersionReq))
    }

    /**
     * 업데이트 체크 API 입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PostMapping("/checkVersion")
    fun checkVersion(@RequestBody checkUpdateVersionReq: CheckUpdateVersionReq): ResponseEntity<Any> {
        Log.out("DeviceController.checkVersion", "$checkUpdateVersionReq")
        return ResponseEntity
            .ok()
            .body(deviceService.checkUpdateVersion(checkUpdateVersionReq))
    }
}