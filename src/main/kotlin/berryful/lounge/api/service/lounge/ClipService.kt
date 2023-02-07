package berryful.lounge.api.service.lounge

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.*
import berryful.lounge.api.externalApi.awsApiGateway.*
import berryful.lounge.api.repository.crm.BlockedMemberRepository
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.crm.NotificationRepository
import berryful.lounge.api.repository.lounge.ArticleCategoryRepository
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.repository.lounge.ClipReadCheckRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.service.crm.NotificationService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ClipService(
    private val commonService: CommonService,
    private val articleRepository: ArticleRepository,
    private val memberRepository: MemberRepository,
    private val clipReadCheckRepository: ClipReadCheckRepository,
    private val articleCategoryRepository: ArticleCategoryRepository,
    private val notificationService: NotificationService,
    private val awsApiGatewayClient: AwsApiGatewayClient,
) {
    @Transactional
    fun v2CreateClip(memberId: Long, postId: Long, req: ArticleReq): Any {
        if (req.content == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CONTENT.code)
        if (req.clipUrl == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CLIPURL.code)
        if (req.articleType != ArticleType.CLIP) return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        req.content = commonService.checkForbiddenWordAndConvert(req.content!!)
        if (req.hashtag != null && req.hashtag != "" && req.hashtag != " ") {
            req.hashtag = commonService.checkForbiddenWordAndConvert(req.hashtag!!)
            req.hashtag = commonService.saveAndCountHashtags(req.hashtag!!)
        }

        val post = articleRepository.findByIdAndArticleTypeAndStatus(postId, ArticleType.POST, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        val newClip = Article(
            member = member,
            parentId = post.id,
            content = req.content,
            articleType = ArticleType.CLIP,
            clipUrl = req.clipUrl,
            hashtag = req.hashtag
        )

        articleRepository.save(newClip)
        notificationService.createNotification(memberId, member.nickname!!, post.member.id!!, newClip.id!!, 2)
        post.interestedArticleList.forEach { interestedArticle ->
            if (interestedArticle.article.member.id == interestedArticle.member.id!!)
                return@forEach
            notificationService.createNotification(memberId, member.nickname!!, interestedArticle.member.id!!, newClip.id!!, 4)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun v3CreateClip(memberId: Long, postId: Long, req: ClipReq): Any {
        if (req.content == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CONTENT.code)
        if (req.clipUrl == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CLIPURL.code)
        if (req.articleType != ArticleType.CLIP) return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        req.content = commonService.checkForbiddenWordAndConvert(req.content!!)
        if (req.hashtag != null && req.hashtag != "" && req.hashtag != " ") {
            req.hashtag = commonService.checkForbiddenWordAndConvert(req.hashtag!!)
            req.hashtag = commonService.saveAndCountHashtags(req.hashtag!!)
        }

        val post = articleRepository.findByIdAndArticleTypeAndStatus(postId, ArticleType.POST, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        val newClip = Article(
            member = member,
            parentId = post.id,
            content = req.content,
            articleType = ArticleType.CLIP,
            mediaType = req.mediaType,
            clipUrl = req.clipUrl,
            clipTimeline = req.offsetEnd - req.offsetStart,
            hashtag = req.hashtag,
            objectStrings = req.objectStrings.toString(),
            clipEncodingCheck = 0
        )
        articleRepository.save(newClip)

        if (req.mediaType == MediaType.VIDEO)
            awsApiGatewayClient.clipEncoding(ClipEncodingReq(req.clipUrl, req.offsetStart, req.offsetEnd, newClip.id!!))

        notificationService.createNotification(memberId, member.nickname!!, post.member.id!!, newClip.id!!, 2)
        post.interestedArticleList.forEach { interestedArticle ->
            if (interestedArticle.article.member.id == interestedArticle.member.id!!)
                return@forEach
            notificationService.createNotification(memberId, member.nickname!!, interestedArticle.member.id!!, newClip.id!!, 4)
        }
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun v4CreateClip(memberId: Long, req: V4ClipReq): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        req.content = commonService.checkForbiddenWordAndConvert(req.content!!)
        if (req.hashtag != null && req.hashtag != "" && req.hashtag != " ") {
            req.hashtag = commonService.checkForbiddenWordAndConvert(req.hashtag!!)
            req.hashtag = commonService.saveAndCountHashtags(req.hashtag!!)
        }

        val category = articleCategoryRepository.findByIdOrNull(req.category)
        val newClip = Article(
            member = member,
            category = category,
            content = req.content,
            articleType = ArticleType.CLIP,
            mediaType = req.mediaType,
            clipUrl = req.clipUrl,
            clipTimeline = req.offsetEnd - req.offsetStart,
            hashtag = req.hashtag,
            objectStrings = req.objectStrings.toString(),
            clipEncodingCheck = 0
        )
        articleRepository.save(newClip)

        if (req.mediaType == MediaType.VIDEO)
            awsApiGatewayClient.clipEncoding(ClipEncodingReq(req.clipUrl, req.offsetStart, req.offsetEnd, newClip.id!!))

        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getClipList(params: ClipParams, memberId: Long): Any {
        val clipPage =
            if (memberId != 0L)
                articleRepository.findAllClip(PageRequest.of(params.page, params.size), memberId)
            else
                articleRepository.findAllClip(PageRequest.of(params.page, params.size))

        return PageableSetRes(
            rowCount = clipPage.totalElements.toInt(),
            rows = clipPage.toList(),
        )
    }

    @Transactional(readOnly = true)
    fun getPostClipList(postId: Long, memberId: Long): Any {
        val post = articleRepository.findByIdAndArticleTypeAndStatus(postId, ArticleType.POST, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        val clipList = articleRepository.findAllClip(postId, memberId)
        val member = memberRepository.findByIdOrNull(memberId)

        return commonService.convertClipRes(clipList, post, member)
    }

    @Transactional(readOnly = true)
    fun getPopularClipList(memberId: Long, params: ClipParams): Any {
        val popularClipPage =
            if (memberId != 0L)
                articleRepository.findPopularClip(PageRequest.of(params.page, params.size), memberId, params)
            else
                articleRepository.findPopularClip(PageRequest.of(params.page, params.size), params)

        return PageableSetRes(
            rowCount = popularClipPage.totalElements.toInt(),
            rows = popularClipPage.toList(),
        )
    }

    @Transactional
    fun updateClip(id: Long, memberId: Long, req: ArticleReq): Any {
        if (req.articleType != ArticleType.CLIP) {
            return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)
        }
        val clip = articleRepository.findByIdAndArticleTypeAndStatus(id, ArticleType.CLIP, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CLIP.code)

        if (memberId == clip.member.id) {
            clip.content = commonService.checkForbiddenWordAndConvert(req.content!!)
            clip.clipUrl = req.clipUrl
            articleRepository.save(clip)
            return ApiResultCode(ErrorMessageCode.OK.code)
        }
        return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)
    }

    @Transactional
    fun deleteClip(memberId: Long, clipId: Long): Any {
        val clip = articleRepository.findByIdAndArticleTypeAndStatus(clipId, ArticleType.CLIP, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CLIP.code)

        if (memberId != clip.member.id) {
            return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)
        }

        val adoptClipPost = articleRepository.findByAdoptClipId(clipId)
        if (adoptClipPost != null && Instant.now().minus(7, ChronoUnit.DAYS) < adoptClipPost.adoptAt)
            return ApiResultCode(ErrorMessageCode.CANNOT_DELETE_ADOPT_CLIP.code)

        articleRepository.deleteAllCommentAndReplyByClipId(clip.id!!)

        if (adoptClipPost != null) {
            clip.content = ""
            clip.thumbsCount = 0
            clip.clipTimeline = null
            clip.hashtag = null
            articleRepository.save(clip)
            awsApiGatewayClient.deleteClipS3Files(DeleteClipReq(clipUrl = clip.clipUrl, adoptCheck = true))
            return ApiResultCode(ErrorMessageCode.OK.code)
        }

        articleRepository.delete(clip)
        awsApiGatewayClient.deleteClipS3Files(DeleteClipReq(clipUrl = clip.clipUrl, adoptCheck = false))
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun clipViewCount(id: Long, memberId: Long): Any {
        articleRepository.findByIdAndArticleTypeAndStatus(id, ArticleType.CLIP, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.OK.code)

        articleRepository.viewCountUp(id)

        if (memberId == 0L)
            return ApiResultCode(ErrorMessageCode.OK.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.OK.code)

        clipReadCheckRepository.save(ClipReadCheck(member, id))
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun adoptClip(memberId: Long, clipId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        val clip = articleRepository.findByIdAndArticleTypeAndStatus(clipId, ArticleType.CLIP, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CLIP.code)

        if (memberId == clip.member.id)
            return ApiResultCode(ErrorMessageCode.CANNOT_ADOPT_CLIP.code)

        val post = articleRepository.findByIdAndArticleTypeAndStatus(clip.parentId!!, ArticleType.POST, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        if (memberId != post.member.id)
            return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)

        if (post.adoptClipId != null)
            return ApiResultCode(ErrorMessageCode.ALREADY_ADOPT_CLIP.code)

        post.adoptClipId = clipId
        post.adoptAt = Instant.now()
        articleRepository.save(post)

        notificationService.createNotification(memberId, member.nickname!!, clip.member.id!!, clip.id!!, 3)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getRecommendClips(pageable: Pageable, memberId: Long): Any {
        var clipPage: Page<Any> = if (memberId != 0L)
            articleRepository.findPersonalizeRecommendClips(pageable, memberId, 7, 0)
        else
            articleRepository.findBasicRecommendClips(pageable, 7, 0)

        if (clipPage.isEmpty)
            clipPage = articleRepository.findBasicRecommendClips(PageRequest.of(0, 10), 7, 0)

        return RecommendClipRes(
            clipCount = clipPage.totalElements.toInt(),
            clips = clipPage.toList(),
        )
    }

    @Transactional
    fun clipEncodingComplete(req: ClipEncodingCompleteReq): Any {
        return articleRepository.clipEncodingCheck(req.clipId)
    }

    @Transactional(readOnly = true)
    fun getFollowingClips(pageable: Pageable, memberId: Long): Any {
        val clipPage = articleRepository.findFollowingClips(pageable, memberId)

        return RecommendClipRes(
            clipCount = clipPage.totalElements.toInt(),
            clips = clipPage.toList(),
        )
    }

    @Transactional
    fun updateClipTimeline(req: ClipTimelineReq): Any {
        return articleRepository.updateClipTimeline(req.clipUrl, req.clipTimeline ?: 0)
    }
}