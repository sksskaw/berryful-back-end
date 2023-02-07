package berryful.lounge.api.service.factoty.notification

import berryful.lounge.api.entity.Article

class LikeNotification : NotificationFactory() {
    override fun createNotificationContent(article: Article?, parentArticle: Article?): String? {
        return when (article?.articleType.toString()) {
            "POST" -> "님이 내 포스트를 좋아해요:" + "\"${article?.title}\""
            "CLIP" -> "님이 내 클립를 좋아해요:" + "\"${parentArticle?.title}\""
            "COMMENT" -> "님이 내 댓글을 좋아해요:" + "\"${article?.content}\""
            "REPLY" -> "님이 내 답글을 좋아해요:" + "\"${article?.content}\""
            else -> null
        }
    }
}