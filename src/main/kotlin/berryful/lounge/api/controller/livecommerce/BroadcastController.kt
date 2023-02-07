package berryful.lounge.api.controller.livecommerce

import berryful.lounge.api.data.CartReq
import berryful.lounge.api.data.DeliveryReq
import berryful.lounge.api.data.UpdateCart
import berryful.lounge.api.service.livecommerce.BroadcastService
import berryful.lounge.api.utils.Log
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
class BroadcastController(
    private val broadcastService: BroadcastService
) {
    @GetMapping("/broadcasts")
    fun getBroadcasts(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int,
    ): ResponseEntity<Any> {
        val pageRequestSort = Sort.by(Sort.Direction.DESC, "id")
        return ResponseEntity
            .ok()
            .body(broadcastService.getBroadcasts(PageRequest.of(page, size, pageRequestSort)))
    }

    @GetMapping("/broadcast/{id}")
    fun getBroadcast(@PathVariable id: Long): ResponseEntity<Any> {
        return ResponseEntity
            .ok()
            .body(broadcastService.getBroadcast(id))
    }

    @GetMapping("/sale/items/{broadcastId}")
    fun getSaleItems(
        @PathVariable broadcastId: Long
    ): ResponseEntity<Any> {
        return ResponseEntity
            .ok()
            .body(broadcastService.getSaleItems(broadcastId))
    }

    @GetMapping("/cart")
    fun getCart(request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(broadcastService.getCart(memberId))
    }

    @PostMapping("/cart")
    fun addCartItem(request: HttpServletRequest, @RequestBody updateCart: CartReq): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(broadcastService.addCartItem(memberId, updateCart))
    }

    @PutMapping("/cart/{id}")
    fun updateCartItem(
        request: HttpServletRequest, @PathVariable id: Long, @RequestBody updateCart: UpdateCart
    ): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(broadcastService.updateCartItem(memberId, id, updateCart))
    }

    @DeleteMapping("/cart/{id}")
    fun deleteCartItem(request: HttpServletRequest, @PathVariable id: Long): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(broadcastService.deleteCartItem(memberId, id))
    }

    @GetMapping("/deliveries")
    fun getDeliveries(request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(broadcastService.getDeliveries(memberId))
    }

    @PostMapping("/delivery")
    fun addDelivery(request: HttpServletRequest, @RequestBody deliveryReq: DeliveryReq): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(broadcastService.addDelivery(memberId, deliveryReq))
    }

    @DeleteMapping("/delivery/{id}")
    fun deleteDelivery(request: HttpServletRequest, @PathVariable id: Long): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(broadcastService.deleteDelivery(memberId, id))
    }
}