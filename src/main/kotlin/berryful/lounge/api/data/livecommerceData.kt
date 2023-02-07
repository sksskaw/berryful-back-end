package berryful.lounge.api.data

import berryful.lounge.api.entity.BroadcastStatus
import berryful.lounge.api.entity.OrderItemStatus
import berryful.lounge.api.externalApi.analyzeContentApi.AiRes
import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.Instant

data class BroadcastRes(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val channelId: String? = null,
    val rtmp: String,
    val status: BroadcastStatus? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val reservationStartAt: Instant? = null,
    val coverUrl: String? = null,
    var saleItems: MutableList<SaleItemRes> = mutableListOf(),
) : Serializable

data class SaleItemRes(
    val id: Long? = null,
    val broadcastId: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val imgUrl: String? = null,
) : Serializable

data class CartReq(
    val saleItemId: Long? = null,
    val quantity: Int? = null,
)

data class CartRes(
    val broadcastTitle: String? = null,
    val cartItems: MutableList<OrderItemRes> = mutableListOf(),
)

data class OrderItemRes(
    val id: Long? = null,
    val cartId: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val quantity: Int? = null,
    val price: Long? = null,
    val imgUrl: String? = null,
    val status: OrderItemStatus? = null,
)

data class UpdateCart(
    val quantity: Int? = null,
)

data class DeliveryReq(
    val title: String? = null,
    val address: String? = null,
    val postCode: String? = null,
)

data class DeliveryRes(
    val id: Long? = null,
    val title: String? = null,
    val address: String? = null,
    val postCode: String? = null,
)

data class AddDeliveryRes(
    val resultCode: Int,
    val id: Long? = null,
)

data class OrderReq(
    val deliveryId: Long? = null,
    val saleItems: MutableList<OrderItemReq> = mutableListOf(),
)

data class AddOrderRes(
    val resultCode: Int,
    val id: Long? = null,
)

data class OrderItemReq(
    val id: Long? = null,
    val quantity: Int? = null,
)

data class UpdateOrderReq(
    val oderItems: MutableList<UpdateOrderItemReq> = mutableListOf(),
)

data class UpdateOrderItemReq(
    val id: Long? = null,
    val status: OrderItemStatus? = null,
)

data class OrderRes(
    val id: Long? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val createAt: Instant? = null,
    val delivery: DeliveryRes? = null,
    val orderItems: MutableList<OderItemsRes> = mutableListOf(),
)

data class OderItemsRes(
    val broadcastTitle: String? = null,
    val oderItems: MutableList<OrderItemRes> = mutableListOf(),
)

data class AnalyzeContentReq(
    val memberId: Long
)

data class AnalyzeContentRes(
    val AiRes: MutableList<AiRes> = mutableListOf(),
    val avg_calculation_time: Double,
    val avg_algorithm_error: Double,
)

data class ErrorRes(
    val message: String
)