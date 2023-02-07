package berryful.lounge.api.externalApi.analyzeContentApi

import berryful.lounge.api.data.AnalyzeContentReq
import com.fasterxml.jackson.databind.ObjectMapper
import berryful.lounge.api.utils.Log
import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.json.simple.parser.JSONParser

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.charset.StandardCharsets

@Component
class AnalyzeContentApiClient(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
) {
    private val url = ""
    private val httpClient = HttpClients.createDefault()
    private val gson = Gson()
    private val HEADERS = arrayOf(
        BasicHeader("Accept", "application/json")
    )

    fun analyzeContent(req: AiReq): Any? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val parameters: HashMap<String, Any> = HashMap()
        parameters["memberId"] = req.memberId
        parameters["content"] = req.content

        val body = objectMapper.writeValueAsString(parameters)
        Log.out("AwsLambdaClient.clipEncoding() body: ", body)
        val entity = HttpEntity(body, headers)
        val url = "$url/analyze/content"
        return restTemplate.postForEntity(url, entity, AiRes::class.java).body!!
    }

    fun learningModel(id: Long, trainData: MultipartFile, testData: MultipartFile): Any {
        val httpPost = HttpPost("$url/learning/model/$id")
        httpPost.setHeaders(HEADERS)

        val trainDataFile = multipartFileConvertTofile(trainData, "train")
        val testDataFile = multipartFileConvertTofile(testData, "test")

        val httpEntity = MultipartEntityBuilder.create()
            .addBinaryBody("train_data", trainDataFile, ContentType.MULTIPART_FORM_DATA, trainDataFile.name)
            .addBinaryBody("test_data", testDataFile, ContentType.MULTIPART_FORM_DATA, testDataFile.name)
            .build()
        httpPost.entity = httpEntity

        val jsonParser = JSONParser()
        return jsonParser.parse(execute(httpPost))
    }

    private fun execute(httpPost: HttpPost): String? {
        try {
            httpClient.execute(httpPost).use { httpResponse ->
                val entity = httpResponse.entity
                return EntityUtils.toString(entity, StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun multipartFileConvertTofile(mFile: MultipartFile, type: String): File {
        val path = File("").absolutePath
        val file = File("$path/resource/$type.txt")
        mFile.transferTo(file)
        return file
    }
}

data class AiRes(
    var member_id: Long = 471,
    var post_id: Long = 0,
    var calculation_time_start: Long,
    var calculation_time_end: Long,
    var user_feedback: Long = 1,
    var ai_feedback: Double,
)

data class AiReq(
    val memberId: Long,
    val content: String
)