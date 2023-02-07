package berryful.lounge.api.repository.lounge

import berryful.lounge.api.entity.ThumbsUp
import berryful.lounge.api.entity.ThumbsUpId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface ThumbsUpRepository : JpaRepository<ThumbsUp, ThumbsUpId> {
    fun findByArticleIdAndMemberId(articleId: Long, memberId: Long): ThumbsUp?
    fun findAllByMemberId(memberId: Long): List<ThumbsUp>

    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO thumbs_up (article_id, member_id) VALUES (:articleId, :memberId)", nativeQuery = true)
    fun insertThumbsUp(@Param("articleId")articleId: Long, @Param("memberId")memberId: Long): Int

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM thumbs_up WHERE article_id= :articleId AND member_id= :memberId", nativeQuery = true)
    fun deleteThumbsUp(@Param("articleId")articleId: Long, @Param("memberId")memberId: Long): Int
}