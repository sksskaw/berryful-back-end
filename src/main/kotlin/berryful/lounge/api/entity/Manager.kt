package berryful.lounge.api.entity

import javax.persistence.*

/**
 *  관리자 정보 테이블
 *  @author sumin Hong
 *  @version 2.0
 */
@Table(name = "Manager")
@Entity
class Manager(

    @Column(name = "manager_id", unique = true)
    var managerId: String,
    @Column(name = "password")
    var password: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ManagerStatus? = ManagerStatus.ACTIVE,
    @Enumerated(EnumType.STRING)
    @Column(name = "rule")
    var rule: ManagerRule? = ManagerRule.STAFF
) : BaseEntity()

enum class ManagerStatus {
    ACTIVE, LEAVE
}

enum class ManagerRule {
    ADMIN, MANAGER, STAFF
}