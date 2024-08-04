package com.watchbe.watchbedemo.dto;

import com.watchbe.watchbedemo.model.Order;
import com.watchbe.watchbedemo.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryDto {
    private Long id;

    private OrderStatusDto orderStatus;

    private Date changeAt;

    private String comments;

}
