package berryful.lounge.api.repository.lounge

import berryful.lounge.api.data.SearchAutoCompleteKeywordRes
import berryful.lounge.api.data.SearchAutoCompleteTagRes
import berryful.lounge.api.entity.Hashtag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface HashtagRepository : JpaRepository<Hashtag, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO hashtag (create_at, update_at, tagname, count) VALUES (NOW(6), NOW(6), :tagname, 1) ON DUPLICATE KEY UPDATE count = count + 1", nativeQuery = true)
    fun insertOrUpdateCount(@Param("tagname")tagname: String)

    @Query(value = "SELECT * FROM hashtag h WHERE MATCH(tagname) AGAINST(:tagname'*' IN BOOLEAN MODE) ORDER BY count DESC, LENGTH(tagname) LIMIT 10", nativeQuery = true)
    fun searchAutoCompleteTags(@Param("tagname")tagname: String?): List<SearchAutoCompleteTagRes>
}