package com.watchbe.watchbedemo.service.admin.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.dto.OrderStatusDto;
import com.watchbe.watchbedemo.exception.NotFoundException;
import com.watchbe.watchbedemo.mapper.OrderMapperImpl;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.OrderRepository;
//import com.watchbe.watchbedemo.repository.OrderStatusHistoryRepository;
import com.watchbe.watchbedemo.repository.OrderStatusRepository;
import com.watchbe.watchbedemo.repository.PaymentRepository;
import com.watchbe.watchbedemo.repository.WatchRepository;
import com.watchbe.watchbedemo.service.EmailService;
import com.watchbe.watchbedemo.service.PaypalService;
import com.watchbe.watchbedemo.service.StripeService;
import com.watchbe.watchbedemo.service.admin.OrderManagementService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderManagementServiceImpl implements OrderManagementService {
    private final OrderRepository orderRepository;
    private final OrderMapperImpl orderMapper;
    private final StripeService stripeService;
    private final PaypalService paypalService;
    private final EmailService emailService;
    private final OrderStatusRepository orderStatusRepository;
    private final WatchRepository watchRepository;
//    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getAll(Pageable page) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc(page);

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> capturePayment(Long orderId) throws StripeException, MessagingException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String paymentMethod = order.getPayment().getType();
        String userEmail = order.getCustomer().getEmail();

        if(paymentMethod.equals("stripe")) {
            stripeService.capturePayment(orderId);
            emailService.sendEmail("tongcongminh2021@gmail.com", " Cảm ơn bạn đã đặt hàng - [TIMEFLOW]",
                    sendOrderedEmail(order.getReceiptUrl(), orderId, order.getOrderDetails(), order.getAmount()));
        } else if (paymentMethod.equals("paypal")) {
            paypalService.capturePayment(orderId);
        } else {
            throw new RuntimeException("Invalid payment method");
        }

        return getAllOrders();
    }
    @Override
    public OrderDto capturePaymentDetails(Long orderId) throws StripeException, MessagingException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        String paymentMethod = order.getPayment().getType();
        if(paymentMethod.equals("stripe")) {
            stripeService.capturePayment(orderId);
            emailService.sendEmail("tongcongminh2021@gmail.com", " Cảm ơn bạn đã đặt hàng - [TIMEFLOW]",
                   sendOrderedEmail(order.getReceiptUrl(), orderId, order.getOrderDetails(), order.getAmount()));
        } else if (paymentMethod.equals("paypal")) {
            paypalService.capturePayment(orderId);
        }else if(paymentMethod.equals("cash")) {
            OrderStatus orderStatus = orderStatusRepository.findByStatus(Status.CONFIRMED)
                    .orElseGet(() -> {
                        OrderStatus newOrderStatus = OrderStatus.builder()
                                .status(Status.CONFIRMED)
                                .build();
                        return orderStatusRepository.save(newOrderStatus);
                    });
            order.setOrderStatus(orderStatus); // Associate the Order with the OrderStatus
            orderRepository.save(order); // Save the Order
            order.getOrderDetails().forEach(or->{
                Watch w = or.getWatch();
                w.setInventoryQuantity(w.getInventoryQuantity()-or.getQuantity());
                w.setSoldQuantity(w.getSoldQuantity()+or.getQuantity());
                watchRepository.save(w);
            });
            emailService.sendEmail("tongcongminh2021@gmail.com", " Cảm ơn bạn đã đặt hàng - [TIMEFLOW]",
                    sendOrderedEmail(order.getReceiptUrl(), orderId, order.getOrderDetails(), order.getAmount()));
        }

        else {
            throw new RuntimeException("Invalid payment method");
        }

        return orderMapper.mapTo(order);
    }

    @Override
    public OrderDto changeOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Optional<OrderStatus> orderStatus = orderStatusRepository.findByStatus(Status.valueOf(status));
        if(orderStatus.isPresent()) {

            order.setOrderStatus(orderStatus.get());
            orderRepository.save(order);

        }
        return orderMapper.mapTo(order);
    }

    @Override
    public List<OrderDto> getCompletedOrders(Pageable page) {
        List<Order> orders = orderRepository.findAllByOrderStatus_statusOrderByOrderDateDesc(Status.DELIVERED,page);

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getUncapturedOrders(Pageable page) {
        List<Order> orders = orderRepository.findAllByOrderStatus_statusOrderByOrderDateDesc(Status.PENDING,page);

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());    }

    @Override
    public List<OrderDto> getPreparingOrders(Pageable page) {
        List<Order> orders = orderRepository.findAllByOrderStatus_statusOrderByOrderDateDesc(Status.PREPARING,page);

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());    }

    @Override
    public List<OrderDto> getShippingOrders(Pageable page) {
        List<Order> orders = orderRepository.findAllByOrderStatus_statusOrderByOrderDateDesc(Status.SHIPPING,page);

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());    }

    @Override
    public List<OrderDto> getRefundedOrders(Pageable page) {
        List<Order> orders = orderRepository.findAllByOrderStatus_statusOrderByOrderDateDesc(Status.REFUNDED,page);

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());    }

    @Override
    public List<OrderDto> getCancelledOrders(Pageable page) {
        List<Order> orders = orderRepository.findAllByOrderStatus_statusOrderByOrderDateDesc(Status.CANCELLED,page);

        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());    }

    @Override
    public List<OrderDto> cancelOrder(Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new Exception(""));

        return null;
    }

    @Override
    public List<OrderDto> refuse(Long orderId, String reason) throws Exception {
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new Exception(""));
        OrderStatus orderStatus = OrderStatus.builder().status(Status.REFUSED).code(10).description(reason).build();

        if(order.getPayment().getPaymentMethod().equals("card")) {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(order.getPaymentIntentId());
            PaymentIntent update = paymentIntent.cancel();
            orderStatusRepository.save(orderStatus);
            order.setOrderStatus(orderStatus);
        }
        orderRepository.save(order);
        emailService.sendEmail("tongcongminh2021@gmail.com",
                "[TIMEFLOW] Your order has been refused",
                sendRefuseEmail(reason, orderId));
        return getAllOrders();
    }

    @Override
    public List<OrderDto> getTodayOrders() {
        List<Order> orders = orderRepository.findAllByOrderDateEqualsToDate( new Date());
        return orders.stream().map(
                orderMapper::mapTo
        ).collect(Collectors.toList());
    }

    public String sendRefuseEmail( String reason, Long orderId) {
        return
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "    <meta charset='UTF-8'>" +
                        "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "    <title>Your Order Refused</title>" +
                        "    <style>" +
                        "        body {" +
                        "            font-family: 'Arial', sans-serif;" +
                        "            margin: 0;" +
                        "            padding: 0;" +
                        "            background-color: #f4f4f4;" +
                        "        }" +
                        "        .container {" +
                        "            max-width: 600px;" +
                        "            margin: 20px auto;" +
                        "            padding: 20px;" +
                        "            background-color: #f6f6f6;" +
                        "            border-radius: 10px;" +
                        "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);" +
                        "        }" +
                        "        h1 {" +
                        "            color: #333333;" +
                        "            margin-bottom: 20px;" +
                        "        }" +
                        "        p {" +
                        "            color: #666666;" +
                        "            margin-bottom: 10px;" +
                        "        }" +
                        "        .reason {" +
                        "            color: #ff0000;" +
                        "            font-weight: bold;" +
                        "        }" +
                        "        .apology {" +
                        "            color: #333333;" +
                        "            font-style: italic;" +
                        "            margin-top: 20px;" +
                        "        }" +
                        "        .footer {" +
                        "            margin-top: 20px;" +
                        "            font-size: 12px;" +
                        "            color: #999999;" +
                        "        }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <h1 style='text-align: center;'>[TIMEFLOW] Your order has been refused</h1>" +
                        "        <p class='apology'>We apologize for any inconvenience caused.</p>" +
                        "        <p>Your order has been refused for the following reason:</p>" +
                        "        <p class='reason'>" + reason + "</p>" +
                        "        <p>Order ID: " + orderId + "</p>" +
                        "        <p>Please contact us for further information.</p>" +
                        "        <p class='footer'>This email was sent from [TIMEFLOW]. Please do not reply to this " +
                        "email.</p>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";
    }
    public String sendOrderedEmail( String receipt, Long orderId, List<OrderDetails> orderDetails, double totalAmount) {
        String productListHTML = "";
        for (int i = 0; i < orderDetails.size(); i++) {
            String productName = orderDetails.get(i).getWatch().getName();
            int quantity = (int) orderDetails.get(i).getQuantity();
            double price = orderDetails.get(i).getPrice()*1000;
            productListHTML += "<li><strong>Product:</strong> " + productName + ", <strong>Quantity:</strong> " + quantity +", " +
                    "<strong>Price:</strong>"+ price +"VND"+"</li>";
        }

        return
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "    <meta charset='UTF-8'>" +
                        "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "    <title>Order Confirmation</title>" +
                        "    <style>" +
                        "        body {" +
                        "            font-family: 'Arial', sans-serif;" +
                        "            margin: 0;" +
                        "            padding: 0;" +
                        "            background-color: #f4f4f4;" +
                        "        }" +
                        "        .container {" +
                        "            max-width: 600px;" +
                        "            margin: 20px auto;" +
                        "            padding: 20px;" +
                        "            background-color: #ffffff;" +
                        "            border-radius: 10px;" +
                        "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);" +
                        "        }" +
                        "        h1 {" +
                        "            color: #333333;" +
                        "            margin-bottom: 20px;" +
                        "        }" +
                        "        p {" +
                        "            color: #666666;" +
                        "            margin-bottom: 10px;" +
                        "        }" +
                        "        .order-details {" +
                        "            background-color: #f9f9f9;" +
                        "            padding: 10px;" +
                        "            border-radius: 5px;" +
                        "        }" +
                        "        .footer {" +
                        "            margin-top: 20px;" +
                        "            font-size: 12px;" +
                        "            color: #999999;" +
                        "        }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <h1 style='text-align: center;'>[TIMEFLOW] Order Confirmation</h1>" +
                        "        <p>Dear Customer,</p>" +
                        "        <p>We are pleased to inform you that your order has been successfully placed. Below are the details of your order:</p>" +
                        "        <div class='order-details'>" +
                        "            <p><strong>Order ID:</strong> " + orderId + "</p>" +
                        "            <ul>" + productListHTML + "</ul>" +
                        "            <p><strong>Total Amount:</strong>" + totalAmount*1000 + "VND</p>" +
                        "            <p><strong>Receipt:</strong> <a href='" + receipt + "'>View Receipt</a></p>" +
                        "        </div>" +
                        "        <p>Thank you for shopping with us. Your satisfaction is our priority.</p>" +
                        "        <p class='footer'>This email was sent from [TIMEFLOW]. Please do not reply to this " +
                        "email.</p>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";
    }

    public Page<OrderDto> getOrdersByDateRange(Long start, Long end, Pageable page) {
        return orderRepository.findAllByOrderDateBetweenAndOrderStatusOrderByOrderDateDesc(Status.DELIVERED,new Date((start)),
                new Date(end), page).map(orderMapper::mapTo);

    }

    @Override
    public List<OrderDto> getTotalOrdersByDateRange(Long start, Long end) {
        return orderRepository.findAllByOrderDateBetweenOrderByOrderDateDesc(new Date((start)),
                new Date(end)).stream().map(orderMapper::mapTo).collect(Collectors.toList());    }

    @Override
    public OrderDto refund(Long id, String reason) throws StripeException {
        Order order = orderRepository.findById(id).orElseThrow(()->new NotFoundException("Order not found"));
        RefundCreateParams refundCreateParams = RefundCreateParams.builder().setPaymentIntent(order.getPaymentIntentId()).setReason(reason.equals("FRAUDULENT") ? RefundCreateParams.Reason.FRAUDULENT : RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER).build();

        Refund refund = Refund.create(refundCreateParams);
        OrderStatus refundStatus = OrderStatus.builder().status(Status.REFUNDED).code(8).build();
        if(reason.equals("FRAUDULENT")) {
            refundStatus.setDescription("Nghi ngờ gian lận");
        }else if(reason.equals("CUSTOMER_REQUEST")) {
            refundStatus.setDescription("Yêu cầu của khách hàng");
        }else if(reason.equals("OUT_OF_STOCK")) {
            refundStatus.setDescription("Hết hàng");}
        else{
                refundStatus.setDescription("Lý do khác");

            }
        orderStatusRepository.save(refundStatus);
        order.setOrderStatus(refundStatus);
        orderRepository.save(order);

        return orderMapper.mapTo(order);
    }

}
