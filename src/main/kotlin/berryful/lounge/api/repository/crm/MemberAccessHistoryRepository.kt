package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.MemberAccessHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface MemberAccessHistoryRepository : JpaRepository<MemberAccessHistory, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO member_access_history (create_at, update_at, activity_type, member_id) VALUES (NOW(6), NOW(6), 'LOGOUT', :memberId)", nativeQuery = true)
    fun insertLogoutMemberAccessHistory(@Param("memberId")memberId: Long)

    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO member_access_history (create_at, update_at, activity_type, member_id) VALUES (NOW(6), NOW(6), 'APPOPEN', :memberId)", nativeQuery = true)
    fun insertAppOpenMemberAccessHistory(@Param("memberId")memberId: Long)
}