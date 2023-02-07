package berryful.lounge.api.entity

import javax.persistence.*

/**
 *  회원에게 전송된 알림 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "notification")
@Entity
class Notification(

    @Column(name="member_id")
    var memberId: Long,

    @Column(name="from_member_id")
    var fromMemberId: Long,

    @Column(name="title")
    var title: String? = null,

    @Column(name="content")
    var content: String? = null,

    @Column(name="article_id")
    var articleId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name="article_type")
    var articleType: ArticleType? = null,

    // 1: 좋아요 알림, 2: 컨텐츠 업로드, 3: 클립 채택, 4: 관심 pick, 5: 팔로우
    @Column(name="notification_type")
    var notificationType: Int? = null,

    @Column(name="post_id")
    var postId: Long? = null,

    @Column(name="clip_id")
    var clipId: Long? = null,

    @Column(name="comment_id")
    var commentId: Long? = null,

    @Column(name="read_check")
    var readCheck: Int = 0,

) : BaseEntity()