package berryful.lounge.api.service.factoty.notification

import berryful.lounge.api.entity.Article

class PickPostNotification : NotificationFactory() {
    override fun createNotificationContent(article: Article?, parentArticle: Article?): String? {
        return when (article?.articleType.toString()) {
            "POST" -> "님이 내 포스트를 관심 포스트로 등록했어요:" + "\"${article?.title}\""
            "CLIP" -> "님이 내 관심 포스트" + "\"${parentArticle?.title}\"" + "에 클립을 올렸어요."
            else -> null
        }
    }
}