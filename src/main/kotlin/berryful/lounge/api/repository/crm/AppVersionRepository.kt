package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.AppVersion
import org.springframework.data.jpa.repository.JpaRepository

interface AppVersionRepository : JpaRepository<AppVersion, Long> {
    fun findFirstByForceUpdateAndPlatformTypeOrderByCreateAtDesc(forceUpdate:Boolean, platformType: String): AppVersion?
}