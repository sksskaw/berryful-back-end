package berryful.lounge.api.entity

import javax.persistence.*

/**
 *  회원이 사용하는 디바이스 정보가 있는 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "Device")
@Entity
class Device(

    @Column(name = "member_id")
    var memberId: Long? = null,

    @Column(name = "notification_token")
    var notificationToken: String,

    // 1: IOS, 2: ANDROID, 3: ETC
    @Column(name = "platform_type")
    var platformType: Int? = null,

    @Column(name = "platform_name")
    var platformName: String? = null,

    @Column(name = "platform_version")
    var platformVersion: String? = null,

    @Column(name = "device_info")
    var deviceInfo: String? = null,

    @Column(name = "device_name")
    var deviceName: String? = null,

    @Column(name = "app_version")
    var appVersion: String? = null
) : BaseEntity()