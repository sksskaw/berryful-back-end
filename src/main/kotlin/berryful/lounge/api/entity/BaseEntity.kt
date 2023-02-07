package berryful.lounge.api.entity

import org.apache.commons.lang3.builder.ToStringBuilder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant
import javax.persistence.*

/**
 *  모든 테이블이 상속받는 기본 Entity
 *  @author Taehoon Kim
 *  @version 2.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    // 데이터 생성 시간
    @CreatedDate
    lateinit var createAt: Instant

    // 데이터 수정 시간
    @LastModifiedDate
    lateinit var updateAt: Instant

    override fun toString(): String = ToStringBuilder.reflectionToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        val otherEntity = (other as? BaseEntity) ?: return false
        return this.id == otherEntity.id
    }

    override fun hashCode(): Int {
        val prime = 59
        val result = 1

        return result * prime + (id?.hashCode() ?: 43)
    }
}