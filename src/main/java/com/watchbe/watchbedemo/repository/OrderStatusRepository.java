package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.OrderStatus;
import com.watchbe.watchbedemo.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long>{
    Optional<OrderStatus> findByStatus(Status status);
}
