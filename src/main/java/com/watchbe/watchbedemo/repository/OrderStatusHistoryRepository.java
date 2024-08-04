package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long>{
    List<OrderStatusHistory> findAllByOrderId(Long id);
}
