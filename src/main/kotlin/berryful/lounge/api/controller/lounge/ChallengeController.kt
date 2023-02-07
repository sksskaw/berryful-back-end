package berryful.lounge.api.controller.lounge

import berryful.lounge.api.data.ChallengeParams
import berryful.lounge.api.data.ClipReq
import berryful.lounge.api.service.lounge.ChallengeService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  챌린지 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/lounge/v2")
class ChallengeController(
    private val challengeService: ChallengeService,
) {
    /**
     * 챌린지 배너 API 입니다.
     * @return 요청된 페이지에 맞는 포스트 목록을 리턴합니다.
     */
    @GetMapping("/challenge/bannerList")
    fun getChallengeBanner(): ResponseEntity<Any> {
        Log.out("ChallengeController.challenge-banner", "")
        return ResponseEntity
            .ok()
            .body(challengeService.getChallengeBanner())
    }

    @PostMapping("/challenge/clip/{challengeId}")
    fun createChallengeClip(
        @PathVariable challengeId: Long,
        @RequestBody clipReq: ClipReq,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()
        Log.out("ChallengeController.createChallengeClip", " Param-> challengeId= $challengeId memberId= $memberId clipReq= $clipReq")

        return ResponseEntity
            .ok()
            .body(challengeService.createChallengeClip(memberId, challengeId, clipReq))
    }

    /**
     * 챌린지 목록 API 입니다.
     * @return 요청된 페이지에 맞는 포스트 목록을 리턴합니다.
     */
    @GetMapping("/challengeList")
    fun getChallengeList(
        params: ChallengeParams,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("getChallengeList.getChallengeList", "memberId: $memberId Params-> $params")

        val page = params.page
        val size = params.size

        return ResponseEntity
            .ok()
            .body(challengeService.getChallengeList(PageRequest.of(page, size), memberId, params))
    }

    /**
     * 챌린지 목록 API 입니다.
     * @return 요청된 페이지에 맞는 포스트 목록을 리턴합니다.
     */
    @GetMapping("/challenge/{challengeId}")
    fun getChallengeOne(
        @PathVariable challengeId: Long,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val memberId =
            if (request.userPrincipal != null)
                request.userPrincipal!!.name.toLong()
            else 0L
        Log.out("getChallengeList.getChallengeOne", "memberId: $memberId challengeId: $challengeId")
        return ResponseEntity
            .ok()
            .body(challengeService.getChallengeOne(memberId, challengeId))
    }
}