package berryful.lounge.api.repository.lounge.article

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.Article
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ArticleDslRepository {
    // POST
    fun findAllPost(pageable: Pageable, memberId: Long, params: PostParams): Page<PostRes>
    fun findAllPost(pageable: Pageable, params: PostParams): Page<PostRes>

    fun findPost(id: Long, memberId: Long): PostRes?
    fun findPost(id: Long): PostRes?

    fun findAllInterestedPost(pageable: Pageable, memberId: Long, params: InterestedPostParams): Page<PostRes>
    fun findAllLikedPostList(pageable: Pageable, memberId: Long): Page<PostRes>

    fun findAllUploadedPost(pageable: Pageable, targetMemberId: Long, loginMemberId: Long, sort: String): Page<PostRes>
    fun findAllUploadedPost(pageable: Pageable, targetMemberId: Long, sort: String): Page<PostRes>

    fun findHotPost(params: PostParams, memberId: Long): PostRes?
    fun findHotPost(params: PostParams): PostRes?

    fun findBestInterestedPost(params: PostParams, memberId: Long): PostRes?
    fun findBestInterestedPost(params: PostParams): PostRes?

    fun findPopularPost(memberId: Long): List<PopularPostRes>
    fun findPopularPost(): List<PopularPostRes>

    // CLIP
    fun findAllClip(postId: Long, memberId: Long): List<Article> // 질문 클립 목록

    fun findAllClip(pageable: Pageable, memberId: Long): Page<Any> // 라운지 최신 클립 목록
    fun findAllClip(pageable: Pageable): Page<Any>

    fun findAllLikedClipList(pageable: Pageable, memberId: Long): Page<Any>

    fun findAllUploadedClip(pageable: Pageable, targetMemberId: Long, loginMemberId: Long): Page<Any>
    fun findAllUploadedClip(pageable: Pageable, targetMemberId: Long): Page<Any>

    fun findBasicRecommendClips(pageable: Pageable, startAt: Long, endAt: Long): Page<Any>
    fun findPersonalizeRecommendClips(pageable: Pageable, memberId: Long, startAt: Long, endAt: Long): Page<Any>

    fun findFollowingClips(pageable: Pageable, memberId: Long): Page<Any>

    fun findPopularClip(pageable: Pageable, memberId: Long, params: ClipParams): Page<Any>
    fun findPopularClip(pageable: Pageable, params: ClipParams): Page<Any>

    // CHALLENGE
    fun findAllChallenge(pageable: Pageable, memberId: Long, params: ChallengeParams): Page<ChallengeRes>
    fun findAllChallenge(pageable: Pageable, params: ChallengeParams): Page<ChallengeRes>
    fun findChallenge(challengeId: Long, memberId: Long): ChallengeDetailRes?
    fun findChallenge(challengeId: Long): ChallengeDetailRes?

    // COMMENT
    fun findAllComment(pageable: Pageable, clipId: Long, memberId: Long): Page<CommentRes>
    fun findAllComment(pageable: Pageable, clipId: Long): Page<CommentRes>

    // REPLY
    fun findAllReply(commentId: Long, memberId: Long): List<ArticleRes>
    fun findAllReply(commentId: Long): List<ArticleRes>
}