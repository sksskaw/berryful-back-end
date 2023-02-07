package berryful.lounge.api.entity

import javax.persistence.*

@Table(name = "member_access_history")
@Entity
class MemberAccessHistory(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type")
    var activityType: ActivityType,
): BaseEntity()

enum class ActivityType {
    LOGIN, LOGOUT, SIGNUP, LEAVE, APPOPEN
}