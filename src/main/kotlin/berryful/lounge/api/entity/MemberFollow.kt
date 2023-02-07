package berryful.lounge.api.entity

import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable
import javax.persistence.*

/**
 *  회원 팔로우 매핑 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "member_follow")
@Entity
@IdClass(MemberFollowId::class)
class MemberFollow(

    // member 테이블 외래키
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_member_id")
    var follower: Member,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_member_id")
    var following: Member,
) {
    override fun toString(): String = ToStringBuilder.reflectionToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        val otherEntity = (other as? MemberFollow) ?: return false
        return this.follower == otherEntity.follower && this.following == otherEntity.following
    }

    override fun hashCode(): Int {
        val prime = 59
        val result = 1

        return result * prime + (follower.id?.hashCode() ?: 43) + (following.id?.hashCode() ?: 43)
    }
}

data class MemberFollowId(
    var follower: Long = 0L,
    var following: Long = 0L,
) : Serializable