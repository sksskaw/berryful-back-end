package berryful.lounge.api.service.lounge

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.*
import berryful.lounge.api.externalApi.awsApiGateway.AwsApiGatewayClient
import berryful.lounge.api.externalApi.awsApiGateway.DeleteClip
import berryful.lounge.api.externalApi.awsApiGateway.DeleteClipsReq
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.lounge.ArticleCategoryRepository
import berryful.lounge.api.repository.lounge.InterestedArticleRepository
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.service.crm.NotificationService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.Any
import kotlin.Long

@Service
class PostService(
    private val commonService: CommonService,
    private val articleRepository: ArticleRepository,
    private val memberRepository: MemberRepository,
    private val interestedArticleRepository: InterestedArticleRepository,
    private val articleCategoryRepository: ArticleCategoryRepository,
    private val notificationService: NotificationService,
    private val awsApiGatewayClient: AwsApiGatewayClient,
) {
    @Transactional
    fun createPost(memberId: Long, req: ArticleReq): Any {
        if (req.title == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_TITLE.code)
        if (req.content == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CONTENT.code)
        if (req.category == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CATEGORY.code)
        if (req.articleType != ArticleType.POST) return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        req.title = commonService.checkForbiddenWordAndConvert(req.title!!)
        req.content = commonService.checkForbiddenWordAndConvert(req.content!!)

        if (req.hashtag != null && req.hashtag != "" && req.hashtag != " ") {
            req.hashtag = commonService.checkForbiddenWordAndConvert(req.hashtag!!)
            req.hashtag = commonService.saveAndCountHashtags(req.hashtag!!)
        }

        val category = articleCategoryRepository.findByIdOrNull(req.category)
        val newPost = Article(
            member = member,
            title = req.title,
            content = req.content,
            articleType = ArticleType.POST,
            hashtag = req.hashtag,
            category = category,
        )

        articleRepository.save(newPost)
        return CreatePostRes(
            resultCode = ErrorMessageCode.OK.code,
            post = PostRes(
                id = newPost.id!!,
                createAt = newPost.createAt,
                memberId = newPost.member.id,
                title = newPost.title,
                content = newPost.content,
                thumbsUp = false,
                thumbsCount = newPost.thumbsCount,
                articleType = newPost.articleType!!,
                memberNickname = newPost.member.nickname!!,
                memberProfilePath = newPost.member.profilePath,
                clips = mutableListOf<Any>(),
                hashtag = newPost.hashtag,
                pickCount = newPost.interestedArticleList.size,
                pick = false,
                adoptClipId = newPost.adoptClipId,
                views = newPost.views,
                categoryId = newPost.category?.id,
                categoryName = newPost.category?.name,
            )
        )
    }

    @Cacheable(value= ["basicCacheConf"])
    @Transactional(readOnly = true)
    fun getPostList(pageable: Pageable, memberId: Long, params: PostParams): Any {
        val postPage =
            if (memberId != 0L)
                articleRepository.findAllPost(pageable, memberId, params)
            else
                articleRepository.findAllPost(pageable, params)

        return PageableSetRes(
            rowCount = postPage.totalElements.toInt(),
            rows = postPage.toList(),
        )
    }

    @Transactional(readOnly = true)
    fun getPostOne(id: Long, memberId: Long): Any {
        return if (memberId != 0L)
            articleRepository.findPost(id, memberId)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)
        else
            articleRepository.findPost(id)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)
    }

    @Transactional
    fun updatePost(id: Long, memberId: Long, req: ArticleReq): Any {
        val post = articleRepository.findByIdAndMemberIdAndArticleTypeAndStatus(id, memberId, req.articleType, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        if (req.articleType != ArticleType.POST) {
            return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)
        }

        if (req.category == null)
            return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CATEGORY.code)

        if (memberId != post.member.id)
            return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)

        post.title = commonService.checkForbiddenWordAndConvert(req.title!!)
        post.content = commonService.checkForbiddenWordAndConvert(req.content!!)
        post.hashtag = req.hashtag?.let { commonService.checkForbiddenWordAndConvert(it) }
        post.category = articleCategoryRepository.findByIdOrNull(req.category)
        articleRepository.save(post)

        val updatePost = articleRepository.findPost(post.id!!)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        return UpdatePostRes(
            ErrorMessageCode.OK.code,
            post = PostRes(
                id = updatePost.id,
                createAt = updatePost.createAt,
                memberId = updatePost.memberId,
                title = updatePost.title,
                content = updatePost.content,
                thumbsUp = updatePost.thumbsUp,
                thumbsCount = updatePost.thumbsCount,
                articleType = updatePost.articleType,
                memberNickname = updatePost.memberNickname,
                memberProfilePath = updatePost.memberProfilePath,
                clips = updatePost.clips,
                hashtag = updatePost.hashtag,
                pickCount = updatePost.pickCount,
                pick = updatePost.pick,
                adoptClipId = updatePost.adoptClipId,
                views = updatePost.views,
                categoryId = updatePost.categoryId,
                categoryName = updatePost.categoryName,
            )
        )
    }

    @Transactional
    fun deletePost(memberId: Long, postId: Long): Any {
        val post = articleRepository.findByIdAndArticleType(postId, ArticleType.POST)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        if (memberId != post.member.id)
            return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)

        val clipList = articleRepository.findAllClip(postId, memberId)
        if (clipList.any { clip -> clip.member.id != memberId })
            return ApiResultCode(ErrorMessageCode.CANNOT_DELETE_POST.code)

        val deleteClips: MutableList<DeleteClip> = mutableListOf()
        post.childArticleList.forEach { clip ->
            deleteClips.add(DeleteClip(clip.clipUrl, false))
        }

        awsApiGatewayClient.deleteAllClipS3Files(DeleteClipsReq(clips = deleteClips))
        articleRepository.deleteAllClipAndCommentAndReplyByPostId(postId)
        articleRepository.delete(post)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    fun postViewCount(id: Long): Any {
        articleRepository.findByIdAndArticleTypeAndStatus(id, ArticleType.POST, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        articleRepository.viewCountUp(id)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun pickPost(memberId: Long, postId: Long, pickReq: PickReq): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        val post = articleRepository.findByIdAndArticleTypeAndStatus(postId, ArticleType.POST, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_POST.code)

        val interestedArticle = interestedArticleRepository.findByIdOrNull(InterestedArticleId(memberId, postId))

        if (interestedArticle == null && pickReq.action == "pick") {
            notificationService.createNotification(memberId, member.nickname!!, post.member.id!!, post.id!!, 4)
            interestedArticleRepository.save(InterestedArticle(Instant.now(), member, post))
            interestedArticleRepository.flush()
            return PickRes(ErrorMessageCode.OK.code, post.interestedArticleList.size)
        }

        if (interestedArticle != null && pickReq.action == "unpick") {
            interestedArticleRepository.delete(interestedArticle)
            interestedArticleRepository.flush()
            return PickRes(ErrorMessageCode.OK.code, post.interestedArticleList.size)
        }

        return PickRes(ErrorMessageCode.OK.code, post.interestedArticleList.size)
    }

    @Transactional(readOnly = true)
    fun getInterestedPostList(pageable: Pageable, memberId: Long, params: InterestedPostParams): Any {
        val interestedPostPage = articleRepository.findAllInterestedPost(pageable, memberId, params)

        return PageableSetRes(
            rowCount = interestedPostPage.totalElements.toInt(),
            rows = interestedPostPage.toList(),
        )
    }

    @Transactional(readOnly = true)
    fun getHotPost(memberId: Long, params: PostParams): Any {
        return if (memberId != 0L)
            articleRepository.findHotPost(params, memberId)
                ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)
        else
            articleRepository.findHotPost(params)
                ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)
    }

    @Transactional(readOnly = true)
    fun getBestInterestedPost(memberId: Long, params: PostParams): Any {
        return if (memberId != 0L)
            articleRepository.findBestInterestedPost(params, memberId)
                ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)
        else
            articleRepository.findBestInterestedPost(params)
                ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)
    }

    @Transactional(readOnly = true)
    fun getCategoryList(): Any {
        val categoryList = articleCategoryRepository.findAllCategory()

        val returnCategoryList: MutableList<CategoryRes> = mutableListOf()
        categoryList.forEach {
            returnCategoryList.add(
                CategoryRes(it.id, it.name)
            )
        }
        return returnCategoryList
    }

    @Transactional(readOnly = true)
    fun getPopularPostList(memberId: Long): Any {
        val popularPostList =
            if (memberId != 0L)
                articleRepository.findPopularPost(memberId)
            else
                articleRepository.findPopularPost()

        return PageableSetRes(
            rowCount = popularPostList.size,
            rows = popularPostList,
        )
    }
}