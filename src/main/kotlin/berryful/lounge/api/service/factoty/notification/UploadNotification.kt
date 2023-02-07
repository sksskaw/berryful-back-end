package berryful.lounge.api.service.factoty.notification

import berryful.lounge.api.entity.Article

class UploadNotification : NotificationFactory() {
    override fun createNotificationContent(article: Article?, parentArticle: Article?): String? {
        return when (article?.articleType.toString()) {
            "CLIP" -> "님이 내 포스트" + "\"${parentArticle?.title}\"" + "에 클립을 올렸어요."
            "COMMENT" -> "님이 내 클립에 댓글을 남겼어요:" + "${article?.content}"
            "REPLY" -> "님이 내 댓글에 답글을 남겼어요:" + "${article?.content}"
            else -> null
        }
    }
}