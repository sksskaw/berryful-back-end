package berryful.lounge.api.repository.lounge

import berryful.lounge.api.entity.InterestedArticle
import berryful.lounge.api.entity.InterestedArticleId
import org.springframework.data.jpa.repository.JpaRepository

interface InterestedArticleRepository : JpaRepository<InterestedArticle, InterestedArticleId> {
}