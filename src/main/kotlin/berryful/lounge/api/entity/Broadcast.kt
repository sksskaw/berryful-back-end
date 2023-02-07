package berryful.lounge.api.entity

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "broadcast")
class Broadcast(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    var manager: Manager,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "channel_id")
    var channelId: String,

    @Column(name = "rtmp")
    var rtmp: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: BroadcastStatus? = null,

    @Column(name = "reservation_start_at")
    var reservationStartAt: Instant? = null,

    @Column(name = "cover_url")
    var coverUrl: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "broadcast")
    var saleItemList: MutableList<SaleItem> = mutableListOf(),
) : BaseEntity()

enum class BroadcastStatus {
    INIT, LIVE, RESERVATION
}