package berryful.lounge.api.externalApi.firebase

import berryful.lounge.api.repository.crm.DeviceRepository
import berryful.lounge.api.utils.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class FcmClient(
    private val deviceRepository: DeviceRepository,
) {
    init {
        val serviceAccount = TypeReference::class.java.getResourceAsStream("")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()
        FirebaseApp.initializeApp(options)
    }

    @Async
    fun send(deviceId: String, title: String, body: String, badgeCount: Int, data: Map<String,String>? = null): String? {
        Log.out("FcmClient.send()", "$title $body $badgeCount")
        val androidConfig = AndroidConfig.builder()
            .setCollapseKey("berryful")
            .setTtl(1728000L)
            .setNotification(AndroidNotification.builder()
                .setNotificationCount(0) //앱이 백그라운드 상태일 때. 0으로 설정하면 자동으로 +1 해줌.
                .build())
            .putData("badge", badgeCount.toString())
            .build()
        val apnsConfig = ApnsConfig.builder()
            .setAps(Aps.builder()
                .setBadge(badgeCount)
                .setMutableContent(true)
                .setSound("critical")
                .setContentAvailable(true)
                .build())
            .putHeader("apns-priority", "5")
            .putHeader("apns-expiration", "1728000000")
            .build()

        val message = Message.builder()
            .setToken(deviceId)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .setAndroidConfig(androidConfig)
            .setApnsConfig(apnsConfig)

        if (data != null) {
            message.putAllData(data)
        }

        return try {
            FirebaseMessaging.getInstance()
                .send(message.build())
        } catch(e: FirebaseMessagingException) {
            Log.out("FcmClient.send()", "Fail to send a Firebase Cloud message to the device(id=$deviceId) about $title. ${e.message}\n ${e.messagingErrorCode} ${e.errorCode}")
            println(e.httpResponse.content)
            deviceRepository.deleteByNotificationToken(deviceId)
            return null
        }
    }

    @Async
    fun sendToMultipleDevices(deviceTokens: List<String>, title: String, body: String, badgeCount: Int, data: Map<String,String>? = null) {
        Log.out("FcmClient.sendToMultipleDevices()", "$title $body $badgeCount")
        val androidConfig = AndroidConfig.builder()
            .setCollapseKey("berryful")
            .setTtl(1728000L)
            .setNotification(AndroidNotification.builder()
                .setNotificationCount(0) //앱이 백그라운드 상태일 때. 0으로 설정하면 자동으로 +1 해줌.
                .build())
            .putData("badge", badgeCount.toString())
            .build()
        val apnsConfig = ApnsConfig.builder()
            .setAps(Aps.builder()
                .setBadge(badgeCount)
                .setMutableContent(true)
                .setSound("critical")
                .setContentAvailable(true)
                .build())
            .putHeader("apns-priority", "5")
            .putHeader("apns-expiration", "1728000000")
            .build()

        val messages = MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .setAndroidConfig(androidConfig)
            .setApnsConfig(apnsConfig)

        if (data != null) {
            messages.putAllData(data)
        }

        val batchResponse = FirebaseMessaging.getInstance()
            .sendMulticast(messages.build())
        if (batchResponse.failureCount > 0) {
            val responses = batchResponse.responses
            val failedTokens = mutableListOf<String>()
            responses.forEachIndexed { index, sendResponse ->
                if (!responses[index].isSuccessful) failedTokens.add(deviceTokens[index])
                Log.out("FcmClient.sendToMultipleDevices()", "token that caused failures: ${deviceTokens[index]} Exception: ${sendResponse.exception}")
            }
            Log.out("FcmClient.sendToMultipleDevices()", "List of tokens that caused failures: $failedTokens")
            deviceRepository.deleteAllByNotificationTokenIn(failedTokens)
        }
    }

    @Async
    fun sendResetBadge(deviceId: String, badgeCount: Int, data: Map<String,String>? = null): String? {
        Log.out("FcmClient.sendResetBadge()", "$badgeCount")
        val apnsConfig = ApnsConfig.builder()
            .setAps(Aps.builder()
                .setBadge(badgeCount)
                .build())
            .putHeader("apns-priority", "5")
            .build()

        val message = Message.builder()
            .setToken(deviceId)
            .setApnsConfig(apnsConfig)

        if (data != null) {
            message.putAllData(data)
        }

        return try {
            FirebaseMessaging.getInstance()
                .send(message.build())
        } catch(e: FirebaseMessagingException) {
            Log.out("FcmClient.sendResetBadge()", "Fail to send a Firebase Cloud message to the device(id=$deviceId). ${e.message}\n ${e.messagingErrorCode} ${e.errorCode}")
            println(e.httpResponse.content)
            return null
        }
    }

    @Async
    fun sendResetBadgeToMultipleDevices(deviceTokens: List<String>, badgeCount: Int, data: Map<String,String>? = null) {
        Log.out("FcmClient.sendResetBadgeToMultipleDevices()", "$badgeCount")
        val apnsConfig = ApnsConfig.builder()
            .setAps(Aps.builder()
                .setBadge(badgeCount)
                .build())
            .putHeader("apns-priority", "5")
            .build()

        val messages = MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setApnsConfig(apnsConfig)

        if (data != null) {
            messages.putAllData(data)
        }

        val batchResponse = FirebaseMessaging.getInstance()
            .sendMulticast(messages.build())
        if (batchResponse.failureCount > 0) {
            val responses = batchResponse.responses
            val failedTokens = mutableListOf<String>()
            responses.forEachIndexed { index, sendResponse ->
                if (!responses[index].isSuccessful) failedTokens.add(deviceTokens[index])
                Log.out("FcmClient.sendResetBadgeToMultipleDevices()", "token that caused failures: ${deviceTokens[index]} Exception: ${sendResponse.exception}")
            }
            Log.out("FcmClient.sendResetBadgeToMultipleDevices()", "List of tokens that caused failures: $failedTokens")
        }
    }
}