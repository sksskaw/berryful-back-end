package berryful.lounge.api.data

data class DeviceReq(
    var memberId: Long,
    var notificationToken: String,
)

data class CheckUpdateVersionReq(
    var memberId: Long = 0,
    var currentVersion: String,
    var platformType: String,
)

data class CheckUpdateVersionRes(
    var resultCode: Int,
    var forceUpdate: Boolean?,
    var updateCheck: Boolean?,
)