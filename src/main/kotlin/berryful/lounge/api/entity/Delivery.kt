package berryful.lounge.api.entity

import javax.persistence.*

@Entity
@Table(name = "delivery")
class Delivery(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "address")
    var address: String? = null,

    @Column(name = "post_code")
    var postCode: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: DeliveryStatus? = DeliveryStatus.ACTIVE,
) : BaseEntity()

enum class DeliveryStatus {
    ACTIVE, DELETE
}