package berryful.lounge.api.repository.crm

import berryful.lounge.api.data.RecommendNicknameWithId
import berryful.lounge.api.entity.RecommendNickname
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface RecommendNicknameRepository : JpaRepository<RecommendNickname, Long> {
    @Query(value = "SELECT id, nickname FROM recommend_nickname WHERE used = 0 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    fun findRandRecommendNicknameId(): RecommendNicknameWithId?

    @Transactional
    @Modifying
    @Query(value = "UPDATE recommend_nickname SET used = 1, used_at = now(6) WHERE id = :id", nativeQuery = true)
    fun updateRecommendNicknameUsed(@Param("id")id: Long)
}