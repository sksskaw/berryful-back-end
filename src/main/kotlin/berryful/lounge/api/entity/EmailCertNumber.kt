package berryful.lounge.api.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 *  이메일 인증번호 생성, 검증을 위한 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "email_cert_number")
@Entity
class EmailCertNumber(
    // 인증번호 6자리
    @Column(name = "cert_number")
    var certNumber: String,

    // 인증번호를 요청한 이메일
    @Column(name = "email")
    var email: String,
) : BaseEntity()