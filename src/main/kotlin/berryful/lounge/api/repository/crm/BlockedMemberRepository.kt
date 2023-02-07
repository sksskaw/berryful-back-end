package berryful.lounge.api.repository.crm

import berryful.lounge.api.data.BlockedListRes
import berryful.lounge.api.entity.BlockedMember
import berryful.lounge.api.entity.BlockedMemberId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface BlockedMemberRepository : JpaRepository<BlockedMember, BlockedMemberId> {
    fun findAllByMemberId(memberId: Long): List<BlockedMember>
    fun findAllByMemberId(pageable: Pageable, memberId: Long): Page<BlockedMember>

    @Query(value = "SELECT bm.blocked_member_id blockedMemberId" +
                   ",m.nickname nickname" +
                   ",m.profile_path profilePath " +
                   "FROM blocked_member bm INNER JOIN member m ON bm.blocked_member_id = m.id " +
                   "WHERE bm.member_id = :memberId",
        countQuery = "SELECT count(*) " +
                     "FROM blocked_member bm INNER JOIN member m ON bm.blocked_member_id = m.id " +
                     "WHERE bm.member_id = :memberId",
        nativeQuery = true)
    fun findAllBlockedListByMemberId(pageable: Pageable, @Param("memberId")memberId: Long): Page<BlockedListRes>

    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO blocked_member (member_id, blocked_member_id) VALUES (:memberId, :blockedMemberId)", nativeQuery = true)
    fun insertBlockedMember(@Param("memberId")memberId: Long, @Param("blockedMemberId")blockedMemberId: Long): Int

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM blocked_member WHERE member_id= :memberId AND blocked_member_id= :blockedMemberId", nativeQuery = true)
    fun deleteBlockedMember(@Param("memberId")memberId: Long, @Param("blockedMemberId")blockedMemberId: Long): Int
}