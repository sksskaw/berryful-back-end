package berryful.lounge.api.repository.crm

import berryful.lounge.api.data.NotificationRes
import berryful.lounge.api.entity.Notification
import org.hibernate.annotations.Formula
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Basic
import javax.persistence.FetchType

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findAllByMemberId(pageable: Pageable, memberId: Long): Page<Notification>

    fun findAllByMemberId(memberId: Long): List<Notification>
    fun deleteAllByMemberIdOrFromMemberId(memberId: Long, fromMemberId: Long): Int

    @Query(
        value = "SELECT (select count(1) from member_follow mf where mf.follower_member_id = :memberId AND mf.following_member_id = n.from_member_id) as follow" +
                ",n.id\n" +
                ",DATE_FORMAT(n.create_at, '%Y-%m-%d %H:%i:%s') createAt\n" +
                ",n.member_id memberId\n" +
                ",n.content\n" +
                ",n.article_id articleId\n" +
                ",n.article_type articleType\n" +
                ",n.notification_type notificationType\n" +
                ",n.post_id postId\n" +
                ",n.clip_id clipId\n" +
                ",n.comment_id commentId\n" +
                ",n.from_member_id fromMemberId\n" +
                ",(SELECT nickname FROM member WHERE id = n.from_member_id) as fromNickname\n" +
                ",n.read_check readCheck\n" +
                ",CASE \n" +
                "  WHEN(n.article_type = 'POST') THEN a.member_id\n" +
                "  WHEN(n.article_type = 'CLIP') THEN (SELECT member_id FROM article WHERE id = a.parent_id)\n" +
                "  ELSE NULL END AS postWriterId\n" +
                ",a.adopt_clip_id adoptClipId\n" +
                ",IF(n.article_type = 'CLIP', a.clip_url, NULL) AS clipUrl\n" +
                "FROM `member` m INNER JOIN notification n ON m.id = n.member_id\n" +
                "LEFT JOIN article a ON n.article_id = a.id\n" +
                "WHERE n.member_id = :memberId AND (a.status <> 'BLOCKED' OR ISNULL(n.article_id)) AND\n" +
                "n.from_member_id NOT IN (SELECT blocked_member_id FROM blocked_member WHERE member_id = m.id)\n",
        countQuery = "SELECT count(*) " +
                "FROM `member` m INNER JOIN notification n ON m.id = n.member_id\n" +
                "LEFT JOIN article a ON n.article_id = a.id\n" +
                "WHERE n.member_id = :memberId AND (a.status <> 'BLOCKED' OR ISNULL(n.article_id)) AND\n" +
                "n.from_member_id NOT IN (SELECT blocked_member_id FROM blocked_member WHERE member_id = m.id)",
        nativeQuery = true
    )
    fun findAllByMemberIdAndFromMemberIdNotIn(pageable: Pageable, @Param("memberId") memberId: Long): Page<NotificationRes>

    @Transactional
    @Modifying
    @Query(value = "UPDATE notification n SET n.read_check = 1 WHERE n.member_id = :memberId", nativeQuery = true)
    fun updateNotificationReadChek(@Param("memberId") memberId: Long)

    @Query(value = "select count(1) from notification n where n.member_id = :memberId AND n.read_check = 0", nativeQuery = true)
    fun selectBadgeCount(@Param("memberId") memberId: Long): Int
}