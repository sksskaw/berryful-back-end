package berryful.lounge.api.service.crm

import berryful.lounge.api.data.CheckUpdateVersionReq
import berryful.lounge.api.data.CheckUpdateVersionRes
import berryful.lounge.api.data.DeviceReq
import berryful.lounge.api.entity.*
import berryful.lounge.api.externalApi.firebase.Notifier
import berryful.lounge.api.repository.crm.*
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import berryful.lounge.api.utils.Log
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val notificationRepository: NotificationRepository,
    private val memberAccessHistoryRepository: MemberAccessHistoryRepository,
    private val appVersionRepository: AppVersionRepository,
    private val notifier: Notifier,
    private val commonService: CommonService,
) {
    @Transactional
    fun createDevice(req: DeviceReq): Any {
        if (req.notificationToken == "")
            return ApiResultCode(ErrorMessageCode.OK.code)

        val device = deviceRepository.findByNotificationToken(req.notificationToken)
        if (device == null) {
            val newDevice = Device(
                memberId = req.memberId,
                notificationToken = req.notificationToken,
                platformType = null,
                platformName = null,
                platformVersion = null,
                deviceInfo = null,
                deviceName = null,
                appVersion = null,
            )
            deviceRepository.save(newDevice)
        } else {
            device.memberId = req.memberId
            deviceRepository.save(device)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun deleteDevice(req: DeviceReq): Any {
        val device = deviceRepository.findByMemberIdAndNotificationToken(req.memberId, req.notificationToken)
        if (device != null) {
            device.memberId = 0L
            deviceRepository.save(device)
        }

        if(req.memberId != 0L){
            memberAccessHistoryRepository.insertLogoutMemberAccessHistory(req.memberId)
        }

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun pushTest(deviceReq: DeviceReq): Any {
        val badgeCount = notificationRepository.selectBadgeCount(deviceReq.memberId) + 1

        Log.out("pushTest", "badgeCount: $badgeCount")
        val devices = deviceRepository.findAllByMemberId(deviceReq.memberId)
        if (devices.isNotEmpty()) {
            val deviceTokens: List<String> = devices.map { it.notificationToken }
            notifier.sendPush(deviceTokens, "Berryful 푸시 테스트 Title", "푸시 테스트 Body", badgeCount)
        }

        val newNotification = Notification(
            memberId = deviceReq.memberId,
            fromMemberId = 0,
            title = "Berryful 푸시 테스트 Title",
            content = "푸시 테스트 Body",
            notificationType = 99,
            articleId = 0,
            articleType = ArticleType.POST,
            postId = 0,
            clipId = 0,
            commentId = 0,
        )

        notificationRepository.save(newNotification)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun pushResetTest(deviceReq: DeviceReq): Any {
        val devices = deviceRepository.findAllByMemberId(deviceReq.memberId)
        if (devices.isNotEmpty()) {
            val deviceTokens: List<String> = devices.map { it.notificationToken }
            notifier.sendResetBadge(deviceTokens, 0)
        }

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun appOpen(req: CheckUpdateVersionReq): Any {
        if(req.memberId != 0L){
            memberAccessHistoryRepository.insertAppOpenMemberAccessHistory(req.memberId)
        }

        val forceUpdateVersion =
            appVersionRepository.findFirstByForceUpdateAndPlatformTypeOrderByCreateAtDesc(true, req.platformType)
                ?: return ApiResultCode(ErrorMessageCode.BAD_REQUEST.code)
        val updateVersion =
            appVersionRepository.findFirstByForceUpdateAndPlatformTypeOrderByCreateAtDesc(false, req.platformType)
                ?: return ApiResultCode(ErrorMessageCode.BAD_REQUEST.code)

        val forceUpdate = commonService.compareVersion(req.currentVersion, forceUpdateVersion.version)
        val updateCheck = commonService.compareVersion(req.currentVersion, updateVersion.version)

        return CheckUpdateVersionRes(
            ErrorMessageCode.OK.code,
            forceUpdate,
            updateCheck
        )
    }

    @Transactional(readOnly = true)
    fun checkUpdateVersion(req: CheckUpdateVersionReq) : Any {
        val forceUpdateVersion =
            appVersionRepository.findFirstByForceUpdateAndPlatformTypeOrderByCreateAtDesc(true,req.platformType)
                ?: return ApiResultCode(ErrorMessageCode.BAD_REQUEST.code)
        val updateVersion =
            appVersionRepository.findFirstByForceUpdateAndPlatformTypeOrderByCreateAtDesc(false,req.platformType)
                ?: return ApiResultCode(ErrorMessageCode.BAD_REQUEST.code)

        val forceUpdate = commonService.compareVersion(req.currentVersion, forceUpdateVersion.version)
        val updateCheck = commonService.compareVersion(req.currentVersion, updateVersion.version)

        return CheckUpdateVersionRes(
            ErrorMessageCode.OK.code,
            forceUpdate,
            updateCheck
        )
    }
}