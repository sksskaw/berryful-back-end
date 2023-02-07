package berryful.lounge.api.service.crm

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.*
import berryful.lounge.api.externalApi.firebase.Notifier
import berryful.lounge.api.repository.crm.*
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.service.factoty.notification.*
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.HashMap

@Service
class NotificationService(
    private val commonService: CommonService,
    private val notificationRepository: NotificationRepository,
    private val notiHistoryRepository: NotiHistoryRepository,
    private val articleRepository: ArticleRepository,
    private val memberRepository: MemberRepository,
    private val deviceRepository: DeviceRepository,
    private val notifier: Notifier,
    private var notificationFactory: NotificationFactory,
) {
    fun createNotification(causeMemberId: Long, causeMemberNickname: String, receiveMemberId: Long, articleId: Long, notificationType: Int): Any {
        if (causeMemberId == receiveMemberId)
            return ApiResultCode(ErrorMessageCode.OK.code)

        if (commonService.checkBlockedMember(receiveMemberId, causeMemberId))
            return ApiResultCode(ErrorMessageCode.OK.code)

        if (checkAndCreateNotiHistory(causeMemberId, articleId, notificationType)) {
            return ApiResultCode(ErrorMessageCode.OK.code)
        }

        val article = articleRepository.findByIdAndStatus(articleId, ArticleStatus.UNBLOCKED)
        val parentArticle =
            if (article != null) articleRepository.findByIdAndStatus(article.parentId ?: 0, ArticleStatus.UNBLOCKED) else null

        notificationFactory = when (notificationType) {
            1 -> LikeNotification()
            2 -> UploadNotification()
            3 -> AdoptClipNotification()
            4 -> PickPostNotification()
            5 -> FollowNotification()
            else -> return ApiResultCode(ErrorMessageCode.NOTIFICATION_TYPE_MISMATCH.code)
        }
        val notificationContent = notificationFactory.createNotificationContent(article, parentArticle)

        insertNotification(receiveMemberId, causeMemberId, notificationType, article, notificationContent)

        if (memberPushSettingCheck(receiveMemberId, notificationType))
            selectDeviceAndSendPush(receiveMemberId, causeMemberNickname + notificationContent)

        setNotificationCheckFalse(receiveMemberId)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun insertNotification(receiveMemberId: Long, causeMemberId: Long, notificationType: Int, article: Article?, content: String?) {
        val articleParentIds = if (article != null) getArticleParentIds(article) else null
        val newNotification = Notification(
            memberId = receiveMemberId,
            fromMemberId = causeMemberId,
            notificationType = notificationType,
            content = content,
            articleId = article?.id,
            articleType = article?.articleType,
            postId = articleParentIds?.get("postId"),
            clipId = articleParentIds?.get("clipId"),
            commentId = articleParentIds?.get("commentId"),
        )
        notificationRepository.save(newNotification)
    }

    fun getArticleParentIds(article: Article): MutableMap<String, Long?> {
        var post: Article? = null
        var clip: Article? = null
        var comment: Article? = null

        when (article.articleType.toString()) {
            "POST" -> {
                post = article
            }
            "CLIP" -> {
                clip = article
                post = articleRepository.findByIdAndArticleTypeAndStatus(article.parentId!!, ArticleType.POST, ArticleStatus.UNBLOCKED)
            }
            "COMMENT" -> {
                clip = articleRepository.findByIdAndArticleTypeAndStatus(article.parentId!!, ArticleType.CLIP, ArticleStatus.UNBLOCKED)
                if (clip != null)
                    post = articleRepository.findByIdAndArticleTypeAndStatus(clip.parentId!!, ArticleType.POST, ArticleStatus.UNBLOCKED)
            }
            "REPLY" -> {
                comment = articleRepository.findByIdAndArticleTypeAndStatus(article.parentId!!, ArticleType.COMMENT, ArticleStatus.UNBLOCKED)
                if (comment != null) {
                    clip = articleRepository.findByIdAndArticleTypeAndStatus(comment.parentId!!, ArticleType.CLIP, ArticleStatus.UNBLOCKED)
                    if (clip != null)
                        post = articleRepository.findByIdAndArticleTypeAndStatus(clip.parentId!!, ArticleType.POST, ArticleStatus.UNBLOCKED)
                }
            }
        }

        val articleParentIds: MutableMap<String, Long?> = HashMap()
        articleParentIds["postId"] = post?.id
        articleParentIds["clipId"] = clip?.id
        articleParentIds["commentId"] = comment?.id
        return articleParentIds
    }

    fun checkAndCreateNotiHistory(causeMemberId: Long, newArticleId: Long, notificationType: Int): Boolean {
        val notiInterval =
            when (notificationType) {
                1 -> 60L
                2 -> 1L
                4 -> 60L
                5 -> 60L
                else -> 0L
            }

        val notiHistory = notiHistoryRepository.findFirstByMemberIdAndArticleIdAndNotificationTypeOrderByIdDesc(
            causeMemberId,
            newArticleId,
            notificationType
        )
        if (notiHistory != null && (Instant.now().minus(notiInterval, ChronoUnit.MINUTES) < notiHistory.createAt)) {
            return true
        }

        val newNotiHistory = NotiHistory(causeMemberId, newArticleId, notificationType)
        notiHistoryRepository.save(newNotiHistory)
        return false
    }

    @Transactional
    fun getNotificationList(pageable: Pageable, memberId: Long): Any {
        memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        memberRepository.notificationCheck(memberId)

        val notificationPage = notificationRepository.findAllByMemberIdAndFromMemberIdNotIn(pageable, memberId)
        notificationRepository.updateNotificationReadChek(memberId)

        commonService.findDeviceTokenAndPush(memberId)
        return NotificationListRes(
            rowCount = notificationPage.totalElements,
            notifications = notificationPage.toList(),
        )
    }

    fun deleteAllNotification(memberId: Long): Any {
        val notificationList = notificationRepository.findAllByMemberId(memberId)
        notificationRepository.deleteAll(notificationList)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun deleteNotification(id: Long, memberId: Long): Any {
        notificationRepository.deleteById(id)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun setNotificationCheckFalse(receiveMemberId: Long) {
        val receiveMember = memberRepository.findByIdOrNull(receiveMemberId)
        if (receiveMember != null) {
            receiveMember.notificationCheck = false
            memberRepository.save(receiveMember)
        }
    }

    fun selectDeviceAndSendPush(receiveMemberId: Long, body: String) {
        val devices = deviceRepository.findAllByMemberId(receiveMemberId)
        val badgeCount = notificationRepository.selectBadgeCount(receiveMemberId)

        if (devices.isNotEmpty()) {
            val deviceTokens: List<String> = devices.map { it.notificationToken }
            notifier.sendPush(deviceTokens, "", body, badgeCount)
        }
    }

    fun memberPushSettingCheck(receiveMemberId: Long, notificationType: Int): Boolean {
        val member = memberRepository.findByIdOrNull(receiveMemberId)
            ?: return false

        when (notificationType) {
            1,2,3,4 -> return member.pushThumbsUp && member.pushContentUpload && member.pushAdoptClip && member.pushInterestedArticle
            5 -> return member.pushFollow
            6 -> return member.pushBerryful
        }

        return false
    }
}