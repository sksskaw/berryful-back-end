package berryful.lounge.api.service

import berryful.lounge.api.data.ClipRes
import berryful.lounge.api.data.PostRes
import berryful.lounge.api.entity.*
import berryful.lounge.api.externalApi.firebase.Notifier
import berryful.lounge.api.repository.crm.BlockedMemberRepository
import berryful.lounge.api.repository.crm.DeviceRepository
import berryful.lounge.api.repository.crm.ForbiddenWordRepository
import berryful.lounge.api.repository.lounge.HashtagRepository
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.domain.Page
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class CommonService(
    private val forbiddenWordRepository: ForbiddenWordRepository,
    private val blockedMemberRepository: BlockedMemberRepository,
    private val hashtagRepository: HashtagRepository,
    private val deviceRepository: DeviceRepository,
    private val notifier: Notifier,
) {

    fun checkMember(member: Member): Int {
        if (member.status == MemberStatus.LEAVE) {
            return ErrorMessageCode.MEMBER_STATUS_LEAVE.code
        }

        if (member.status == MemberStatus.SUSPENDED) {
            return ErrorMessageCode.MEMBER_STATUS_SUSPENDED.code
        }

        if (!member.agreeService || !member.agreePersonalInfo || member.nickname == null) {
            return ErrorMessageCode.REQUEST_SIGNUP.code
        }
        return ErrorMessageCode.OK.code
    }

    fun checkForbiddenWordAndConvert(str: String): String {
        /*
        val forbiddenList = forbiddenWordRepository.findAll()
        var convertStr = str
        forbiddenList.forEach {
            val forbiddenWordCount = it.word.length
            var heartStr = ""
            for(i in 1..forbiddenWordCount) {
                heartStr = "$heartStr♡"
            }
            convertStr = convertStr.replace(it.word, heartStr)
        }
        return convertStr
        */
        return str
    }

    fun saveAndCountHashtags(str: String): String {
        var hashtags = str.split(" ")
        hashtags = hashtags.filter { !it.contains("♡") }
        hashtags.forEach { hashtag ->
            hashtagRepository.insertOrUpdateCount(hashtag)
        }
        return hashtags.joinToString(" ")
    }

    fun checkBlockedMember(memberId: Long, blockedMemberId: Long): Boolean {
        val blockedMemberList: List<BlockedMember>
        val blockedMembers: MutableList<Long> = mutableListOf(0)

        if (memberId != 0L) {
            blockedMemberList = blockedMemberRepository.findAllByMemberId(memberId)
            blockedMemberList.forEach { blockedMember ->
                blockedMembers.add(
                    blockedMember.blockedMemberId
                )
            }
        }

        if (blockedMembers.find { it == blockedMemberId } != null) {
            return true
        }
        return false
    }

    @Async
    fun findDeviceTokenAndPush(memberId: Long) {
        val devices = deviceRepository.findAllByMemberId(memberId)
        if (devices.isNotEmpty()) {
            val deviceTokens: List<String> = devices.map { it.notificationToken }
            notifier.sendResetBadge(deviceTokens, 0)
        }
    }

    fun convertClipRes(clipList: List<Article>, post: Article, member: Member?): Any {
        val returnClipList: MutableList<ClipRes> = mutableListOf()
        // 클립 순서 : 채택 클립 -> 포스트 작성자 클립 -> 안본 클립 -> 본 클립
        val adoptClip = clipList.filter { it.id == post.adoptClipId }
        val postWriterClipList = clipList.filter { it.member.id == post.member.id }
        val elseClipList = clipList.filter { it.id != post.adoptClipId && it.member.id != post.member.id }
        val sortClipList =
            if (member == null) adoptClip + postWriterClipList + elseClipList
            else {
                val unReadClipList = elseClipList.filter { !member.clipReadCheckList.contains(ClipReadCheck(member, it.id)) }
                val readClipList = elseClipList.filter { member.clipReadCheckList.contains(ClipReadCheck(member, it.id)) }
                adoptClip + postWriterClipList + unReadClipList + readClipList
            }
        sortClipList.forEach { clip ->
            returnClipList.add(
                ClipRes(
                    id = clip.id!!,
                    createAt = clip.createAt,
                    parentId = clip.parentId,
                    memberId = clip.member.id,
                    content = clip.content,
                    categoryId = clip.category?.id,
                    categoryName = clip.category?.name,
                    thumbsUp = clip.thumbsUpList.find { it.article.id == clip.id!! && it.member.id == member?.id } != null,
                    thumbsCount = clip.thumbsCount,
                    articleType = clip.articleType!!,
                    mediaType = clip.mediaType,
                    clipUrl = clip.clipUrl,
                    memberNickname = clip.member.nickname!!,
                    memberProfilePath = clip.member.profilePath,
                    comments = null,
                    commentCount = clip.childArticleList.filter { it.status == ArticleStatus.UNBLOCKED }.size,
                    hashtag = clip.hashtag,
                    clipTimeline = clip.clipTimeline,
                    clipReadCheck = member?.clipReadCheckList?.contains(ClipReadCheck(member, clip.id)) ?: false,
                    status = clip.status,
                    views = clip.views,
                    postId = post.id!!,
                    postTitle = post.title,
                    postMemberId = post.member.id,
                    postMemberNickname = post.member.nickname!!,
                    parentArticleType = post.articleType!!,
                    objectStrings = clip.objectStrings,
                )
            )
        }
        return returnClipList
    }

    fun compareVersion(v1: String, v2: String, index: Int = 0): Boolean {
        return try {
            val code1 = v1.split(".")[index].toInt()
            val code2 = v2.split(".")[index].toInt()
            val nextIndex = index + 1
            when {
                code1 < code2 ->
                    true
                code1 == code2
                -> {
                    compareVersion(v1, v2, nextIndex)
                }
                else -> false
            }
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }
}