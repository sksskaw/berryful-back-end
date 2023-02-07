package berryful.lounge.api.service.factoty.notification

import berryful.lounge.api.entity.Article
import org.springframework.stereotype.Component

@Component
class AdoptClipNotification : NotificationFactory() {
    override fun createNotificationContent(article: Article?, parentArticle: Article?): String? {
        return when (article?.articleType.toString()) {
            "CLIP" -> "님이 내 클립을 채택했어요:" + "\"${parentArticle?.title}\""
            else -> null
        }
    }
}