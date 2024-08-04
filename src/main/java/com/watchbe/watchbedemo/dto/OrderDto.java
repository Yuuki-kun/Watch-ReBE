package com.watchbe.watchbedemo.dto;

import com.watchbe.watchbedemo.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;

    private CustomerDto customer;

    private Date orderDate;

    private String paymentOrderId;
    private String paymentIntentId;

    private double amount;
    private float tax;
    private float shipping;
    private List<OrderDetailsDto> orderDetails = new ArrayList<>();
    private OrderStatusDto orderStatus;
    private ShippingAddressDto address;
    private PaymentDto payment;
    private String paymentUrl;
    private String receiptUrl;
    List<OrderStatusHistoryDto> orderStatusHistory = new ArrayList<>();
}
