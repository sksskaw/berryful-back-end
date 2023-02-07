package berryful.lounge.api.repository.crm

import berryful.lounge.api.data.FollowersRes
import berryful.lounge.api.data.FollowingRes
import berryful.lounge.api.data.ReportRes
import berryful.lounge.api.entity.MemberFollow
import berryful.lounge.api.entity.MemberFollowId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface MemberFollowRepository : JpaRepository<MemberFollow, MemberFollowId> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO member_follow (follower_member_id, following_member_id) VALUES (:memberId, :followMemberId)", nativeQuery = true)
    fun insertMemberFollow(@Param("memberId") memberId: Long, @Param("followMemberId") followMemberId: Long): Int

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM member_follow WHERE follower_member_id= :memberId AND following_member_id= :followMemberId", nativeQuery = true)
    fun deleteMemberFollow(@Param("memberId") memberId: Long, @Param("followMemberId") followMemberId: Long): Int

    @Query(
        value = "SELECT m.id" +
                ",m.nickname" +
                ",m.profile_path profilePath" +
                ",IF(targetMember.following_member_id = loginMember.following_member_id,TRUE,FALSE) AS follow\n" +
                "FROM member m INNER JOIN member_follow targetMember \n" +
                "ON m.id = targetMember.following_member_id AND targetMember.follower_member_id = :targetMemberId\n" +
                "LEFT JOIN member_follow loginMember \n" +
                "ON targetMember.following_member_id = loginMember.following_member_id AND loginMember.follower_member_id = :loginMemberId\n" +
                "WHERE m.status = 'ACTIVE' AND m.id NOT IN(SELECT blocked_member_id FROM blocked_member WHERE member_id = :loginMemberId)" +
                "ORDER BY FIELD(targetMember.following_member_id,:loginMemberId) DESC, follow DESC",
        countQuery = "SELECT count(1)" +
                "FROM member m INNER JOIN member_follow targetMember \n" +
                "ON m.id = targetMember.following_member_id AND targetMember.follower_member_id = :targetMemberId \n" +
                "LEFT JOIN member_follow loginMember \n" +
                "ON targetMember.following_member_id = loginMember.following_member_id AND loginMember.follower_member_id = :loginMemberId \n" +
                "WHERE m.status = 'ACTIVE' AND m.id NOT IN(SELECT blocked_member_id FROM blocked_member WHERE member_id = :loginMemberId)",
        nativeQuery = true
    )
    fun findAllFollowing(pageable: Pageable, @Param("targetMemberId") targetMemberId: Long,  @Param("loginMemberId") loginMemberId: Long): Page<FollowingRes>

    @Query(
        value = "SELECT m.id" +
                ",m.nickname" +
                ",m.profile_path profilePath" +
                ",IF(targetMember.follower_member_id = loginMember.following_member_id,TRUE,FALSE) AS follow\n" +
                "FROM member m INNER JOIN member_follow targetMember \n" +
                "ON m.id = targetMember.follower_member_id AND targetMember.following_member_id = :targetMemberId\n" +
                "LEFT JOIN member_follow loginMember \n" +
                "ON targetMember.follower_member_id = loginMember.following_member_id AND loginMember.follower_member_id = :loginMemberId\n" +
                "WHERE m.status = 'ACTIVE'\n" +
                "ORDER BY FIELD(targetMember.follower_member_id, :loginMemberId) DESC, follow DESC",
        countQuery = "SELECT count(1)" +
                "FROM member m INNER JOIN member_follow targetMember \n" +
                "ON m.id = targetMember.follower_member_id AND targetMember.following_member_id = :targetMemberId\n" +
                "LEFT JOIN member_follow loginMember \n" +
                "ON targetMember.follower_member_id = loginMember.following_member_id AND loginMember.follower_member_id = :loginMemberId\n" +
                "WHERE m.status = 'ACTIVE'",
        nativeQuery = true
    )
    fun findAllFollowers(pageable: Pageable, @Param("targetMemberId") targetMemberId: Long,  @Param("loginMemberId") loginMemberId: Long): Page<FollowersRes>
}