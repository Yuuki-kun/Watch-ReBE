package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.dto.OrderStatusHistoryDto;
import com.watchbe.watchbedemo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/search-order")
    public ResponseEntity<List<OrderDto>> searchOrder(@RequestParam("userId") Long userId, @RequestParam("searchValue") String searchValue){
        return ResponseEntity.ok(orderService.searchOrder(userId, searchValue));
    }

    @GetMapping("/retrieve-order/{orderId}")
    public ResponseEntity<OrderDto> retrieveOrder(@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.retrieveOrderById(orderId));
    }

    @GetMapping("/retrieve-orders/status/{customerId}/{orderStatusId}")
    public ResponseEntity<List<OrderDto>> retrieveOrdersByStatus(@PathVariable Long customerId, @PathVariable Long orderStatusId){
        return ResponseEntity.ok(orderService.retrieveOrdersByStatus(customerId, orderStatusId));
    }

//    @GetMapping("/retrieve-order-status-history/{orderId}")
//    public ResponseEntity<List<OrderStatusHistoryDto>> retrieveOrderStatusHistory(@PathVariable Long orderId){
//        return ResponseEntity.ok(orderService.retrieveOrderStatusHistory(orderId));
//    }

    @PostMapping("/cancel-order/{orderId}")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long orderId, @RequestParam String reason){
        return ResponseEntity.ok(orderService.cancelOrder(orderId, reason));
    }

}
