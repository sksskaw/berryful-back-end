package berryful.lounge.api.entity

import org.apache.commons.lang3.builder.ToStringBuilder
import org.springframework.data.annotation.CreatedDate
import java.io.Serializable
import java.time.Instant
import javax.persistence.*

@Table(name = "interested_article")
@Entity
@IdClass(InterestedArticleId::class)
class InterestedArticle(
    @CreatedDate
    var createAt: Instant,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    var article: Article,
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

data class InterestedArticleId(
    var member: Long = 0L,
    var article: Long = 0L,
) : Serializable