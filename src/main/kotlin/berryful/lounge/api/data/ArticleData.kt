package berryful.lounge.api.data

import berryful.lounge.api.entity.Article
import berryful.lounge.api.entity.ArticleStatus
import berryful.lounge.api.entity.ArticleType
import berryful.lounge.api.entity.MediaType
import com.fasterxml.jackson.annotation.JsonFormat
import com.querydsl.core.annotations.QueryProjection
import org.json.simple.JSONObject
import java.io.Serializable
import java.time.Instant

data class ClipEncodingCompleteReq(
    val clipId: Long = 0
)

data class ClipTimelineReq(
    var clipUrl: String,
    val clipTimeline: Long? = null
)

data class ArticleReq(
    var title: String? = null,
    var content: String? = null,
    var articleType: ArticleType,
    var clipUrl: String? = null,
    var hashtag: String? = null,
    var clipTimeline: String? = null,
    var category: Int? = null,
)

data class ClipReq(
    var content: String? = null,
    var articleType: ArticleType,
    var mediaType: MediaType? = null,
    var clipUrl: String? = null,
    var hashtag: String? = null,
    var clipTimeline: String? = null,
    var offsetStart: Long,
    var offsetEnd: Long,
    var objectStrings: JSONObject? = null,
)

data class V4ClipReq(
    var category: Int? = null,
    var content: String? = null,
    var articleType: ArticleType,
    var mediaType: MediaType? = null,
    var clipUrl: String? = null,
    var hashtag: String? = null,
    var clipTimeline: String? = null,
    var offsetStart: Long,
    var offsetEnd: Long,
    var objectStrings: JSONObject? = null,
)

data class CreatePostRes(
    var resultCode: Int,
    var post: PostRes,
)

data class UpdatePostRes(
    var resultCode: Int,
    var post: PostRes,
)

data class ArticleRes(
    var id: Long = -1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var createAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var updateAt: Instant? = null,
    var parentId: Long? = null,
    var memberId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var thumbsUp: Boolean,
    var thumbsCount: Int,
    var articleType: ArticleType,
    var clipUrl: String? = null,
    var memberNickname: String,
    var memberProfilePath: String? = null,
    var hashtag: String? = null,
)

data class PostRes(
    var id: Long = -1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var createAt: Instant,
    var memberId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var thumbsUp: Boolean,
    var thumbsCount: Int,
    var articleType: ArticleType,
    var memberNickname: String,
    var memberProfilePath: String? = null,
    var clips: Any? = null,
    var hashtag: String? = null,
    var status: ArticleStatus? = null,
    var pick: Boolean,
    var pickCount: Int? = null,
    var adoptClipId: Long? = null,
    var views: Long,
    var categoryId: Int?,
    var categoryName: String?,
) : Serializable

data class PopularPostRes(
    var id: Long = -1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var createAt: Instant,
    var memberId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var thumbsUp: Boolean,
    var thumbsCount: Int,
    var articleType: ArticleType,
    var memberNickname: String,
    var memberProfilePath: String? = null,
    var mainClipIndex: Int? = null,
    var clips: Any? = null,
    var hashtag: String? = null,
    var status: ArticleStatus? = null,
    var pick: Boolean,
    var pickCount: Int? = null,
    var adoptClipId: Long? = null,
    var views: Long,
    var categoryId: Int?,
    var categoryName: String?,
)

data class ClipRes(
    var id: Long = -1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var createAt: Instant,
    var parentId: Long? = null,
    var memberId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var categoryId: Int?,
    var categoryName: String?,
    var thumbsUp: Boolean,
    var thumbsCount: Int,
    var articleType: ArticleType,
    var mediaType: MediaType? = null,
    var clipUrl: String? = null,
    var memberNickname: String,
    var memberProfilePath: String? = null,
    var comments: Any? = null,
    var commentCount: Int = 0,
    var hashtag: String? = null,
    var clipTimeline: Long? = null,
    var clipReadCheck: Boolean? = null,
    var status: ArticleStatus? = null,
    var views: Long,
    var postId: Long? = null,
    var postTitle: String? = null,
    var postMemberId: Long? = null,
    var postMemberNickname: String? = null,
    var parentArticleType: ArticleType? = null,
    var objectStrings: String? = "",
) : Serializable

