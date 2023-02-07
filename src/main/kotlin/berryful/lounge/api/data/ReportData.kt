package berryful.lounge.api.data

data class ReportReq(
    var articleId: Long,
    var reportType: Int,
    var reportDescription: String? = null,
)

interface ReportRes{
    var uploaderNickname: String
    var articleType:String
    var reportType: Int
    var createAt: String
}