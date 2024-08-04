package com.watchbe.watchbedemo.controller.admin;


import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.model.Order;
import com.watchbe.watchbedemo.service.EmailService;
import com.watchbe.watchbedemo.service.PaypalService;
import com.watchbe.watchbedemo.service.StripeService;
import com.watchbe.watchbedemo.service.admin.impl.OrderManagementServiceImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin-order-mgt")
@RequiredArgsConstructor
public class OrderMgtController {
    private final OrderManagementServiceImpl orderManagementService;
    private final StripeService stripeService;
    private final PaypalService paypalService;

    @PostMapping("/capture/{orderId}")
    public ResponseEntity<?> capturePayment(@PathVariable Long orderId,
                                                         @RequestParam("type") String type) throws StripeException, MessagingException {
        System.out.println("type="+type);
        if(type.equals("list")){

          List<OrderDto> orderDtos =  orderManagementService.capturePayment(orderId);

          return ResponseEntity.ok(orderDtos);

      } else if (type.equals("details")) {
          OrderDto orderDto = orderManagementService.capturePaymentDetails(orderId);
            return ResponseEntity.ok(orderDto);
      }
        return ResponseEntity.ok("Invalid type");
    }

    //change order status
    @PutMapping("/status/{orderId}")
    public ResponseEntity<?> changeOrderStatus(@PathVariable Long orderId,
                                               @RequestParam("status") String status){
        return ResponseEntity.ok(orderManagementService.changeOrderStatus(orderId, status));
    }

    //cancel order
    @PostMapping("/refuse-order/{orderId}")
    public ResponseEntity<List<OrderDto>> cancelOrder(@PathVariable Long orderId, @RequestParam String reason) throws Exception {
        System.out.println("Cancel id="+orderId);

//        Order order = orderRepository.findById(id).orElseThrow(() -> new Exception(""));
//        Status status = statusRepository.findByTtTrangThai("Đã hủy").orElseThrow(()-> new Exception(""));
//
//        PaymentIntent paymentIntent = PaymentIntent.retrieve(order.getPiId());
//        PaymentIntent update = paymentIntent.cancel();
//
//        order.setTrangThai(status);
//        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder().donHang(order).trangThai(status).statusChangeDate(new Date()).build();
//        orderRepository.save(order);
//        orderStatusHistoryRepository.save(orderStatusHistory);
        return ResponseEntity.ok( orderManagementService.refuse(orderId, reason));
    }


    //refund order
    @PostMapping("/refund-payment/{id}")
    public ResponseEntity<OrderDto> refundPayment(@PathVariable("id") Long id,
                                                        @RequestParam("reason") String reason) throws Exception {

        return ResponseEntity.ok(orderManagementService.refund(id,reason));
    }
}
