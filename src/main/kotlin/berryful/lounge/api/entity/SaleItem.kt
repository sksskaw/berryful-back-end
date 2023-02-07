package berryful.lounge.api.entity

import javax.persistence.*

@Entity
@Table(name = "sale_item")
class SaleItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broadcast_id")
    var broadcast: Broadcast,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "price")
    var price: Long? = null,

    @Column(name = "img_url")
    var imgUrl: String? = null,
) : BaseEntity()