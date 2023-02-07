package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.Device
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

interface DeviceRepository : JpaRepository<Device, Long> {
    fun findByNotificationToken(token: String): Device?
    fun findByMemberIdAndNotificationToken(memberId: Long, token: String): Device?
    fun findAllByMemberId(memberId: Long): List<Device>

    @Transactional
    fun deleteByNotificationToken(token: String)
    @Transactional
    fun deleteAllByNotificationTokenIn(token: List<String>)
}