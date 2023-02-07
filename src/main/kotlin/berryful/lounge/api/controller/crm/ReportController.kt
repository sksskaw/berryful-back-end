package berryful.lounge.api.controller.crm

import berryful.lounge.api.data.ReportReq
import berryful.lounge.api.service.crm.ReportService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  신고 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/crm/v2")
class ReportController(
    private val reportService: ReportService,
) {

    @PostMapping("/report")
    fun createReport(@RequestBody reportReq: ReportReq, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ReportController.createReport", " Param-> memberId= $memberId reportReq= $reportReq")

        return ResponseEntity
            .ok()
            .body(reportService.createReport(memberId, reportReq))
    }

    @GetMapping("/member/reportedList")
    fun getReportedList(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ReportController.getReportedList", " Param-> memberId= $memberId")

        val sort = Sort.by(Sort.Direction.DESC, "create_at")
        return ResponseEntity
            .ok()
            .body(reportService.getReportedList(PageRequest.of(page, size, sort), memberId))
    }
}