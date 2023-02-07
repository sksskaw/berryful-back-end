package berryful.lounge.api.controller.lounge

import berryful.lounge.api.data.ArticleReq
import berryful.lounge.api.service.lounge.CommentService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  댓글, 대댓글 CRUD 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/lounge/v2")
class CommentController(
    private val commentService: CommentService,
) {
    /**
     * 댓글 작성 API 입니다.
     * @param clipId 댓글을 작성할 클립의 id 입니다.
     * @param articleReq 댓글 작성에 필요한 데이터입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @PostMapping("/comment/{clipId}")
    fun createComment(@PathVariable clipId: Long, @RequestBody articleReq: ArticleReq, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("CommentController.createComment", " Param-> clipId= $clipId memberId= $memberId articleReq= $articleReq")

        return ResponseEntity
            .ok()
            .body(commentService.createComment(memberId, clipId, articleReq))
    }

    /**
     * 댓글 리스트 API 입니다.
     * @param clipId 댓글 목록을 가져올 클립의 id 입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @GetMapping("/commentList/{clipId}")
    fun getCommentList(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        @PathVariable clipId: Long,
        request: HttpServletRequest): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("CommentController.getCommentList", "Param-> clipId= $clipId")

        val sort = Sort.by(Sort.Direction.DESC, "createAt")
        return ResponseEntity
            .ok()
            .body(commentService.getCommentList(PageRequest.of(page, size, sort), clipId, memberId))
    }

    /**
     * 댓글 삭제 API 입니다.
     * @param id 삭제할 댓글의 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @DeleteMapping("/comment/{id}")
    fun deleteComment(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.deleteComment", " Param-> memberId= $memberId id= $id")

        return ResponseEntity
            .ok()
            .body(commentService.deleteComment(memberId, id))
    }

    /**
     * 댓글 수정 API 입니다.
     * @param id 삭제할 댓글의 id 입니다.
     * @param articleReq 수정할 댓글 데이터 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @PutMapping("/comment/{id}")
    fun putComment(
        @PathVariable id: Long,
        @RequestBody articleReq: ArticleReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.updateClip", " Param-> memberId= $memberId clipId= $id articleReq= $articleReq")

        return ResponseEntity
            .ok()
            .body(commentService.updateComment(id, memberId, articleReq))
    }

    /**
     * 대댓글 작성 API 입니다.
     * @param commentId 대댓글을 작성할 댓글의 id 입니다.
     * @param articleReq 대댓글 작성에 필요한 데이터입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @PostMapping("/reply/{commentId}")
    fun createReply(
        @PathVariable commentId: Long,
        @RequestBody articleReq: ArticleReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out(
            "CommentController.createReply",
            " Param-> clipId= $commentId memberId= $memberId articleReq= $articleReq"
        )

        return ResponseEntity
            .ok()
            .body(commentService.createReply(memberId, commentId, articleReq))
    }

    /**
     * 대댓글 리스트 API 입니다.
     * @param commentId 대댓글 목록을 가져올 댓글의 id 입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @GetMapping("/replyList/{commentId}")
    fun getReplyList(@PathVariable commentId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        var memberId = 0L
        if (request.userPrincipal != null) {
            memberId = request.userPrincipal!!.name.toLong()
        }
        Log.out("CommentController.getReplyList", "Param-> commentId= $commentId")
        return ResponseEntity
            .ok()
            .body(commentService.getReplyList(commentId, memberId))
    }

    /**
     * 대댓글 삭제 API 입니다.
     * @param id 삭제 대댓글 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @DeleteMapping("/reply/{id}")
    fun deleteReply(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("CommentController.deleteReply", " Param-> memberId= $memberId id= $id")

        return ResponseEntity
            .ok()
            .body(commentService.deleteReply(memberId, id))
    }

    /**
     * 대댓글 수정 API 입니다.
     * @param id 수정할 대댓글 id 입니다.
     * @param articleReq 수정할 대댓글 데이터 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @PutMapping("/reply/{id}")
    fun putReply(
        @PathVariable id: Long,
        @RequestBody articleReq: ArticleReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("CommentController.putReply", " Param-> memberId= $memberId clipId= $id articleReq= $articleReq")

        return ResponseEntity
            .ok()
            .body(commentService.updateReply(id, memberId, articleReq))
    }
}