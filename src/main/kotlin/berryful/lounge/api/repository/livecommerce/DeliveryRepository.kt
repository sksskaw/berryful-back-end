package berryful.lounge.api.repository.livecommerce

import berryful.lounge.api.entity.Delivery
import org.springframework.data.jpa.repository.JpaRepository

interface DeliveryRepository : JpaRepository<Delivery, Long> {
}