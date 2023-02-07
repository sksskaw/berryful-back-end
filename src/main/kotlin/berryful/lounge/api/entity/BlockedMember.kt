package berryful.lounge.api.entity

import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable
import javax.persistence.*

@Table(name = "blocked_member")
@Entity
@IdClass(BlockedMemberId::class)
class BlockedMember(

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @Id
    @Column(name = "blocked_member_id")
    var blockedMemberId: Long,
) {
    override fun toString(): String = ToStringBuilder.reflectionToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        val otherEntity = (other as? BlockedMember) ?: return false
        return this.member == otherEntity.member && this.blockedMemberId == otherEntity.blockedMemberId
    }

    override fun hashCode(): Int {
        val prime = 59
        val result = 1

        return result * prime + (member.id.hashCode()) + (blockedMemberId.hashCode())
    }
}

data class BlockedMemberId(
    var member: Long = 0L,
    var blockedMemberId: Long = 0L,
) : Serializable