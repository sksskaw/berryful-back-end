package berryful.lounge.api.repository.livecommerce

import berryful.lounge.api.entity.SaleItem
import org.springframework.data.jpa.repository.JpaRepository

interface SaleItemRepository : JpaRepository<SaleItem, Long> {
}