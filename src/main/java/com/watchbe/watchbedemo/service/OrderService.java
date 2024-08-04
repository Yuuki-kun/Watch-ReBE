package com.watchbe.watchbedemo.service;

import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.dto.OrderStatusHistoryDto;
import com.watchbe.watchbedemo.mapper.OrderMapperImpl;
import com.watchbe.watchbedemo.mapper.OrderStatusHisMapperImpl;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.OrderRepository;
//import com.watchbe.watchbedemo.repository.OrderStatusHistoryRepository;
import com.watchbe.watchbedemo.repository.OrderStatusRepository;
import com.watchbe.watchbedemo.service.utils.Similarity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapperImpl orderMapper;
    private final OrderStatusHisMapperImpl orderStatusHisMapper;
//    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    public OrderDto retrieveOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        return orderMapper.mapTo(order);
    }

//    public List<OrderStatusHistoryDto> retrieveOrderStatusHistory(Long orderId) {
//        List<OrderStatusHistory> orderStatusHistories = orderStatusHistoryRepository.findAllByOrderId(orderId);
//        return orderStatusHistories.stream().map(orderStatusHisMapper::mapTo).collect(Collectors.toList());
//    }


    public List<OrderDto> retrieveOrdersByStatus(Long customerId, Long orderStatusId) {
        List<Order> orders = orderRepository.findAllByCustomerIdAndOrderStatusIdOrderByOrderDateDesc(customerId, orderStatusId);
        return orders.stream().map(orderMapper::mapTo).collect(Collectors.toList());
    }
    private final OrderStatusRepository orderStatusRepository;
    private final EntityManager entityManager;
    public OrderDto cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        OrderStatus orderStatus = OrderStatus.builder().status(Status.CANCELLED).code(7).description("Đơn hàng" +
                " bị hủy theo yêu cầu của Khách Hàng. Lí do: "+reason).build();
        orderStatusRepository.save(orderStatus);
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        return orderMapper.mapTo(order);
    }

    public List<OrderDto> searchOrder(Long userId, String searchValue) {
        List<Order> orders = orderRepository.findAllByCustomer_IdOrderByOrderDateDesc(userId);

        for (Order order : orders) {
            if (order.getId().toString().equals(searchValue)) {
                orders.clear();
                orders.add(order);
                break;
            }
        }

        return orders.stream().map(orderMapper::mapTo).collect(Collectors.toList());
    }
}
