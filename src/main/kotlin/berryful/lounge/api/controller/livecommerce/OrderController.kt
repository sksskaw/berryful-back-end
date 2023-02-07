package berryful.lounge.api.controller.livecommerce

import berryful.lounge.api.data.OrderReq
import berryful.lounge.api.data.UpdateOrderItemReq
import berryful.lounge.api.data.UpdateOrderReq
import berryful.lounge.api.service.livecommerce.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping("/order")
    fun addOrder(request: HttpServletRequest, @RequestBody orderReq: OrderReq): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(orderService.addOrder(memberId, orderReq))
    }

    @GetMapping("/orders")
    fun getOrders(request: HttpServletRequest): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(orderService.getOrders(memberId))
    }

    @GetMapping("/order/{id}")
    fun getOrder(request: HttpServletRequest, @PathVariable id: Long): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(orderService.getOrder(memberId, id))
    }

    @PutMapping("/orders")
    fun updateOrders(request: HttpServletRequest, @RequestBody req: UpdateOrderReq): ResponseEntity<Any> {
        val memberId = request.userPrincipal!!.name.toLong()

        return ResponseEntity
            .ok()
            .body(orderService.updateOrders(memberId, req))
    }
}