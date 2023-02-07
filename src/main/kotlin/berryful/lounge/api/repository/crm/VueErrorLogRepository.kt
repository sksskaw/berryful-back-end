package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.VueErrorLog
import org.springframework.data.jpa.repository.JpaRepository

interface VueErrorLogRepository : JpaRepository<VueErrorLog, Long> {
}