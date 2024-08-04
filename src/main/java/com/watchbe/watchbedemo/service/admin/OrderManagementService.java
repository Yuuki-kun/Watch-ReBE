package com.watchbe.watchbedemo.service.admin;

import com.stripe.exception.StripeException;
import com.watchbe.watchbedemo.dto.OrderDto;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderManagementService {
    List<OrderDto> getAllOrders();

    List<OrderDto> getAll(Pageable page);
    List<OrderDto> capturePayment(Long orderId) throws StripeException, MessagingException;
    OrderDto capturePaymentDetails(Long orderId) throws StripeException, MessagingException;

    OrderDto changeOrderStatus(Long orderId, String status);


    List<OrderDto> getCompletedOrders(Pageable page);

    List<OrderDto> getUncapturedOrders(Pageable page);
    List<OrderDto> getPreparingOrders(Pageable page);
    List<OrderDto> getShippingOrders(Pageable page);

    List<OrderDto> getRefundedOrders(Pageable page);
    List<OrderDto> getCancelledOrders(Pageable page);


    List<OrderDto> cancelOrder(Long orderId) throws Exception;

    List<OrderDto> refuse(Long orderId, String reason) throws Exception;


    List<OrderDto> getTodayOrders();


    Page<OrderDto> getOrdersByDateRange(Long start, Long end, Pageable page);

    List<OrderDto> getTotalOrdersByDateRange(Long start, Long end);

    OrderDto refund(Long id, String reason) throws StripeException;
}
