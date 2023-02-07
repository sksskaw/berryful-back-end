package berryful.lounge.api.entity

import org.hibernate.annotations.ColumnDefault
import javax.persistence.*

/**
 *  회원 정보 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "member")
@Entity
class Member(

    @Column(name = "email", unique = true)
    var email: String? = null,

    @Column(name = "nickname", unique = true)
    var nickname: String? = null,

    @Column(name = "uppercase_nickname", unique = true)
    var uppercaseNickname: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: MemberStatus? = MemberStatus.ACTIVE,

    @Column(name = "profile_path")
    var profilePath: String? = null,

    // null: 선택안됨, F: 여성, M: 남성
    @Column(name = "gender", length = 1)
    var gender: String? = null,

    @Column(name = "birthday")
    var birthday: String? = null,

    @Column(name = "agree_personal_info")
    var agreePersonalInfo: Boolean = false,

    @Column(name = "agree_service")
    var agreeService: Boolean = false,

    @Column(name = "agree_marketing")
    var agreeMarketing: Boolean = false,

    @Column(name = "sns_kakao", unique = true)
    var snsKakao: String? = null,

    @Column(name = "sns_apple", unique = true)
    var snsApple: String? = null,

    @Column(name = "sns_facebook", unique = true)
    var snsFacebook: String? = null,

    @Column(name = "sns_naver", unique = true)
    var snsNaver: String? = null,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "member")
    var articleList: MutableList<Article> = mutableListOf(),

    // 클립 재생여부 확인 리스트
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "member")
    var clipReadCheckList: MutableList<ClipReadCheck> = mutableListOf(),

    @Column(name = "notification_check")
    var notificationCheck: Boolean = true,

    @Column(name = "sns_type")
    var snsType: String? = null,

    @Column(name = "sns_phone_number")
    var snsPhoneNumber: String? = null,

    @Column(name = "sns_email")
    var snsEmail: String? = null,

    @Column(name = "profile_intro")
    var profileIntro: String? = null,

    @Column(name = "youtube_url")
    var youtubeUrl: String? = null,

    @Column(name = "instagram_url")
    var instagramUrl: String? = null,

    @Column(name = "blog_url")
    var blogUrl: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "follower")
    var memberFollowingList: MutableList<MemberFollow> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "following")
    var memberFollowerList: MutableList<MemberFollow> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "member")
    var cartList: MutableList<Cart> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "member")
    var deliveryList: MutableList<Delivery> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "member")
    @OrderBy(value = "id DESC")
    var orderList: MutableList<Order> = mutableListOf(),

    @Column(name = "push_berryful")
    @ColumnDefault("1")
    var pushBerryful: Boolean = true,

    @Column(name = "push_thumbs_up")
    @ColumnDefault("1")
    var pushThumbsUp: Boolean = true,

    @Column(name = "push_content_upload")
    @ColumnDefault("1")
    var pushContentUpload: Boolean = true,

    @Column(name = "push_adopt_clip")
    @ColumnDefault("1")
    var pushAdoptClip: Boolean = true,

    @Column(name = "push_interested_article")
    @ColumnDefault("1")
    var pushInterestedArticle: Boolean = true,

    @Column(name = "push_follow")
    @ColumnDefault("1")
    var pushFollow: Boolean = true,
    ) : BaseEntity()

enum class MemberStatus {
    ACTIVE, INACTIVE, LEAVE, SUSPENDED
}