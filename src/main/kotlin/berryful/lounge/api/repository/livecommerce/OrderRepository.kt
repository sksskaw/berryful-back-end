package berryful.lounge.api.repository.livecommerce

import berryful.lounge.api.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
}