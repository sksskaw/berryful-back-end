package berryful.lounge.api.data

import berryful.lounge.api.entity.ArticleType

data class NotificationListRes(
    var rowCount: Long? = 0,
    var notifications: Any? = null,
)

interface NotificationRes {
    var follow: Int?
    var id: Long?
    var createAt: String
    var memberId: Long?
    var fromNickname: String
    var fromMemberId: Long?
    var content: String?
    var clipUrl: String?
    var articleId: Long?
    var articleType: ArticleType?
    var notificationType: Int?
    var postId: Long?
    var clipId: Long?
    var commentId: Long?
    var postWriterId: Long?
    var adoptClipId: Long?
    var readCheck: Int?
}

data class ClipTypeNotificationRes(
    var postId: Long? = 0,
    var clips: Any? = null,
)

data class CommentTypeNotificationRes(
    var postId: Long? = 0,
    var clipId: Long? = 0,
)

data class ReplyTypeNotificationRes(
    var postId: Long? = 0,
    var clipId: Long? = 0,
    var commentId: Long? = 0,
)