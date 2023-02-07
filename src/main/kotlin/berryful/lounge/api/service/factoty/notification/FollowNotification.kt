package berryful.lounge.api.service.factoty.notification

import berryful.lounge.api.entity.Article

class FollowNotification : NotificationFactory() {
    override fun createNotificationContent(article: Article?, parentArticle: Article?): String? {
        return "님이 나를 팔로우하기 시작했어요"
    }
}