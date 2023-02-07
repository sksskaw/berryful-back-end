package berryful.lounge.api.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 *  앱 버전별 강제 업데이트 여부
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "app_version")
@Entity
class AppVersion(
    @Column(name = "version")
    var version: String,

    @Column(name = "force_update")
    var forceUpdate: Boolean,

    @Column(name = "platform_type")
    var platformType: String,
) : BaseEntity()