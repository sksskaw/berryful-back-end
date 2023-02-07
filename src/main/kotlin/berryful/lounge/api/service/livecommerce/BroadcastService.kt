package berryful.lounge.api.service.livecommerce

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.Broadcast
import berryful.lounge.api.entity.Cart
import berryful.lounge.api.entity.Delivery
import berryful.lounge.api.entity.DeliveryStatus
import berryful.lounge.api.repository.crm.MemberRepository
import berryful.lounge.api.repository.livecommerce.BroadcastRepository
import berryful.lounge.api.repository.livecommerce.CartRepository
import berryful.lounge.api.repository.livecommerce.DeliveryRepository
import berryful.lounge.api.repository.livecommerce.SaleItemRepository
import berryful.lounge.api.utils.ApiResultCode
import berryful.lounge.api.utils.ErrorMessageCode
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

@Service
class BroadcastService(
    private val memberRepository: MemberRepository,
    private val broadcastRepository: BroadcastRepository,
    private val saleItemRepository: SaleItemRepository,
    private val cartRepository: CartRepository,
    private val deliveryRepository: DeliveryRepository,
) {
    @Cacheable(value = ["basicCacheConf"])
    @Transactional(readOnly = true)
    fun getBroadcasts(pageable: Pageable): Any {
        val broadcasts = broadcastRepository.findAll(pageable)

        val returnBroadcasts: MutableList<BroadcastRes> = mutableListOf()
        broadcasts.forEach { broadcast ->
            val returnSaleItems: MutableList<SaleItemRes> = mutableListOf()
            broadcast.saleItemList.forEach { saleItem ->
                returnSaleItems.add(
                    SaleItemRes(
                        id = saleItem.id,
                        broadcastId = broadcast.id,
                        title = saleItem.title,
                        description = saleItem.description,
                        price = saleItem.price,
                        imgUrl = saleItem.imgUrl,
                    )
                )
            }

            returnBroadcasts.add(
                BroadcastRes(
                    id = broadcast.id,
                    title = broadcast.title,
                    description = broadcast.description,
                    channelId = broadcast.channelId,
                    rtmp = broadcast.rtmp,
                    status = broadcast.status,
                    reservationStartAt = broadcast.reservationStartAt,
                    coverUrl = broadcast.coverUrl,
                    saleItems = returnSaleItems,
                )
            )
        }

        return PageableSetRes(
            rowCount = broadcasts.totalElements.toInt(),
            rows = returnBroadcasts
        )
    }

    fun getBroadcast(id: Long) : Any{
        val broadcast = broadcastRepository.findByIdOrNull(id)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)

        val returnSaleItems: MutableList<SaleItemRes> = mutableListOf()
        broadcast.saleItemList.forEach { saleItem ->
            returnSaleItems.add(
                SaleItemRes(
                    id = saleItem.id,
                    broadcastId = broadcast.id,
                    title = saleItem.title,
                    description = saleItem.description,
                    price = saleItem.price,
                    imgUrl = saleItem.imgUrl,
                )
            )
        }

        return BroadcastRes(
            id = broadcast.id,
            title = broadcast.title,
            description = broadcast.description,
            channelId = broadcast.channelId,
            rtmp = broadcast.rtmp,
            status = broadcast.status,
            reservationStartAt = broadcast.reservationStartAt,
            coverUrl = broadcast.coverUrl,
            saleItems = returnSaleItems,
        )
    }

    @Cacheable(value = ["basicCacheConf"])
    @Transactional(readOnly = true)
    fun getSaleItems(broadcastId: Long): Any {
        val saleItemRes1 = SaleItemRes(
            id = 1,
            broadcastId = 1,
            title = "상품 1",
            description = "상품 1 입니다.",
            price = 10000,
            imgUrl = "https://berryful-files-dev.s3.ap-northeast-2.amazonaws.com/berryful-semple-img/item-1.jpg",
        )

        val saleItemRes2 = SaleItemRes(
            id = 2,
            broadcastId = 1,
            title = "상품 2",
            description = "상품 2 입니다.",
            price = 20000,
            imgUrl = "https://berryful-files-dev.s3.ap-northeast-2.amazonaws.com/berryful-semple-img/item-2.jpg",
        )

        val saleItemRes3 = SaleItemRes(
            id = 3,
            broadcastId = 1,
            title = "상품 3",
            description = "상품 3 입니다.",
            price = 10000,
            imgUrl = "https://berryful-files-dev.s3.ap-northeast-2.amazonaws.com/berryful-semple-img/item-3.jpg",
        )

        val returnSaleItems: MutableList<SaleItemRes> = mutableListOf()
        returnSaleItems.add(saleItemRes1)
        returnSaleItems.add(saleItemRes2)
        returnSaleItems.add(saleItemRes3)
        return returnSaleItems
    }

    @Transactional(readOnly = true)
    fun getCart(memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val cartList = member.cartList
        val broadcasts: MutableSet<Broadcast> = mutableSetOf()
        cartList.forEach { cart ->
            broadcasts.add(cart.saleItem.broadcast)
        }

        val returnCarts: MutableList<CartRes> = mutableListOf()
        broadcasts.forEach { broadcast ->
            val returnCartItems: MutableList<OrderItemRes> = mutableListOf()
            val cartItems = cartList.filter { it.saleItem.broadcast.id == broadcast.id }
            cartItems.forEach {
                returnCartItems.add(
                    OrderItemRes(
                        id = it.saleItem.id,
                        cartId = it.id,
                        title = it.saleItem.title,
                        description = it.saleItem.description,
                        quantity = it.quantity,
                        price = it.saleItem.price,
                        imgUrl = it.saleItem.imgUrl,
                    )
                )
            }
            returnCarts.add(CartRes(broadcast.title, returnCartItems))
        }

        return returnCarts
    }

    @Transactional
    fun addCartItem(memberId: Long, req: CartReq): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val saleItem = saleItemRepository.findByIdOrNull(req.saleItemId)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)

        val newCart = Cart(member, saleItem, req.quantity)
        cartRepository.save(newCart)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun updateCartItem(memberId: Long, cartId: Long, req: UpdateCart): Any {
        val cart = cartRepository.findByIdOrNull(cartId)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)

        cart.quantity = req.quantity
        cartRepository.save(cart)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional
    fun deleteCartItem(memberId: Long, cartId: Long): Any {
        val cart = cartRepository.findByIdOrNull(cartId)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)

        cartRepository.delete(cart)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }

    @Transactional(readOnly = true)
    fun getDeliveries(memberId: Long): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val returnDeliveries: MutableList<DeliveryRes> = mutableListOf()
        member.deliveryList.forEach { delivery ->
            if (delivery.status == DeliveryStatus.DELETE)
                return@forEach
            returnDeliveries.add(
                DeliveryRes(
                    id = delivery.id,
                    title = delivery.title,
                    address = delivery.address,
                    postCode = delivery.postCode,
                )
            )
        }

        return returnDeliveries
    }

    @Transactional
    fun addDelivery(memberId: Long, req: DeliveryReq): Any {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: return ApiResultCode(ErrorMessageCode.NOT_FOUND_MEMBER.code)

        val newDelivery = Delivery(member, req.title, req.address, req.postCode)
        deliveryRepository.save(newDelivery)

        return AddDeliveryRes(ErrorMessageCode.OK.code, newDelivery.id)
    }

    @Transactional
    fun deleteDelivery(memberId: Long, deliveryId: Long): Any {
        val delivery = deliveryRepository.findByIdOrNull(deliveryId)
            ?: return ApiResultCode(ErrorMessageCode.ENTITY_NULL.code)

        delivery.status = DeliveryStatus.DELETE
        deliveryRepository.save(delivery)
        return ApiResultCode(ErrorMessageCode.OK.code)
    }
}