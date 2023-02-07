package berryful.lounge.api.controller.crm

import berryful.lounge.api.data.FollowReq
import berryful.lounge.api.data.PushSettingReq
import berryful.lounge.api.data.UpdateMemberInfoReq
import berryful.lounge.api.service.crm.MemberService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  회원 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/crm/v2")
class MemberController(
    private val memberService: MemberService
) {
    /**
     * 회원 정보 수정 API 입니다.
     * @param updateMemberInfoReq 요청된 회원 id클립 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PutMapping("/member")
    fun updateMemberInfo(
        @RequestBody updateMemberInfoReq: UpdateMemberInfoReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.updateMemberInfo", "$updateMemberInfoReq")
        return ResponseEntity
            .ok()
            .body(memberService.updateMemberInfo(updateMemberInfoReq, memberId))
    }

    /**
     * 회원 탈퇴 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @DeleteMapping("/member")
    fun deleteMemberInfo(request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.deleteMemberInfo", "memberId: $memberId")
        return ResponseEntity
            .ok()
            .body(memberService.deleteMember(memberId))
    }

    /**
     * 회원피트 메인 정보 API 입니다.
     * @param id 회원 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/memberInfo/{id}")
    fun getMemberInfo(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("MemberController.getMemberInfo", "id: $id memberId: $memberId")
        return ResponseEntity
            .ok()
            .body(memberService.getMemberInfo(id, memberId))
    }

    /**
     * 회원 차단 API 입니다.
     * @param blockMemberId 차단할 회원 ID
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PostMapping("/member/block/{blockMemberId}")
    fun blockMember(@PathVariable blockMemberId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.blockMember", "memberId: $memberId blockMemberId: $blockMemberId")
        return ResponseEntity
            .ok()
            .body(memberService.blockMember(memberId, blockMemberId))
    }

    /**
     * 회원 차단 해제 API 입니다.
     * @param unblockMemberId 차단 해제할 회원 ID
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PostMapping("/member/unblock/{unblockMemberId}")
    fun unblockMember(@PathVariable unblockMemberId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.unblockMember", "memberId: $memberId blockMemberId: $unblockMemberId")
        return ResponseEntity
            .ok()
            .body(memberService.unblockMember(memberId, unblockMemberId))
    }

    /**
     * 회원 차단 리스트 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/member/blockedList")
    fun getBlockedList(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.getBlockedList", "memberId: $memberId")

        return ResponseEntity
            .ok()
            .body(memberService.getBlockedList(PageRequest.of(page, size), memberId))
    }

    /**
     * 최초 앱 실행 초기화 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/member/init")
    fun memberActivityInit(request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.memberActivityInit", "memberId: $memberId")

        return ResponseEntity
            .ok()
            .body(memberService.memberActivityInit(memberId))
    }

    /**
     * 회원 피트 페이지 업로드한 콘텐츠 목록 API 입니다.
     * @param id 회원 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/member/uploadedList/{id}")
    fun getUploadedList(
        @RequestParam params: MutableMap<String, String>,
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("MemberController.getUploadedList", "memberId: $memberId id: $id Params-> $params")
        return ResponseEntity
            .ok()
            .body(memberService.getUploadedList(id, memberId, params))
    }

    /**
     * 내가 좋아요한 콘텐츠 목록 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/member/likedList/{articleType}")
    fun getLikedList(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        @PathVariable articleType: String,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.getUploadedList", "memberId: $memberId")

        return ResponseEntity
            .ok()
            .body(memberService.getLikedList(PageRequest.of(page, size), memberId, articleType))
    }

    /**
     * 팔로우 처리 API 입니다.
     * @param id 팔로우 처리 할 member id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return DB에 생성된 article id를 리턴합니다.
     */
    @PatchMapping("/follow/{id}")
    fun followMember(@RequestBody followReq: FollowReq, @PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ThumbsUpController.thumbsUpArticle", " Param-> memberId= $memberId followMemberId= $id action= ${followReq.action}")

        return ResponseEntity
            .ok()
            .body(memberService.followMember(memberId, id, followReq))
    }

    /**
     * 팔로잉 목록 API 입니다.
     * @param id 회원 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/member/following/{id}")
    fun getFollowingList(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("MemberController.getFollowingList", "memberId: $memberId id: $id")

        return ResponseEntity
            .ok()
            .body(memberService.getFollowingList(PageRequest.of(page, size), id, memberId))
    }

    /**
     * 팔로워 목록 API 입니다.
     * @param id 회원 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @GetMapping("/member/followers/{id}")
    fun getFollowerList(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("MemberController.getFollowerList", "memberId: $memberId id: $id")

        return ResponseEntity
            .ok()
            .body(memberService.getFollowerList(PageRequest.of(page, size), id, memberId))
    }

    /**
     * 팔로워 목록 삭제 API 입니다.
     * @param id 팔로워 목록에서 삭제할 회원 id 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @DeleteMapping("/member/follower/{id}")
    fun deleteFollower(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.getFollowerList", "memberId: $memberId deleteMemberId: $id")

        return ResponseEntity
            .ok()
            .body(memberService.deleteFollower(memberId, id))
    }

    /**
     * 푸시 알림 설정 API 입니다.
     * @param request JWT Security 인증 시 userPrincipal에 저장된 memberId를 가져오기 위한 데이터입니다.
     * @return 결과에 따른 결과코드를 리턴합니다.
     */
    @PutMapping("/member/push/setting")
    fun pushSetting(@RequestBody pushSettingReq: PushSettingReq, request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("MemberController.pushSetting", "memberId: $memberId pushSettingReq: $pushSettingReq")

        return ResponseEntity
            .ok()
            .body(memberService.pushSetting(memberId, pushSettingReq))
    }
}