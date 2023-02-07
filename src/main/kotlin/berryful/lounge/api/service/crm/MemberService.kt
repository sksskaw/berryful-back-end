package berryful.lounge.api.service.crm

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.*
import berryful.lounge.api.externalApi.awsApiGateway.AwsApiGatewayClient
import berryful.lounge.api.externalApi.awsApiGateway.DeleteClipReq
import berryful.lounge.api.externalApi.awsApiGateway.DeleteMemberProfileReq
import berryful.lounge.api.repository.crm.*
import berryful.lounge.api.repository.lounge.RewardWinnerRepository
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.domain.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val commonService: CommonService,
    private val memberRepository: MemberRepository,
    private val blockedMemberRepository: BlockedMemberRepository,
    private val articleRepository: ArticleRepository,
    private val rewardWinnerRepository: RewardWinnerRepository,
    private val forbiddenWordRepository: ForbiddenWordRepository,
    private val notificationRepository: NotificationRepository,
    private val deviceRepository: DeviceRepository,
    private val memberAccessHistoryRepository: MemberAccessHistoryRepository,
    private val memberFollowRepository: MemberFollowRepository,
    private val notificationService: NotificationService,
    private val awsApiGatewayClient: AwsApiGatewayClient,
) {
    fun returnUpdateMemberInfoRes(member: Member): UpdateMemberInfoRes {
        return UpdateMemberInfoRes(
            resultCode = ErrorMessageCode.OK.code,
            email = member.email,
            phoneNumber = member.phoneNumber,
            nickname = member.nickname!!,
            profilePath = member.profilePath,
            gender = member.gender,
            birthday = member.birthday,
            profileIntro = member.profileIntro,
            youtubeUrl = member.youtubeUrl,
            instagramId = member.instagramUrl,
            blogUrl = member.blogUrl,
        )
    }

    @Transactional
    fun updateMemberInfo(updateMemberInfoReq: UpdateMemberInfoReq, memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        when (updateMemberInfoReq.updateColumn) {
            "nickname" -> {
                val nickname = updateMemberInfoReq.updateData.replace(" ", "")
                val exp = Regex("^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9 -]{2,12}\$")
                if (!exp.matches(nickname)) {
                    return ApiResultCode(ErrorMessageCode.INVALID_NICKNAME.code)
                }

                val forbiddenList = forbiddenWordRepository.findAll()
                forbiddenList.forEach {
                    if (nickname.lowercase().contains(it.word)) return ApiResultCode(ErrorMessageCode.FORBIDDEN_NICKNAME.code)
                }

                val uppercaseNickname = nickname.uppercase()
                if (memberRepository.findByUppercaseNickname(uppercaseNickname) != null) {
                    return ApiResultCode(ErrorMessageCode.DUPLICATE_NICKNAME.code)
                }
                member.nickname = nickname
                member.uppercaseNickname = uppercaseNickname
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "phoneNumber" -> {
                val expPhoneNumber = Regex("^010(?:\\d{3}|\\d{4})\\d{4}\$")
                if (!expPhoneNumber.matches(updateMemberInfoReq.updateData)) {
                    return ApiResultCode(ErrorMessageCode.INVALID_PHONENUMBER.code)
                }
                member.phoneNumber = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "profilePath" -> {
                awsApiGatewayClient.deleteMemberProfileS3File(DeleteMemberProfileReq(profileUrl = member.profilePath))
                member.profilePath = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "gender" -> {
                member.gender = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "birthday" -> {
                member.birthday = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "profileIntro" -> {
                member.profileIntro = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "youtubeUrl" -> {
                member.youtubeUrl = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "instagramId" -> {
                member.instagramUrl = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
            "blogUrl" -> {
                member.blogUrl = updateMemberInfoReq.updateData
                memberRepository.save(member)
                return returnUpdateMemberInfoRes(member)
            }
        }

        return ApiResultCode(ErrorMessageCode.BAD_REQUEST.code)
    }

    @Transactional
    fun deleteMember(memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        val articleList = articleRepository.findAllByMemberId(memberId)
        // 내가 업로드한 클립을 우선 전체 삭제
        articleList.forEach { clip ->
            if (clip.articleType == ArticleType.CLIP) {

                val winnerClip = rewardWinnerRepository.findByArticle(clip)
                if (winnerClip != null)
                    return@forEach

                val adoptClipPost = articleRepository.findByAdoptClipId(clip.id!!)
                if (adoptClipPost != null) {
                    clip.content = "deleted adopt clip"
                    clip.thumbsCount = 0
                    clip.clipTimeline = null
                    clip.hashtag = null
                    articleRepository.save(clip)
                    awsApiGatewayClient.deleteClipS3Files(DeleteClipReq(clipUrl = clip.clipUrl, adoptCheck = true))
                    return@forEach
                }
                articleRepository.delete(clip)
                awsApiGatewayClient.deleteClipS3Files(DeleteClipReq(clipUrl = clip.clipUrl, adoptCheck = false))
            }
        }

        // 클립이 없는 포스트 삭제
        articleList.forEach {
            if (it.articleType == ArticleType.POST) {
                val clipList = articleRepository.findAllByParentIdAndArticleType(it.id!!, ArticleType.CLIP)
                if (clipList.isEmpty()) {
                    articleRepository.delete(it)
                }
            } else { // 남아있는 댓글 답글 전체삭제
                if (it.articleType == ArticleType.COMMENT || it.articleType == ArticleType.REPLY) {
                    articleRepository.delete(it)
                }
            }
        }

        // 알림 삭제
        notificationRepository.deleteAllByMemberIdOrFromMemberId(memberId, memberId)

        // s3 프로필 파일 삭제
        awsApiGatewayClient.deleteMemberProfileS3File(DeleteMemberProfileReq(profileUrl = member.profilePath))

        member.email = null
        member.status = MemberStatus.LEAVE
        member.profilePath = null
        member.gender = null
        member.birthday = null
        member.agreePersonalInfo = false
        member.agreeService = false
        member.agreeMarketing = false
        member.snsKakao = null
        member.snsApple = null
        member.snsFacebook = null
        member.snsNaver = null
        member.phoneNumber = null
        member.notificationCheck = true
        member.snsType = null
        member.snsPhoneNumber = null
        member.snsEmail = null
        member.profileIntro = null
        member.youtubeUrl = null
        member.instagramUrl = null
        member.blogUrl = null
        memberRepository.save(member)

        val memberDeviceList = deviceRepository.findAllByMemberId(member.id!!)
        memberDeviceList.forEach {
            it.memberId = 0L
            deviceRepository.save(it)
        }

        memberAccessHistoryRepository.save(
            MemberAccessHistory(
                member = member,
                activityType = ActivityType.LEAVE,
            )
        )

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getMemberInfo(id: Long, memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(id)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        var follow = false
        if (memberId != 0L)
            follow = member.memberFollowerList.any { it.follower.id == memberId && it.following.status.toString() == "ACTIVE" } == true

        return MemberProfileInfo(
            id = member.id,
            nickname = member.nickname!!,
            profilePath = member.profilePath,
            profileIntro = member.profileIntro,
            youtubeUrl = member.youtubeUrl,
            instagramId = member.instagramUrl,
            blogUrl = member.blogUrl,
            followerCount = member.memberFollowerList.filter { it.follower.status.toString() == "ACTIVE" }.size,
            followingCount = member.memberFollowingList.filter { it.following.status.toString() == "ACTIVE" }.size,
            clipCount = member.articleList.filter { it.articleType.toString() == "CLIP" && it.status.toString() == "UNBLOCKED" }.size,
            follow = follow,
        )
    }

    fun blockMember(memberId: Long, blockMemberId: Long): Any {
        if (blockedMemberRepository.insertBlockedMember(memberId, blockMemberId) < 1) {
            return ApiResultCode(ErrorMessageCode.ALREADY_BLOCKED_MEMBER.code)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun unblockMember(memberId: Long, unblockMemberId: Long): Any {
        if (blockedMemberRepository.deleteBlockedMember(memberId, unblockMemberId) < 1) {
            return ApiResultCode(ErrorMessageCode.ALREADY_UNBLOCKED_MEMBER.code)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getBlockedList(pageable: Pageable, memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        val blockedMemberPage = blockedMemberRepository.findAllBlockedListByMemberId(pageable, memberId)

        return PageableSetRes(
            rowCount = blockedMemberPage.totalElements.toInt(),
            rows = blockedMemberPage.toList(),
        )
    }

    @Transactional(readOnly = true)
    fun memberActivityInit(memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        val memberArticles = member.articleList
        val totalPostCount = memberArticles.filter { it.articleType == ArticleType.POST }.size
        val totalClipCount = memberArticles.filter { it.articleType == ArticleType.CLIP }.size
        val totalCommentCount = memberArticles.filter { it.articleType == ArticleType.COMMENT }.size
        val totalReplyCount = memberArticles.filter { it.articleType == ArticleType.REPLY }.size
        val badgeCount = notificationRepository.selectBadgeCount(memberId)

        return InitMemberRes(
            notificationCheck = member.notificationCheck,
            badgeCount = badgeCount,
            totalPostsCreated = totalPostCount,
            totalClipsCreated = totalClipCount,
            totalCommentsCreated = totalCommentCount + totalReplyCount,
            emailVerification = member.email == null,
            accountType = member.snsType ?: "email",
            notificationsEnabled = true,
        )
    }

    @Transactional(readOnly = true)
    fun getUploadedList(id: Long, memberId: Long, params: MutableMap<String, String>): Any {
        val page = params["page"]?.toInt() ?: 0
        val size = params["size"]?.toInt() ?: 10
        val sort = params["sort"] ?: "latestPost"
        when (params["articleType"]?.uppercase()) {
            "POST" -> return getUploadedPostList(PageRequest.of(page, size), id, memberId, sort)
            "CLIP" -> return getUploadedClipList(PageRequest.of(page, size), id, memberId)
        }

        return ApiResultCode(ErrorMessageCode.BAD_REQUEST.code)
    }

    fun getUploadedPostList(pageable: Pageable, targetMemberId: Long, loginMemberId: Long, sort: String): Any {
        val postPage =
            if (loginMemberId != 0L)
                articleRepository.findAllUploadedPost(pageable,targetMemberId,loginMemberId,sort)
            else
                articleRepository.findAllUploadedPost(pageable,targetMemberId,sort)

        return PageableSetRes(
            rowCount = postPage.totalElements.toInt(),
            rows = postPage.toList(),
        )
    }

    fun getUploadedClipList(pageable: Pageable, targetMemberId: Long, loginMemberId: Long): Any {
        val clipPage =
            if (loginMemberId != 0L)
                articleRepository.findAllUploadedClip(pageable,targetMemberId,loginMemberId)
            else
                articleRepository.findAllUploadedClip(pageable,targetMemberId)

        return PageableSetRes(
            rowCount = clipPage.totalElements.toInt(),
            rows = clipPage.toList(),
        )
    }

    @Transactional(readOnly = true)
    fun getLikedList(pageable: Pageable, memberId: Long, articleType: String): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        when (articleType.uppercase()) {
            "POST" -> return getLikedPostList(pageable, member)
            "CLIP" -> return getLikedClipList(pageable, member)
        }

        return ApiResultCode(ErrorMessageCode.BAD_REQUEST.code)
    }

    fun getLikedPostList(pageable: Pageable, member: Member): Any {
        val likedPostPage = articleRepository.findAllLikedPostList(pageable,member.id!!)

        return PageableSetRes(
            rowCount = likedPostPage.totalElements.toInt(),
            rows = likedPostPage.toList(),
        )
    }

    fun getLikedClipList(pageable: Pageable, member: Member): Any {
        val likedClipPage = articleRepository.findAllLikedClipList(pageable, member.id!!)

        return PageableSetRes(
            rowCount = likedClipPage.totalElements.toInt(),
            rows = likedClipPage.toList(),
        )
    }

    @Transactional
    fun followMember(memberId: Long, followMemberId: Long, req: FollowReq): Any {
        if (memberId == followMemberId) return ApiResultCode(ErrorMessageCode.OK.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val followMember = memberRepository.findByIdOrNull(followMemberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        val checkFollowMemberResult = commonService.checkMember(followMember)
        if (checkMemberResult != 0 || checkFollowMemberResult != 0) return ApiResultCode(checkMemberResult)

        val blockedMemberList = blockedMemberRepository.findAllByMemberId(memberId)
        val blockedMembers: MutableList<Long> = mutableListOf()
        blockedMemberList.forEach { blockedMember ->
            blockedMembers.add(
                blockedMember.blockedMemberId
            )
        }
        if (blockedMembers.any { it == followMember.id })
            return ApiResultCode(ErrorMessageCode.FOLLOW_MEMBER_BLOCKED.code)

        val memberFollow = memberFollowRepository.findByIdOrNull(MemberFollowId(memberId, followMemberId))

        if (memberFollow == null && req.action == "follow") {
            memberFollowRepository.insertMemberFollow(memberId, followMemberId)
            notificationService.createNotification(memberId, member.nickname!!, followMemberId, 0, 5)
        }

        if (memberFollow != null && req.action == "unfollow")
            memberFollowRepository.deleteMemberFollow(memberId, followMemberId)

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getFollowingList(pageable: Pageable, targetMemberId: Long, loginMemberId: Long): Any {
        val followingPage = memberFollowRepository.findAllFollowing(pageable, targetMemberId, loginMemberId)

        return PageableSetRes(
            rowCount = followingPage.totalElements.toInt(),
            rows = followingPage.toList(),
        )
    }

    @Transactional(readOnly = true)
    fun getFollowerList(pageable: Pageable, targetMemberId: Long, loginMemberId: Long): Any {
        val followingPage = memberFollowRepository.findAllFollowers(pageable, targetMemberId, loginMemberId)

        return PageableSetRes(
            rowCount = followingPage.totalElements.toInt(),
            rows = followingPage.toList(),
        )
    }

    fun deleteFollower(memberId: Long, deleteMemberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val memberFollow = member.memberFollowerList.find { it.follower.id == deleteMemberId && it.following.id == memberId }
        if (memberFollow != null)
            memberFollowRepository.deleteMemberFollow(memberFollow.follower.id!!, memberFollow.following.id!!)

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun pushSetting(memberId: Long, req: PushSettingReq): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        when (req.alertType) {
            "contentAlert" -> {
                member.pushThumbsUp = req.action
                member.pushContentUpload = req.action
                member.pushAdoptClip = req.action
                member.pushInterestedArticle = req.action
            }
            "followAlert" -> member.pushFollow = req.action
            "berryfulAlert" -> member.pushBerryful = req.action
        }

        memberRepository.save(member)
        return PushSettingRes(
            contentAlert = member.pushThumbsUp && member.pushContentUpload && member.pushAdoptClip && member.pushInterestedArticle,
            followAlert = member.pushFollow,
            berryfulAlert = member.pushBerryful,
        )
    }
}