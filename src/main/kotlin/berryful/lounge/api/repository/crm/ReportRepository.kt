package berryful.lounge.api.repository.crm

import berryful.lounge.api.data.ReportRes
import berryful.lounge.api.entity.Report
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReportRepository : JpaRepository<Report, Long> {
    fun findByArticleIdAndMemberIdAndReportType(articleId: Long, memberId: Long, reportType: Int): Report?
    fun countByArticleId(articleId: Long): Int

    @Query(value = "SELECT DATE_FORMAT(r.create_at, '%Y-%m-%d %H:%i:%s') createAt" +
                   ",r.report_type reportType" +
                   ",m.nickname uploaderNickname" +
                   ",a.article_type articleType\n" +
                   "FROM report r, article a, member m\n" +
                   "WHERE r.member_id = :member_id AND " +
                         "r.article_id = a.id AND " +
                         "a.member_id = m.id AND " +
                         "(m.status <> 'LEAVE' AND m.status <> 'SUSPENDED')",
            countQuery = "SELECT count(*) FROM report r, article a, member m\n" +
                    "WHERE r.member_id = :member_id AND " +
                    "r.article_id = a.id AND " +
                    "a.member_id = m.id AND " +
                    "(m.status <> 'LEAVE' AND m.status <> 'SUSPENDED')",
            nativeQuery = true)
    fun findAllReportedList(pageable: Pageable, @Param("member_id")memberId: Long): Page<ReportRes>

    fun deleteAllByArticleId(articleId: Long): Int
}