package com.watchbe.watchbedemo.service;

import com.stripe.exception.StripeException;
import com.watchbe.watchbedemo.dto.CreateCheckoutRequest;
import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.*;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public abstract class CheckoutService {
    protected final OrderRepository orderRepository;
    protected final OrderDetailsRepository orderDetailsRepository;
    protected final CustomerRepository customerRepository;
    protected final OrderStatusRepository orderStatusRepository;
    protected final PaymentRepository paymentRepository;
    protected  final WatchRepository watchRepository;
    protected final ShippingRepository shippingRateRepository;
//    protected final OrderStatusHistoryRepository orderStatusHistoryRepository;
    public CheckoutService(OrderRepository orderRepository, OrderDetailsRepository orderDetailsRepository
            , CustomerRepository customerRepository, OrderStatusRepository orderStatusRepository, PaymentRepository paymentRepository, WatchRepository watchRepository, ShippingRepository shippingRateRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.customerRepository = customerRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.paymentRepository = paymentRepository;
        this.watchRepository = watchRepository;
//        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.shippingRateRepository = shippingRateRepository;
    }
    protected Order createOrder(CreateCheckoutRequest checkOutRequest, String method) {
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
                .mapToDouble(orderDetails -> orderDetails.getPrice() * orderDetails.getQuantity() )
                .sum();
        totalAmount = Math.round(totalAmount * 100.0) / 100.0; // làm tròn với hai chữ số thập phân

        Customer customer =
                customerRepository.findById(checkOutRequest.getCustomerId()).orElseThrow(() -> new IllegalArgumentException("Customer not found"));



        Payment payment = Payment.builder()
                .date(new Date())
                .type(method)
                .build();
        paymentRepository.save(payment);

        ShippingAddress shippingAddress =
                customer.getShippingAddresses().stream().filter(ShippingAddress::getIsDefault).findFirst().get();

        //calculate shipping fee
        float shippingFee = 0;
        if (shippingAddress.getCity() != null) {

            //find shipping rate by province
            ShippingRate shippingRate = shippingRateRepository.findByProvince(shippingAddress.getCity());
            if(totalAmount < shippingRate.getFreeShippingThreshold()){
                shippingFee = shippingRate.getRate();
            }else{
                shippingFee = 0;
            }
        }

        Order order =
                Order.builder().customer(customer).shipping(shippingFee).address(shippingAddress).payment(payment).orderDate(new Date()).tax(100f).amount(totalAmount+shippingFee/1000).build();
        order.setOrderDetails(orderDetailsList);
        orderRepository.save(order);



        //find in customer.getShippingAddresses if shipping address is default address then set it to order
        //if not then set the first address to order
//        order.setAddress(customer.getShippingAddresses().stream().filter(ShippingAddress::getIsDefault).findFirst().get());
        return order;
    }
    protected abstract String createPaymentIntent(Order order) throws StripeException;
    public String processPaymentIntent(CreateCheckoutRequest request, String method) throws StripeException {
        Order order = createOrder(request, method);
        String paymentURL =  createPaymentIntent(order);
        order.setPaymentUrl(paymentURL);
        orderRepository.save(order);
        return paymentURL;
    }

    public abstract void capturePayment(Long orderId) throws StripeException;

    }
