package berryful.lounge.api.repository.lounge

import berryful.lounge.api.entity.ArticleCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ArticleCategoryRepository : JpaRepository<ArticleCategory, Int> {
    @Query(value = "SELECT * FROM article_category WHERE weight IS NOT NULL ORDER BY weight", nativeQuery = true)
    fun findAllCategory(): List<ArticleCategory>
}