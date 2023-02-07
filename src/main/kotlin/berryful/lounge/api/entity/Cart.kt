package berryful.lounge.api.entity

import javax.persistence.*

@Entity
@Table(name = "cart")
class Cart(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_item_id")
    var saleItem: SaleItem,

    @Column(name = "quantity")
    var quantity: Int? = null,
) : BaseEntity()