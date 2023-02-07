package berryful.lounge.api.service.livecommerce

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.Broadcast
import berryful.lounge.api.entity.Order
import berryful.lounge.api.entity.OrderItem
import berryful.lounge.api.entity.OrderItemStatus
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.livecommerce.DeliveryRepository
import berryful.lounge.api.repository.livecommerce.OrderItemRepository
import berryful.lounge.api.repository.livecommerce.OrderRepository
import berryful.lounge.api.repository.livecommerce.SaleItemRepository
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val memberRepository: MemberRepository,
    private val deliveryRepository: DeliveryRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val saleItemRepository: SaleItemRepository
) {

    @Transactional
    fun addOrder(memberId: Long, req: OrderReq): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val delivery = deliveryRepository.findByIdOrNull(req.deliveryId)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)

        val newOrder = Order(member, delivery)
        orderRepository.save(newOrder)

        req.saleItems.forEach {
            val saleItem = saleItemRepository.findByIdOrNull(it.id)
                ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)
            orderItemRepository.save(
                OrderItem(newOrder, saleItem, saleItem.broadcast.title, it.quantity, OrderItemStatus.PREPARE)
            )
        }
        return AddOrderRes(
            resultCode = ErrorMessageCode.OK.code,
            id = newOrder.id
        )
    }

    @Transactional(readOnly = true)
    fun getOrders(memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val returnOrders: MutableList<OrderRes> = mutableListOf()
        member.orderList.forEach { order ->
            val orderItemList = order.orderItemList
            val broadcasts: MutableSet<Broadcast> = mutableSetOf()
            orderItemList.forEach { orderItem ->
                broadcasts.add(orderItem.saleItem.broadcast)
            }

            val oderItemsRes: MutableList<OderItemsRes> = mutableListOf()
            broadcasts.forEach { broadcast ->
                val returnOrderItems: MutableList<OrderItemRes> = mutableListOf()
                val orderItems = orderItemList.filter { it.saleItem.broadcast.id == broadcast.id }
                orderItems.forEach {
                    returnOrderItems.add(
                        OrderItemRes(
                            id = it.id,
                            cartId = it.id,
                            title = it.saleItem.title,
                            description = it.saleItem.description,
                            quantity = it.quantity,
                            price = it.saleItem.price,
                            imgUrl = it.saleItem.imgUrl,
                            status = it.status,
                        )
                    )
                }
                oderItemsRes.add(OderItemsRes(broadcast.title, returnOrderItems))
            }

            val returnDelivery = DeliveryRes(order.delivery.id, order.delivery.title, order.delivery.address, order.delivery.postCode)
            returnOrders.add(
                OrderRes(
                    id = order.id,
                    createAt = order.createAt,
                    delivery =
                    returnDelivery,
                    orderItems = oderItemsRes
                )
            )
        }
        return returnOrders
    }

    fun getOrder(memberId: Long, orderId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val order = orderRepository.findByIdOrNull(orderId)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)

        if (member != order.member)
            return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)

        val orderItemList = order.orderItemList
        val broadcasts: MutableSet<Broadcast> = mutableSetOf()
        orderItemList.forEach { orderItem ->
            broadcasts.add(orderItem.saleItem.broadcast)
        }

        val oderItemsRes: MutableList<OderItemsRes> = mutableListOf()
        broadcasts.forEach { broadcast ->
            val returnOrderItems: MutableList<OrderItemRes> = mutableListOf()
            val orderItems = orderItemList.filter { it.saleItem.broadcast.id == broadcast.id }
            orderItems.forEach {
                returnOrderItems.add(
                    OrderItemRes(
                        id = it.id,
                        cartId = it.id,
                        title = it.saleItem.title,
                        description = it.saleItem.description,
                        quantity = it.quantity,
                        price = it.saleItem.price,
                        imgUrl = it.saleItem.imgUrl,
                        status = it.status,
                    )
                )
            }
            oderItemsRes.add(OderItemsRes(broadcast.title, returnOrderItems))
        }

        val returnDelivery = DeliveryRes(order.delivery.id, order.delivery.title, order.delivery.address, order.delivery.postCode)

        return OrderRes(
                id = order.id,
                createAt = order.createAt,
                delivery =
                returnDelivery,
                orderItems = oderItemsRes
        )
    }

    fun updateOrders(memberId: Long, req: UpdateOrderReq): Any {
        req.oderItems.forEach {
            val orderItem = orderItemRepository.findByIdOrNull(it.id)
                ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

            if (orderItem.order.member.id != memberId)
                return ApiResultCode(ErrorMessageCode.NOT_HAVE_PERMISSION.code)

            orderItem.status = it.status
            orderItemRepository.save(orderItem)
        }

        return ApiResultCode(ErrorMessageCode.OK.code)
    }
}