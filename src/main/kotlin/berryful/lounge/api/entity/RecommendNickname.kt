package berryful.lounge.api.entity

import java.time.Instant
import javax.persistence.*

@Table(name = "recommend_nickname")
@Entity
class RecommendNickname(
    @Column(name = "nickname", unique = true)
    var nickname: String? = null,

    @Column(name = "used")
    var used: Int = 0,

    @Column(name = "used_at")
    var usedAt: Instant? = null,
) : BaseEntity()