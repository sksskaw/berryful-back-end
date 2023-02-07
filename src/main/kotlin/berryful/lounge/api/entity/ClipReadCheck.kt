package berryful.lounge.api.entity

import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable
import javax.persistence.*

@Table(name = "clip_read_check")
@Entity
@IdClass(ClipReadCheckId::class)
class ClipReadCheck(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @Id
    @Column(name = "article_id")
    var articleId: Long? = null,
)  {
    override fun toString(): String = ToStringBuilder.reflectionToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        val otherEntity = (other as? ClipReadCheck) ?: return false
        return this.member == otherEntity.member && this.articleId == otherEntity.articleId
    }

    override fun hashCode(): Int {
        val prime = 59
        val result = 1

        return result * prime + (articleId?.hashCode() ?: 43) + (member.id?.hashCode() ?: 43)
    }
}

data class ClipReadCheckId(
    var member: Long = 0L,
    var articleId: Long = 0L,
) : Serializable