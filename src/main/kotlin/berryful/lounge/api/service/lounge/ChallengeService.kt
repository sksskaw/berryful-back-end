package berryful.lounge.api.service.lounge

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.Article
import berryful.lounge.api.entity.ArticleStatus
import berryful.lounge.api.entity.ArticleType
import berryful.lounge.api.entity.MediaType
import berryful.lounge.api.externalApi.awsApiGateway.AwsApiGatewayClient
import berryful.lounge.api.externalApi.awsApiGateway.ClipEncodingReq
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.service.crm.NotificationService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChallengeService(
    private val commonService: CommonService,
    private val articleRepository: ArticleRepository,
    private val memberRepository: MemberRepository,
    private val awsApiGatewayClient: AwsApiGatewayClient,
) {
    @Transactional(readOnly = true)
    fun getChallengeBanner(): Any {
        var challenges =
            articleRepository.findAllByArticleTypeAndStatusAndChallengeEndAtGreaterThanEqual(ArticleType.CHALLENGE, ArticleStatus.UNBLOCKED, Instant.now())

        challenges = challenges.sortedByDescending { it.createAt }
        val challengeBanners: MutableList<ChallengeBannerRes> = mutableListOf()
        challenges.forEach {
            val status = checkChallengeStatus(it.challengeStartAt, it.challengeEndAt)
            challengeBanners.add(
                ChallengeBannerRes(it.id, it.challengeBannerUrl, status)
            )
        }
        return challengeBanners
    }

    fun checkChallengeStatus(start: Instant?, end: Instant?): String {
        val now = Instant.now()
        if (now > start && now > end)
            return "closed"
        if (now >= start && now <= end)
            return "opened"
        return "waiting"
    }

    @Transactional
    fun createChallengeClip(memberId: Long, challengeId: Long, req: ClipReq): Any {
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

        val challenge = articleRepository.findByIdAndArticleTypeAndStatus(challengeId, ArticleType.CHALLENGE, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CHALLENGE.code)

        val newClip = Article(
            member = member,
            parentId = challenge.id,
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
    fun getChallengeList(pageable: Pageable, memberId: Long, params: ChallengeParams): Any {
        val challengePage =
            if (memberId != 0L)
                articleRepository.findAllChallenge(pageable, memberId, params)
            else
                articleRepository.findAllChallenge(pageable, params)

        return PageableSetRes(
            rowCount = challengePage.totalElements.toInt(),
            rows = challengePage.toList(),
        )
    }

    @Transactional(readOnly = true)
    fun getChallengeOne(memberId: Long, challengeId: Long): Any {
        return if (memberId != 0L)
            articleRepository.findChallenge(challengeId, memberId)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CHALLENGE.code)
        else
            articleRepository.findChallenge(challengeId)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CHALLENGE.code)
    }
}