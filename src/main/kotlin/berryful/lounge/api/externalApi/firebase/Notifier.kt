package berryful.lounge.api.externalApi.firebase

import org.springframework.stereotype.Component

@Component
class Notifier(
    private val fcmClient: FcmClient,
) {
    fun sendPush(deviceTokens: List<String>, title: String, body: String, badgeCount: Int) {
        if (deviceTokens.size == 1) {
            fcmClient.send(deviceTokens[0], title, body, badgeCount)
        } else {
            fcmClient.sendToMultipleDevices(deviceTokens, title, body, badgeCount)
        }
    }

    fun sendResetBadge(deviceTokens: List<String>, badgeCount: Int) {
        if (deviceTokens.size == 1) {
            fcmClient.sendResetBadge(deviceTokens[0], badgeCount)
        } else {
            fcmClient.sendResetBadgeToMultipleDevices(deviceTokens, badgeCount)
        }
    }
}