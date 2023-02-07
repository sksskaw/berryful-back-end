package berryful.lounge.api.entity

import javax.persistence.*

/**
 *  휴대폰 인증번호 생성, 검증을 위한 테이블
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "phone_cert_number")
@Entity
class PhoneCertNumber(

    // 인증번호 6자리
    @Column(name = "cert_number")
    var certNumber: String,

    // 인증번호를 요청한 휴대폰 번호
    @Column(name = "phone_number")
    var phoneNumber: String,
) : BaseEntity()