package berryful.lounge.api.entity

import javax.persistence.*

@Entity
@Table(name = "order_item")
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_item_id")
    var saleItem: SaleItem,

    @Column(name = "broadcast_title")
    var broadcastTitle: String? = null,

    @Column(name = "quantity")
    var quantity: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: OrderItemStatus? = null,
) : BaseEntity()

enum class OrderItemStatus {
    PREPARE,
    REQUEST_RETURN,
    RETURN_COMPLETE,
    CANCEL,
    COMPLETE
}