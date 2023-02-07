package berryful.lounge.api.service.lounge

import berryful.lounge.api.entity.SearchKeyword
import berryful.lounge.api.repository.lounge.HashtagRepository
import berryful.lounge.api.repository.lounge.SearchKeywordRepository
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val hashtagRepository: HashtagRepository,
    private val searchKeywordRepository: SearchKeywordRepository,
) {
    fun insertSearchKeyword(keyword: String) {
        searchKeywordRepository.save(SearchKeyword(keyword))
    }

    fun searchAutoCompleteTags(tagname: String?): Any {
        return hashtagRepository.searchAutoCompleteTags(tagname)
    }

    fun searchAutoCompleteKeywords(keyword: String?): Any {
        return searchKeywordRepository.searchAutoCompleteKeyword(keyword)
    }
}