package berryful.lounge.api.controller.crm

import berryful.lounge.api.service.crm.NotificationService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  알림 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/crm/v2")
class NotificationController(
    private val notificationService: NotificationService,
) {
    /**
     * 알림 목록 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/notificationList")
    fun getNotificationList(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("NotificationController.getNotificationList", " Param-> page= $page size= $size")
        val sort = Sort.by(Sort.Direction.DESC, "n.create_at")
        return ResponseEntity
            .ok()
            .body(notificationService.getNotificationList(PageRequest.of(page, size, sort), memberId))
    }

    /**
     * 알림 전체 삭제 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @DeleteMapping("all/notification")
    fun deleteAllNotification(request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("NotificationController.deleteAllNotification", "memberId= $memberId Delete all notifications")
        return ResponseEntity
            .ok()
            .body(notificationService.deleteAllNotification(memberId))
    }

    /**
     * 알림 삭제 API 입니다.
     * @param id 삭제할 알림의 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @DeleteMapping("/notification/{id}")
    fun deleteNotification(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("NotificationController.deleteNotification", "memberId= $memberId notificationId =$id")
        return ResponseEntity
            .ok()
            .body(notificationService.deleteNotification(id, memberId))
    }
}