package berryful.lounge.api.service.lounge

import berryful.lounge.api.data.ArticleReq
import berryful.lounge.api.data.PageableSetRes
import berryful.lounge.api.entity.*
import berryful.lounge.api.repository.crm.BlockedMemberRepository
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.crm.NotificationRepository
import berryful.lounge.api.repository.lounge.article.ArticleRepository
import berryful.lounge.api.service.CommonService
import berryful.lounge.api.service.crm.NotificationService
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commonService: CommonService,
    private val articleRepository: ArticleRepository,
    private val memberRepository: MemberRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationService: NotificationService,
) {
    @Transactional
    fun createComment(memberId: Long, clipId: Long, req: ArticleReq): Any {
        if (req.content == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CONTENT.code)
        if (req.articleType != ArticleType.COMMENT) return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        req.content = commonService.checkForbiddenWordAndConvert(req.content!!)

        val clip =
            articleRepository.findByIdAndArticleTypeAndStatus(clipId, ArticleType.CLIP, ArticleStatus.UNBLOCKED)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_CLIP.code)

        val newComment = Article(
            member = member,
            parentId = clip.id,
            content = req.content,
            articleType = ArticleType.COMMENT,
        )

        articleRepository.save(newComment)
        notificationService.createNotification(memberId, member.nickname!!, clip.member.id!!, newComment.id!!, 2)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getCommentList(pageable: Pageable, clipId: Long, memberId: Long): Any {
        val commentPage =
            if (memberId != 0L)
                articleRepository.findAllComment(pageable, clipId, memberId)
            else
                articleRepository.findAllComment(pageable, clipId)

        return PageableSetRes(
            rowCount = commentPage.totalElements.toInt(),
            rows = commentPage.toList(),
        )
    }

    @Transactional
    fun updateComment(id: Long, memberId: Long, req: ArticleReq): Any {
        val comment =
            articleRepository.findByIdAndArticleTypeAndStatus(id, ArticleType.COMMENT, ArticleStatus.UNBLOCKED)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_COMMENT.code)

        if (req.articleType != ArticleType.COMMENT) {
            return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)
        }

        if (memberId == comment.member.id) {
            comment.content = commonService.checkForbiddenWordAndConvert(req.content!!)
            comment.modified = true
            articleRepository.save(comment)
            return ApiResultCode(ErrorMessageCode.OK.code)
        }
        return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)
    }

    @Transactional
    fun deleteComment(memberId: Long, commentId: Long): Any {
        val comment = articleRepository.findByIdAndArticleType(commentId, ArticleType.COMMENT)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_COMMENT.code)

        if (memberId != comment.member.id)
            return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)

        articleRepository.deleteAllReplyByCommentId(comment.id!!)
        articleRepository.delete(comment)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun createReply(memberId: Long, commentId: Long, req: ArticleReq): Any {
        if (req.content == null) return ApiResultCode(ErrorMessageCode.REQUIRED_TO_CONTENT.code)
        if (req.articleType != ArticleType.REPLY) return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)

        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val checkMemberResult = commonService.checkMember(member)
        if (checkMemberResult != 0) return ApiResultCode(checkMemberResult)

        req.content = commonService.checkForbiddenWordAndConvert(req.content!!)

        val comment =
            articleRepository.findByIdAndArticleTypeAndStatus(commentId, ArticleType.COMMENT, ArticleStatus.UNBLOCKED)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_COMMENT.code)

        val newReply = Article(
            member = member,
            parentId = comment.id,
            content = req.content,
            articleType = ArticleType.REPLY,
        )

        articleRepository.save(newReply)
        notificationService.createNotification(memberId, member.nickname!!, comment.member.id!!, newReply.id!!, 2)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getReplyList(commentId: Long, memberId: Long): Any {
        return if (memberId != 0L)
            articleRepository.findAllReply(commentId, memberId)
        else
            articleRepository.findAllReply(commentId)
    }

    @Transactional
    fun updateReply(id: Long, memberId: Long, req: ArticleReq): Any {
        val reply = articleRepository.findByIdAndArticleTypeAndStatus(id, ArticleType.REPLY, ArticleStatus.UNBLOCKED)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_REPLY.code)

        if (req.articleType != ArticleType.REPLY) {
            return ApiResultCode(ErrorMessageCode.ARTICLE_TYPE_MISMATCH.code)
        }

        if (memberId == reply.member.id) {
            reply.content = commonService.checkForbiddenWordAndConvert(req.content!!)
            reply.modified = true
            articleRepository.save(reply)
            return ApiResultCode(ErrorMessageCode.OK.code)
        }
        return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)
    }

    @Transactional
    fun deleteReply(memberId: Long, replyId: Long): Any {
        val reply = articleRepository.findByIdAndArticleType(replyId, ArticleType.REPLY)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_REPLY.code)

        if (memberId != reply.member.id)
            return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)

        articleRepository.delete(reply)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }
}