package berryful.lounge.api.repository.lounge

import berryful.lounge.api.data.SearchAutoCompleteKeywordRes
import berryful.lounge.api.data.SearchAutoCompleteTagRes
import berryful.lounge.api.entity.SearchKeyword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SearchKeywordRepository : JpaRepository<SearchKeyword, Long> {
    @Query(
        value = "SELECT keyword, count(keyword) as count "+
                "FROM search_keyword "+
                "WHERE MATCH(keyword) AGAINST(:keyword'*' IN BOOLEAN MODE) "+
                "GROUP BY keyword "+
                "ORDER BY LENGTH(keyword), count DESC "+
                "LIMIT 10", nativeQuery = true
    )
    fun searchAutoCompleteKeyword(@Param("keyword") keyword: String?): List<SearchAutoCompleteKeywordRes>
}