package berryful.lounge.api.repository.livecommerce

import berryful.lounge.api.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository

interface CartRepository : JpaRepository<Cart, Long> {
}