package berryful.lounge.api.service.crm

import berryful.lounge.api.data.PageableSetRes
import berryful.lounge.api.data.ReportReq
import berryful.lounge.api.entity.ArticleStatus
import berryful.lounge.api.entity.Report
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.crm.ReportRepository
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportService(
    private val commonService: CommonService,
    private val reportRepository: ReportRepository,
    private val articleRepository: ArticleRepository,
    private val memberRepository: MemberRepository,
) {
    @Transactional
    fun createReport(memberId: Long, req: ReportReq): Any {
        if (reportRepository.findByArticleIdAndMemberIdAndReportType(req.articleId, memberId, req.reportType) != null)
            return ApiResultCode(ErrorMessageCode.OK.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        req.reportDescription = commonService.checkForbiddenWordAndConvert(req.reportDescription!!)

        val reportCount = reportRepository.countByArticleId(req.articleId)
        if (reportCount < 3) {
            val newReport = Report(
                articleId = req.articleId,
                memberId = memberId,
                reportType = req.reportType,
                reportDescription = req.reportDescription,
            )
            reportRepository.save(newReport)
        }

        if (reportCount >= 2) {
            val article = articleRepository.findByIdAndStatus(req.articleId, ArticleStatus.UNBLOCKED)
            if (article != null) {
                article.status = ArticleStatus.BLOCKED
                articleRepository.save(article)
            }
            return ApiResultCode(ErrorMessageCode.OK.code)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getReportedList(pageable: Pageable, memberId: Long) : Any {
        val reportPage = reportRepository.findAllReportedList(pageable ,memberId)

        return PageableSetRes(
            rowCount = reportPage.totalElements.toInt(),
            rows = reportPage.toList(),
        )
    }
}