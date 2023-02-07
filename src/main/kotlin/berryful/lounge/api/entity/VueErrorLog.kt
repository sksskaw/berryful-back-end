package berryful.lounge.api.entity

import javax.persistence.*

@Table(name = "vue_error_log")
@Entity
class VueErrorLog(
    @Column(name = "member_id")
    var memberId: Long? = 0,

    @Column(name = "module")
    var module: String? = null,

    @Column(name = "error_code")
    var errorCode: Int? = null,

    @Column(name = "error_msg", columnDefinition = "TEXT")
    var errorMsg: String? = null,
): BaseEntity()