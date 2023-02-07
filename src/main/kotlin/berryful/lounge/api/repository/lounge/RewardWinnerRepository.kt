package berryful.lounge.api.repository.lounge

import berryful.lounge.api.entity.Article
import berryful.lounge.api.entity.RewardWinner
import org.springframework.data.jpa.repository.JpaRepository

interface RewardWinnerRepository : JpaRepository<RewardWinner, Long> {
    fun findByArticle(article: Article): RewardWinner?
}