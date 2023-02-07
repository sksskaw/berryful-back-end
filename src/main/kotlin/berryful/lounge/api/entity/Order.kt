package berryful.lounge.api.entity

import javax.persistence.*

@Entity
@Table(name = "`order`")
class Order(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    var delivery: Delivery,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "order")
    var orderItemList: MutableList<OrderItem> = mutableListOf(),
) : BaseEntity()