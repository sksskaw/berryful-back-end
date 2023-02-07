package berryful.lounge.api.repository.lounge

import berryful.lounge.api.entity.ClipReadCheck
import berryful.lounge.api.entity.ClipReadCheckId
import org.springframework.data.jpa.repository.JpaRepository

interface ClipReadCheckRepository : JpaRepository<ClipReadCheck, ClipReadCheckId> {
    fun findAllByMemberId(memberId: Long): List<ClipReadCheck>
}