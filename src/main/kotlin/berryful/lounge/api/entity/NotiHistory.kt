package berryful.lounge.api.entity

import javax.persistence.*

@Table(name = "noti_history")
@Entity
class NotiHistory(

    @Column(name = "member_id")
    var memberId: Long,

    @Column(name = "article_id")
    var articleId: Long,

    // 1: 좋아요, 2: 업로드
    @Column(name = "notification_type")
    var notificationType: Int,
): BaseEntity()