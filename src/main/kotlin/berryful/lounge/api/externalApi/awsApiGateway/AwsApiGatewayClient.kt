package berryful.lounge.api.externalApi.awsApiGateway

import berryful.lounge.api.utils.Log
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.*
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AwsApiGatewayClient(
    private val props: AwsApiGatewayAttributes,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
) {
    @Async
    fun clipEncoding(req: ClipEncodingReq) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val parameters: HashMap<String, Any> = HashMap()
        parameters["BucketFrom"] = props.BucketFrom
        parameters["BucketTo"] = props.BucketTo
        req.Filename?.also { parameters["Filename"] = "$it.mp4" }
        req.StartTimecode?.also { parameters["StartTimecode"] = formatMillisecondToString(it) }
        req.EndTimecode?.also { parameters["EndTimecode"] = formatMillisecondToString(it) }
        parameters["clipId"] = req.clipId

        val body = objectMapper.writeValueAsString(parameters)
        Log.out("AwsLambdaClient.clipEncoding() body: ", body)
        val entity = HttpEntity(body, headers)
        val url = props.berryfulEncodingEndpoint + "prod/create"
        restTemplate.postForEntity(url, entity, ClipEncodingRes::class.java).body!!
    }

    @Async("deleteMemberS3FilesExecutor")
    fun deleteClipS3Files(req: DeleteClipReq) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val parameters: HashMap<String, Any> = HashMap()
        parameters["clipEncodedBucketName"] = props.clipEncodedBucketName
        parameters["clipCoverBucketName"] = props.clipCoverBucketName
        req.clipUrl?.also { parameters["clipUrl"] = it }
        req.adoptCheck?.also { parameters["adoptCheck"] = it }

        val body = objectMapper.writeValueAsString(parameters)
        Log.out("AwsLambdaClient.deleteClipS3Files() body: ", body)
        val entity = HttpEntity(body, headers)
        val url = props.deleteS3ResourceEndpoint + "s3/clip"
        restTemplate.exchange(url, HttpMethod.DELETE, entity, DeleteClipRes::class.java).body
    }

    // 클립 s3 벌크 삭제
    @Async("deleteMemberS3FilesExecutor")
    fun deleteAllClipS3Files(req: DeleteClipsReq) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val parameters: HashMap<String, Any> = HashMap()
        parameters["clipEncodedBucketName"] = props.clipEncodedBucketName
        parameters["clipCoverBucketName"] = props.clipCoverBucketName
        req.clips.also { parameters["clips"] = it }

        val body = objectMapper.writeValueAsString(parameters)
        Log.out("AwsLambdaClient.deleteAllClipS3Files() body: ", body)
        val entity = HttpEntity(body, headers)
        val url = props.deleteS3ResourceEndpoint + "s3/clips"
        restTemplate.exchange(url, HttpMethod.DELETE, entity, DeleteClipRes::class.java).body
    }

    @Async("deleteMemberS3FilesExecutor")
    fun deleteMemberProfileS3File(req: DeleteMemberProfileReq) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val parameters: HashMap<String, Any> = HashMap()
        parameters["bucketName"] = props.profilesBucketName
        req.profileUrl?.also { parameters["profileUrl"] = it.substring(it.length - 36, it.length) }

        val body = objectMapper.writeValueAsString(parameters)
        Log.out("AwsLambdaClient.deleteMemberProfileS3File() body: ", body)
        val entity = HttpEntity(body, headers)
        val url = props.deleteS3ResourceEndpoint + "s3/member-profile"
        restTemplate.exchange(url, HttpMethod.DELETE, entity, DeleteMemberProfileRes::class.java).body
    }

    fun formatMillisecondToString(millisecond: Long): String {
        val hours: Long = millisecond / 1000 / 60 / 60 % 24
        val minutes: Long = millisecond / 1000 / 60 % 60
        val seconds: Long = millisecond / 1000 % 60
        val milliseconds = (millisecond % 1000) / 10
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, milliseconds)
    }
}

data class ClipEncodingReq(
    var Filename: String? = null,
    var StartTimecode: Long? = null,
    var EndTimecode: Long? = null,
    var clipId: Long,
)

data class ClipEncodingRes(
    var statusCode: Int? = null,
    var body: String? = null,
)

data class DeleteClipReq(
    var bucketName: String? = null,
    var clipUrl: String? = null,
    var adoptCheck: Boolean? = null
)

data class DeleteClipsReq(
    var bucketName: String? = null,
    var clips: MutableList<DeleteClip> = mutableListOf(),
)

data class DeleteClip(
    var clipUrl: String? = null,
    var adoptCheck: Boolean? = null
)

data class DeleteClipRes(
    var body: String? = null,
)

data class DeleteMemberProfileReq(
    var bucketName: String? = null,
    var profileUrl: String? = null,
)

data class DeleteMemberProfileRes(
    var body: String? = null,
)