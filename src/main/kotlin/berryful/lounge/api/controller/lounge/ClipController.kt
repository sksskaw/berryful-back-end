package berryful.lounge.api.controller.lounge

import berryful.lounge.api.data.*
import berryful.lounge.api.service.lounge.ClipService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *  클립 CRUD 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/lounge")
class ClipController(
    private val clipService: ClipService,
) {
    /**
     * 클립 업로드 API 입니다.
     * @param postId 클립을 업로드할 포스트의 id 입니다.
     * @param articleReq 클립 업로드에 필요한 데이터입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @PostMapping("/v2/clip/{postId}")
    fun createClip(
        @PathVariable postId: Long,
        @RequestBody articleReq: ArticleReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.createClip", " Param-> postId= $postId memberId= $memberId articleReq= $articleReq")

        return ResponseEntity
            .ok()
            .body(clipService.v2CreateClip(memberId, postId, articleReq))
    }

    @PostMapping("/v3/clip/{postId}")
    fun v3CreateClip(
        @PathVariable postId: Long,
        @RequestBody clipReq: ClipReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.createClip", " Param-> postId= $postId memberId= $memberId clipReq= $clipReq")

        return ResponseEntity
            .ok()
            .body(clipService.v3CreateClip(memberId, postId, clipReq))
    }

    @PostMapping("/v4/clip")
    fun v4CreateClip(
        @RequestBody clipReq: V4ClipReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.createClip", " Param->memberId= $memberId clipReq= $clipReq")

        return ResponseEntity
            .ok()
            .body(clipService.v4CreateClip(memberId, clipReq))
    }

    /**
     * 클립 리스트 API 입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @GetMapping("/v2/clipList")
    fun getClipList(params: ClipParams, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("ClipController.getClipList", "params -> $params")
        return ResponseEntity
            .ok()
            .body(clipService.getClipList(params, memberId))
    }

    /**
     * 포스트 클립 리스트 API 입니다.
     * @param postId 클립 목록을 가져올 포스트의 id 입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @GetMapping("/v2/clipList/{postId}")
    fun getPostClipList(@PathVariable postId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("ClipController.getPostClipList", " Param-> postId= $postId")
        return ResponseEntity
            .ok()
            .body(clipService.getPostClipList(postId, memberId))
    }

    /**
     * 인기 클립 리스트 API 입니다.
     * @return 인기 클립 목록을 리턴합니다.
     */
    @GetMapping("/v2/popular/clipList")
    fun getPopularClipList(params: ClipParams, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("ClipController.getPopularClipList", "params -> $params")
        return ResponseEntity
            .ok()
            .body(clipService.getPopularClipList(memberId, params))
    }

    /**
     * 클립 삭제 API 입니다.
     * @param id 삭제할 클립 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @DeleteMapping("/v2/clip/{id}")
    fun deleteClip(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.deleteClip", " Param-> memberId= $memberId id= $id")

        return ResponseEntity
            .ok()
            .body(clipService.deleteClip(memberId, id))
    }

    /**
     * 클립 수정 API 입니다.
     * @param id 수정할 클립 id 입니다.
     * @param articleReq 수정할 클립 데이터 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @PutMapping("/v2/clip/{id}")
    fun updateClip(
        @PathVariable id: Long,
        @RequestBody articleReq: ArticleReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.updateClip", " Param-> memberId= $memberId clipId= $id articleReq= $articleReq")

        return ResponseEntity
            .ok()
            .body(clipService.updateClip(id, memberId, articleReq))
    }

    /**
     * 클립 조회수 API 입니다.
     * @param
     * @return
     */
    @GetMapping("/v2/count/clip/{id}")
    fun countClip(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L

        return ResponseEntity
            .ok()
            .body(clipService.clipViewCount(id, memberId))
        /*val cookies = request.cookies
        if (cookies == null) {
            postService.postViewCount(id)
            val cookie = Cookie("postViews", id.toString())
            cookie.maxAge = 60 * 60 * 24
            response.addCookie(cookie)
            return
        }

        cookies.forEach {
            if (it.name == "postViews") {
                val valueList = it.value.split("_")
                if (valueList.find { it == id.toString() } != null) {
                    return
                } else {
                    postService.postViewCount(id)
                    val cookie = Cookie("postViews", it.value + "_" + id.toString())
                    response.addCookie(cookie)
                    return
                }
            }
        }*/
    }

    /**
     * 클립 채택 API 입니다.
     * @param id 채택할 클립 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 성공여부에 대해 Code 값을 리턴합니다.
     */
    @PostMapping("/v2/adopt/clip/{id}")
    fun adoptClip(
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.adoptClip", " Param-> memberId= $memberId clipId= $id")

        return ResponseEntity
            .ok()
            .body(clipService.adoptClip(memberId, id))
    }

    /**
     * 추천 클립 리스트 API 입니다.
     * @return 클립을 리턴합니다.
     */
    @GetMapping("/v2/recommend/clips")
    fun getRecommendClips(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "20") size: Int,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("ClipController.getRecommendClips", "memberId= $memberId")
        return ResponseEntity
            .ok()
            .body(clipService.getRecommendClips(PageRequest.of(page, size), memberId))
    }

    /**
     * 클립 인코딩 완료 처리 API 입니다.
     * @return 처리 완료 메시지
     */
    @PostMapping("/v2/clip/encoding/complete")
    fun clipEncodingComplete(
        @RequestBody clipEncodingCompleteReq: ClipEncodingCompleteReq
    ): ResponseEntity<Any> {
        Log.out("ClipController.clipEncodingComplete", "clipEncodingCompleteReq= $clipEncodingCompleteReq")
        return ResponseEntity
            .ok()
            .body(clipService.clipEncodingComplete(clipEncodingCompleteReq))
    }

    /**
     * 팔로우 유저 클립 리스트 API 입니다.
     * @return 클립을 리턴합니다.
     */
    @GetMapping("/v2/following/clipList")
    fun getFollowingClips(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "20") size: Int,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ClipController.getFollowingClips", "memberId= $memberId")
        return ResponseEntity
            .ok()
            .body(clipService.getFollowingClips(PageRequest.of(page, size), memberId))
    }

    /**
     * 클립 타임라인 수정 API 입니다.
     * @return 클립을 리턴합니다.
     */
    @PutMapping("/v2/clipTimeline")
    fun updateClipTimeline(@RequestBody clipTimelineReq: ClipTimelineReq): ResponseEntity<Any> {
        Log.out("ClipController.updateClipTimeline", " Param-> clipTimelineReq= $clipTimelineReq")

        return ResponseEntity
            .ok()
            .body(clipService.updateClipTimeline(clipTimelineReq))
    }
}