package berryful.lounge.api.repository.lounge.article

import berryful.lounge.api.entity.Article
import berryful.lounge.api.entity.ArticleStatus
import berryful.lounge.api.entity.ArticleType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import javax.persistence.LockModeType

interface ArticleRepository : JpaRepository<Article, Long>, ArticleDslRepository {
    fun findAllByArticleTypeAndStatusAndChallengeEndAtGreaterThanEqual(
        articleType: ArticleType,
        articleStatus: ArticleStatus,
        challengeEndAt: Instant
    ): List<Article>

    fun findAllByParentIdAndArticleType(parentId: Long, articleType: ArticleType): List<Article>

    fun findByIdAndArticleType(id: Long, articleType: ArticleType): Article?
    fun findByIdAndArticleTypeAndStatus(id: Long, articleType: ArticleType, articleStatus: ArticleStatus): Article?
    fun findByIdAndMemberIdAndArticleTypeAndStatus(id: Long, memberId: Long, articleType: ArticleType, articleStatus: ArticleStatus): Article?

    fun findAllByMemberId(memberId: Long): List<Article>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByIdAndStatus(id: Long, articleStatus: ArticleStatus): Article?

    fun findByAdoptClipId(adoptClipId: Long): Article?

    @Transactional
    @Modifying
    @Query(value = "UPDATE article a SET a.views = a.views+1 WHERE a.id = :id", nativeQuery = true)
    fun viewCountUp(@Param("id") id: Long)

    @Transactional
    @Modifying
    @Query(value = "update article set clip_encoding_check = 1 where id = :clipId", nativeQuery = true)
    fun clipEncodingCheck(@Param("clipId") clipId: Long)

    @Transactional
    @Modifying
    @Query(value = "update article set clip_timeline = :clipTimeline where clip_url like %:clipUrl%", nativeQuery = true)
    fun updateClipTimeline(@Param("clipUrl") clipUrl: String, @Param("clipTimeline") clipTimeline: Long)

    @Transactional
    @Modifying
    @Query(
        value = "DELETE clip, comment, reply\n" +
                "FROM article post LEFT JOIN article clip ON clip.id = post.parent_id AND clip.article_type = 'CLIP'\n" +
                                  "LEFT JOIN article comment ON clip.id = comment.parent_id AND comment.article_type = 'COMMENT'\n" +
                                  "LEFT JOIN article reply ON comment.id = reply.parent_id AND reply.article_type = 'REPLY'\n" +
                "WHERE post.id = :postId", nativeQuery = true
    )
    fun deleteAllClipAndCommentAndReplyByPostId(@Param("postId") postId: Long)

    @Transactional
    @Modifying
    @Query(
        value = "DELETE comment, reply\n" +
                "FROM article clip LEFT JOIN article comment ON clip.id = comment.parent_id AND comment.article_type = 'COMMENT'\n" +
                "LEFT JOIN article reply ON comment.id = reply.parent_id AND reply.article_type = 'REPLY'\n" +
                "WHERE clip.id = :clipId", nativeQuery = true
    )
    fun deleteAllCommentAndReplyByClipId(@Param("clipId") clipId: Long)

    @Transactional
    @Modifying
    @Query(
        value = "DELETE reply\n" +
                "FROM article comment LEFT JOIN article reply ON comment.id = reply.parent_id AND reply.article_type = 'REPLY'\n" +
                "WHERE comment.id = :commentId", nativeQuery = true
    )
    fun deleteAllReplyByCommentId(@Param("commentId") commentId: Long)
}