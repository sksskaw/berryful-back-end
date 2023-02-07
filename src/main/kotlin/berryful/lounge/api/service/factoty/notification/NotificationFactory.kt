package berryful.lounge.api.service.factoty.notification

import berryful.lounge.api.entity.Article

abstract class NotificationFactory {
    abstract fun createNotificationContent(article: Article?, parentArticle: Article?): String?
}