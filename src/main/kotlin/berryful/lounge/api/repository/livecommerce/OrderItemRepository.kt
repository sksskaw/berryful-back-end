package berryful.lounge.api.repository.livecommerce

import berryful.lounge.api.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItem, Long> {
}