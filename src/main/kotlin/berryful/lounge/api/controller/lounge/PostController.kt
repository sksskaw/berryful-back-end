package berryful.lounge.api.controller.lounge

import berryful.lounge.api.data.*
import berryful.lounge.api.service.lounge.PostService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *  포스트 CRUD 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/lounge/v2")
class PostController(
    private val postService: PostService,
) {
    /**
     * 포스트 작성 API 입니다.
     * @param articleReq 포스트 작성에 필요한 데이터입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return DB에 생성된 article id를 리턴합니다.
     */
    @PostMapping("/post")
    fun createPost(@RequestBody articleReq: ArticleReq, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("PostController.createPost", " Param-> memberId= $memberId articleReq= $articleReq")

        return ResponseEntity
            .ok()
            .body(postService.createPost(memberId, articleReq))
    }

    /**
     * 포스트 리스트 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 요청된 페이지에 맞는 포스트 목록을 리턴합니다.
     */
    @GetMapping("/postList")
    fun getPostList(
        params: PostParams,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        //Log.out("PostController.getPostList", "memberId: $memberId Params-> $params")

        val page = params.page
        val size = params.size
        val pageRequestSort = Sort.by(Sort.Direction.DESC, params.sort)
        return ResponseEntity
            .ok()
            .body(postService.getPostList(PageRequest.of(page, size, pageRequestSort), memberId, params))
    }

    /**
     * 관심 포스트 리스트 API 입니다.
     * @param params 페이징, 정렬 관련 파라미터.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 요청된 페이지에 맞는 포스트 목록을 리턴합니다.
     */
    @GetMapping("pick/postList")
    fun getInterestedPostList(
        params: InterestedPostParams,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("PostController.getInterestedPostList", "memberId: $memberId Params-> $params")

        val page = params.page
        val size = params.size
        val pageRequestSort = Sort.by(Sort.Direction.DESC, params.sort)

        return ResponseEntity
            .ok()
            .body(postService.getInterestedPostList(PageRequest.of(page, size, pageRequestSort), memberId, params))
    }

    /**
     * 포스트 상세 API 입니다.
     * @param id 요청한 포스트 id 입니다.
     * @return 요청된 id 맞는 포스트를 리턴합니다.
     */
    @GetMapping("/post/{id}")
    fun getPostOne(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("PostController.getPostOne", "id= $id")

        return ResponseEntity
            .ok()
            .body(postService.getPostOne(id, memberId))
    }

    /**
     * 포스트 삭제 API 입니다.
     * @param id 요청한 포스트 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 삭제된 포스트의 id를 리턴합니다.
     */
    @DeleteMapping("/post/{id}")
    fun deletePost(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("PostController.deletePost", " Param-> memberId= $memberId id= $id")

        return ResponseEntity
            .ok()
            .body(postService.deletePost(memberId, id))
    }

    /**
     * 포스트 수정 API 입니다.
     * @param id 요청한 포스트 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @param articleReq 수정된 포스트 데이터입니다.
     * @return 수정된 포스트의 id를 리턴합니다.
     */
    @PutMapping("/post/{id}")
    fun updatePost(
        @PathVariable id: Long,
        request: HttpServletRequest,
        @RequestBody articleReq: ArticleReq
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("PostController.updatePost", " Param-> memberId= $memberId id= $id")

        return ResponseEntity
            .ok()
            .body(postService.updatePost(id, memberId, articleReq))
    }

    /**
     * 포스트 조회수 API 입니다.
     * @param
     * @return
     */
    @GetMapping("/count/post/{id}")
    fun countPost(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Any> {
        Log.out("PostController.countPost", " Param-> id= $id")

        return ResponseEntity
            .ok()
            .body(postService.postViewCount(id))
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
     * 관심 포스트 pick API 입니다.
     * @param id 요청한 포스트 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 수정된 포스트의 id를 리턴합니다.
     */
    @PatchMapping("/pick/post/{id}")
    fun pickPost(
        @RequestBody pickReq: PickReq,
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("PostController.pickPost", " Param-> memberId= $memberId id= $id action= ${pickReq.action}")

        return ResponseEntity
            .ok()
            .body(postService.pickPost(memberId, id, pickReq))
    }

    /**
     * Hot 포스트 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return Hot 포스트를 리턴합니다.
     */
    @GetMapping("/hot/post")
    fun getHotPostOne(request: HttpServletRequest, params: PostParams): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("PostController.getHotPostOne", "memberId= $memberId")

        return ResponseEntity
            .ok()
            .body(postService.getHotPost(memberId, params))
    }

    /**
     * 관심폭팔 포스트 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 관심폭팔 포스트를 리턴합니다.
     */
    @GetMapping("/best/pick/post")
    fun getBestInterestedPost(request: HttpServletRequest, params: PostParams): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("PostController.getBestInterestedPost", "memberId= $memberId")

        return ResponseEntity
            .ok()
            .body(postService.getBestInterestedPost(memberId, params))
    }

    /**
     * 카테고리 목록 API 입니다.
     * @return 카테고리를 리턴합니다.
     */
    @GetMapping("/categoryList")
    fun getCategoryList(): ResponseEntity<Any> {
        Log.out("PostController.getCategoryList", "")

        return ResponseEntity
            .ok()
            .body(postService.getCategoryList())
    }

    /**
     * 인기 포스트 목록 API 입니다.
     * @return 카테고리를 리턴합니다.
     */
    @GetMapping("/popular/postClips")
    fun getPopularPostList(request: HttpServletRequest): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("PostController.getPopularPostList", "")

        return ResponseEntity
            .ok()
            .body(postService.getPopularPostList(memberId))
    }
}