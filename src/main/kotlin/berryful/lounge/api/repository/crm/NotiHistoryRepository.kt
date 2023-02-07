package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.NotiHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface NotiHistoryRepository : JpaRepository<NotiHistory, Long> {

    fun findFirstByMemberIdAndArticleIdAndNotificationTypeOrderByIdDesc(memberId: Long, articleId: Long, notificationType: Int): NotiHistory?

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO noti_history (member_id, article_id, create_at, notification_type) VALUES (:memberId, :articleId, :createAt, :notificationType)", nativeQuery = true)
    fun insertNotiHistory(@Param("memberId") memberId: Long, @Param("articleId") articleId: Long?,
                          @Param("createAt") createAt: String, @Param("notificationType") notificationType: Int): Int
}