data class ChallengeClipRes(
    var id: Long = -1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var createAt: Instant,
    var parentId: Long? = null,
    var memberId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var categoryId: Int?,
    var categoryName: String?,
    var thumbsUp: Boolean,
    var thumbsCount: Int,
    var articleType: ArticleType,
    var mediaType: MediaType? = null,
    var clipUrl: String? = null,
    var memberNickname: String,
    var memberProfilePath: String? = null,
    var comments: Any? = null,
    var commentCount: Int = 0,
    var hashtag: String? = null,
    var clipTimeline: Long? = null,
    var clipReadCheck: Boolean? = null,
    var status: ArticleStatus? = null,
    var views: Long,
    var challengeId: Long? = null,
    var challengeTitle: String? = null,
    var parentArticleType: ArticleType,
    var objectStrings: String? = "",
    var guideClip: Boolean? = false,
    var winning: Boolean? = false,
)

data class PostClip @QueryProjection constructor(
    var post: Article? = null,
    var clip: Article,
)

data class ClipWithParent @QueryProjection constructor(
    var parent: Article? = null,
    var clip: Article,
)

data class CommentRes(
    var id: Long = -1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var createAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var updateAt: Instant? = null,
    var parentId: Long? = null,
    var memberId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var thumbsUp: Boolean,
    var thumbsCount: Int,
    var articleType: ArticleType,
    var clipUrl: String? = null,
    var memberNickname: String,
    var memberProfilePath: String? = null,
    var replys: Any? = null,
    var replyCount: Int = 0,
    var hashtag: String? = null,
)

data class PageableSetRes(
    var rowCount: Int,
    var rows: Any,
) : Serializable

data class RecommendClipRes(
    var clipCount: Int,
    var clips: Any,
)

data class ThumbsUpReq(
    var action: String,
)

data class ThumbsUpRes(
    val resultCode: Int,
    var thumbsCount: Int,
)

data class PickReq(
    var action: String,
)

data class PickRes(
    val resultCode: Int,
    var pickCount: Int,
)

interface SearchAutoCompleteKeywordRes{
    val keyword: String
    val count: Int
}

interface SearchAutoCompleteTagRes{
    val tagname: String
    val count: Int
}

data class CategoryRes(
    val id: Int,
    val name: String,
)

data class ChallengeBannerRes(
    val id: Long?,
    val bannerUrl: String?,
    val status: String?,
)

data class ChallengeRes(
    var id: Long = -1,
    var title: String? = null,
    var clips: Any? = null,
    var status: String? = null,
)

data class ChallengeDetailRes(
    var id: Long = -1,
    var title: String? = null,
    var content: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var startAt: Instant?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var endAt: Instant?,
    val bannerUrl: String?,
    var clips: Any? = null,
    var status: String? = null,
    var challengeRewards: MutableList<ChallengeRewardRes> = mutableListOf(),
)

data class ChallengeRewardRes(
    var prize: String?,
    var reward: String?,
    var numberOfWinners: Int?,
    var winners: MutableList<WinnersRes> = mutableListOf(),
)

data class WinnersRes(
    var id: Long?,
    var nickname: String?,
)

data class PostParams(
    val page: Int = 0,
    val size: Int = 10,
    val sort: String = "latestPost",
    val category: Int? = null,
    val keyword: String? = null,
    val hashtag: String? = null,
) : Serializable

data class ClipParams(
    val page: Int = 0,
    val size: Int = 10,
    val category: Int? = null,
    val keyword: String? = null,
    val hashtag: String? = null,
)

data class InterestedPostParams(
    val page: Int = 0,
    val size: Int = 10,
    val sort: String = "latestPost",
    val category: Int? = null,
)

data class ChallengeParams(
    val page: Int = 0,
    val size: Int = 10,
    val keyword: String? = null,
    val hashtag: String? = null,
)