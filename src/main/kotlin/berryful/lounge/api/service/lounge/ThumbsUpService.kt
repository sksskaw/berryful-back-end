package berryful.lounge.api.service.lounge

import berryful.lounge.api.data.ThumbsUpReq
import berryful.lounge.api.data.ThumbsUpRes
import berryful.lounge.api.entity.ArticleStatus
import berryful.lounge.api.entity.ThumbsUp
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.repository.lounge.ThumbsUpRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.service.crm.NotificationService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ThumbsUpService(
    private val commonService: CommonService,
    private val articleRepository: ArticleRepository,
    private val memberRepository: MemberRepository,
    private val thumbsUpRepository: ThumbsUpRepository,
    private val notificationService: NotificationService,
) {
    @Transactional
    fun thumbsUpArticle(memberId: Long, articleId: Long, thumbsUpReq: ThumbsUpReq): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        val article = articleRepository.findByIdAndStatus(articleId, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_ARTICLE.code)

        if (thumbsUpReq.action == "like" && thumbsUpRepository.insertThumbsUp(articleId, memberId) > 0) {
            thumbsUpRepository.save(ThumbsUp(Instant.now(), article, member))
            article.thumbsCount++
            articleRepository.save(article)
            notificationService.createNotification(memberId, member.nickname!!, article.member.id!!, articleId, 1)
            return ThumbsUpRes(ErrorMessageCode.OK.code, article.thumbsCount)
        }

        if (thumbsUpReq.action == "unlike" && thumbsUpRepository.deleteThumbsUp(articleId, memberId) > 0) {
            thumbsUpRepository.delete(ThumbsUp(Instant.now(), article, member))
            article.thumbsCount--
            articleRepository.save(article)
            return ThumbsUpRes(ErrorMessageCode.OK.code, article.thumbsCount)
        }

        return ThumbsUpRes(ErrorMessageCode.OK.code, article.thumbsCount)
    }
}