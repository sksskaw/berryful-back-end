package berryful.lounge.api.controller.lounge

import berryful.lounge.api.service.lounge.SearchService
import berryful.lounge.api.utils.Log
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 *  라운지 검색 관련 컨트롤러 입니다.
 *  @author Taehoon Kim
 *  @version 2.0
 */
@RestController
@RequestMapping("/lounge/v2")
class SearchController(
    private val searchService: SearchService,
) {
    /**
     * 검색어 키워드 저장 API 입니다.
     */
    @PostMapping("/search")
    fun insertSearchKeyword(
        @RequestParam(value = "keyword", required = false) keyword: String?,
    ) {
        if (keyword == null || keyword == "" || keyword == "null" || keyword == "undefined") return
        Log.out("SearchController.insertSearchKeyword", "keyword= $keyword")

        searchService.insertSearchKeyword(keyword)
    }

    /**
     * 연관 검색어 자동완성 API 입니다.
     * @return 키워드에 맞는 자동완성 검색어를 리턴합니다.
     */
    @GetMapping("/search/autocomplete/keywords")
    fun searchAutoCompleteKeywords(
        @RequestParam(value = "keyword", required = false) keyword: String?,
    ): ResponseEntity<Any> {
        Log.out("SearchController.searchAutoCompleteKeywords", "keyword= $keyword")
        return ResponseEntity
            .ok()
            .body(searchService.searchAutoCompleteKeywords(keyword))
    }

    /**
     * 해시태그 자동완성 API 입니다.
     * @return 키워드에 맞는 자동완성 검색어를 리턴합니다.
     */
    @GetMapping("/search/autocomplete/tags")
    fun searchAutoCompleteTags(
        @RequestParam(value = "tagname", required = false) tagname: String?,
    ): ResponseEntity<Any> {
        Log.out("SearchController.searchKeywordAutoComplete", "tagname= $tagname")
        return ResponseEntity
            .ok()
            .body(searchService.searchAutoCompleteTags(tagname))
    }
}