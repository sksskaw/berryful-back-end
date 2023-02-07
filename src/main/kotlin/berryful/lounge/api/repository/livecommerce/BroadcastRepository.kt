package berryful.lounge.api.repository.livecommerce

import berryful.lounge.api.entity.Broadcast
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BroadcastRepository : JpaRepository<Broadcast, Long> {
    override fun findAll(pageable: Pageable): Page<Broadcast>
}