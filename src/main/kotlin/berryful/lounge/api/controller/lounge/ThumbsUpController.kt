package berryful.lounge.api.controller.lounge

import berryful.lounge.api.data.ThumbsUpReq
import berryful.lounge.api.service.lounge.ThumbsUpService
import berryful.lounge.api.utils.Log
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  포스트 CRUD 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/lounge/v2")
class ThumbsUpController(
    private val thumbsUpService: ThumbsUpService,
) {
    /**
     * 좋아요 처리 API 입니다.
     * @param id 좋아요 처리 할 article 의 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return DB에 생성된 article id를 리턴합니다.
     */
    @PatchMapping("/thumbsUp/{id}")
    fun thumbsUpArticle(@RequestBody thumbsUpReq: ThumbsUpReq, @PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ThumbsUpController.thumbsUpArticle", " Param-> memberId= $memberId articleId= $id action= ${thumbsUpReq.action}")

        return ResponseEntity
            .ok()
            .body(thumbsUpService.thumbsUpArticle(memberId, id, thumbsUpReq))
    }
}