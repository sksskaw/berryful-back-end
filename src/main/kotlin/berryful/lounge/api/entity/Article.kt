package berryful.lounge.api.entity

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Formula
import java.time.Instant
import javax.persistence.*

/**
 *  포스트, 클립, 댓글 정보가 있는 중첩 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "article")
@Entity
class Article(
    // 부모 article_id
    @Column(name = "parent_id")
    var parentId: Long? = null,

    // 자식 articles
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "parentId")
    var childArticleList: MutableList<Article> = mutableListOf(),

    @Basic(fetch = FetchType.LAZY)
    @Formula("(select count(1) from article child where child.parent_id = id AND child.status = 'UNBLOCKED')")
    var childArticleCount: Int = 0,

    // 회원 테이블 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    // 컨텐츠 타입별 post = 제목, clip = null, comment = null, reply = null, report = 제목
    @Column(name = "title", columnDefinition = "TEXT")
    var title: String? = null,

    // 컨텐츠 타입별 post = 내용, clip = 캡션, comment = 내용, reply = 내용, report = 내용
    @Column(name = "content", columnDefinition = "TEXT")
    var content: String? = null,

    // 좋아요 수
    @Column(name = "thumbs_count")
    var thumbsCount: Int = 0,

    // 컨텐츠 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "article_type")
    var articleType: ArticleType? = null,

    // 미디어 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    var mediaType: MediaType? = null,

    // 클립 동영상 파일 경로
    @Column(name = "clip_url")
    var clipUrl: String? = null,

    // 컨텐츠 블라인드 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ArticleStatus? = ArticleStatus.UNBLOCKED,

    // 해시태그
    @Column(name = "hashtag", columnDefinition = "TEXT")
    var hashtag: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "article")
    var thumbsUpList: MutableList<ThumbsUp> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "article")
    var interestedArticleList: MutableList<InterestedArticle> = mutableListOf(),

    @Basic(fetch = FetchType.LAZY)
    @Formula("(select count(1) from interested_article ia where ia.article_id = id)")
    var interestedCount: Int = 0,

    // 조회수
    @Column(name = "views")
    var views: Long = 0,

    @Column(name = "clip_timeline")
    var clipTimeline: Long? = null,

    // 채택된 클립 id
    @Column(name = "adopt_clip_id")
    var adoptClipId: Long? = null,

    // 채택된 날짜
    @Column(name = "adopt_at")
    var adoptAt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    var category: ArticleCategory? = null,

    // 클립 에디터 영상 해석기 관련 데이터
    @Column(name = "object_strings", columnDefinition = "TEXT")
    var objectStrings: String? = null,

    // 채택된 클립 id
    @Column(name = "clip_encoding_check")
    var clipEncodingCheck: Int? = null,

    @Column(name = "challenge_start_at")
    var challengeStartAt: Instant? = null,

    @Column(name = "challenge_end_at")
    var challengeEndAt: Instant? = null,

    @Column(name = "challenge_banner_url")
    var challengeBannerUrl: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "article")
    var ChallengeRewardList: MutableList<ChallengeReward> = mutableListOf(),

    // 챌린지 가이드 클립 여부
    @Column(name = "guide_clip")
    var guideClip: Boolean? = null,

    // article 내용 수정 여부
    @Column(name = "modified")
    var modified: Boolean? = null,
) : BaseEntity()

enum class ArticleType {
    POST, CLIP, COMMENT, REPLY, CHALLENGE
}

enum class MediaType {
    VIDEO, PHOTO, TEXT
}

enum class ArticleStatus {
    BLOCKED, UNBLOCKED
}