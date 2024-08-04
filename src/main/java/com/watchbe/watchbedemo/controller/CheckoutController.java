package com.watchbe.watchbedemo.controller;

import com.stripe.exception.StripeException;
import com.watchbe.watchbedemo.dto.CreateCheckoutRequest;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.*;
import com.watchbe.watchbedemo.service.PaypalService;
import com.watchbe.watchbedemo.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final StripeService stripeCheckoutService;
    private final PaypalService paypalService;
//    public CheckoutController(@Qualifier("stripeCheckoutServiceImpl") CheckoutService stripeCheckoutService) {
//        this.stripeCheckoutService = stripeCheckoutService;
//    }

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusRepository orderStatusRepository;
    @PostMapping("/create-payment-session/{method}")
        public String createPaymentIntent(@RequestBody CreateCheckoutRequest checkOutRequest,
                                          @PathVariable("method") String method) throws StripeException {
        System.out.println("ck request  = "+checkOutRequest);
        System.out.println("ck method = "+method);
        String paymentUrl;
        if(method.equals("stripe"))
            paymentUrl = stripeCheckoutService.processPaymentIntent(checkOutRequest, method);
        else if (method.equals("cash")) {
            Order order = createOrder(checkOutRequest, method);
            paymentUrl = "Order created successfully";

        }else{
            throw new RuntimeException("Invalid payment method");
        }
        return paymentUrl;
    }



    public Order createOrder(CreateCheckoutRequest checkOutRequest, String method) {
        if (checkOutRequest.getOrderDetailsId() == null || checkOutRequest.getOrderDetailsId().isEmpty()) {
            throw new IllegalArgumentException("order items cannot be empty");
        }
        if (checkOutRequest.getCustomerId()  <0) {
            throw new IllegalArgumentException("Customer id cannot be empty");
        }

        List<OrderDetails> orderDetailsList =
                orderDetailsRepository.findAllById(checkOutRequest.getOrderDetailsId());
        orderDetailsList.forEach(orderDetails -> orderDetails.setCart(null));

        double totalAmount =  orderDetailsList.stream()
                .mapToDouble(orderDetails -> orderDetails.getPrice() * orderDetails.getQuantity())
                .sum();

        Customer customer =
                customerRepository.findById(checkOutRequest.getCustomerId()).orElseThrow(() -> new IllegalArgumentException("Customer not found"));



        Payment payment = Payment.builder()
                .date(new Date())
                .type(method)
                .build();
        paymentRepository.save(payment);

        ShippingAddress shippingAddress =
                customer.getShippingAddresses().stream().filter(ShippingAddress::getIsDefault).findFirst().get();

        Order order =
                Order.builder().customer(customer).address(shippingAddress).payment(payment).orderDate(new Date()).tax(100f).amount(totalAmount).shipping(100f).build();
        order.setOrderDetails(orderDetailsList);
        OrderStatus orderStatus = orderStatusRepository.findByStatus(Status.PENDING)
                .orElseThrow(() -> new RuntimeException("Order status not found"));
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);



        //find in customer.getShippingAddresses if shipping address is default address then set it to order
        //if not then set the first address to order
//        order.setAddress(customer.getShippingAddresses().stream().filter(ShippingAddress::getIsDefault).findFirst().get());
        return order;
    }


}
