package berryful.lounge.api.controller.livecommerce

import berryful.lounge.api.data.AnalyzeContentReq
import berryful.lounge.api.service.livecommerce.AnalyzeContentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class AnalyzeContentController(
    private val analyzeContentService: AnalyzeContentService
) {
    @PostMapping("/analyze/content")
    fun analyzeContent(
        @RequestBody analyzeContentReq: AnalyzeContentReq,
    ): ResponseEntity<Any> {
        return ResponseEntity
            .ok()
            .body(analyzeContentService.analyzeContent(analyzeContentReq))
    }

    @PostMapping("/learning/model/{id}")
    fun learningModel(
        @PathVariable id: Long,
        @RequestParam("train_data", required = true) trainData: MultipartFile,
        @RequestParam("test_data", required = true) testData: MultipartFile,
    ): ResponseEntity<Any> {
        return ResponseEntity
            .ok()
            .body(analyzeContentService.learningModel(id, trainData, testData))
    }
}