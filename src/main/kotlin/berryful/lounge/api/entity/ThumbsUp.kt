package berryful.lounge.api.entity

import org.apache.commons.lang3.builder.ToStringBuilder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant
import javax.persistence.*

/**
 *  좋아요 매핑 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "thumbs_up")
@Entity
@IdClass(ThumbsUpId::class)
class ThumbsUp(
    @CreatedDate
    var createAt: Instant,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    var article: Article,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,
) {
    override fun toString(): String = ToStringBuilder.reflectionToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        val otherEntity = (other as? ThumbsUp) ?: return false
        return this.article == otherEntity.article && this.member == otherEntity.member
    }

    override fun hashCode(): Int {
        val prime = 59
        val result = 1

        return result * prime + (article.id?.hashCode() ?: 43) + (member.id?.hashCode() ?: 43)
    }
}

data class ThumbsUpId(
    var article: Long = 0L,
    var member: Long = 0L,
) : Serializable