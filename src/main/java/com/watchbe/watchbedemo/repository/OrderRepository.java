package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.Order;
import com.watchbe.watchbedemo.model.Status;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findAllByCustomer_IdOrderByOrderDateDesc(Long id);
    List<Order> findAllByCustomer_IdOrderByOrderDateAsc(Long id, Pageable page);
    List<Order> findAllByCustomer_IdAndOrderDateBetweenOrderByOrderDateAsc(Long id, Date start,
                                                                           Date end,
                                                                           Pageable page);
    Order findByPaymentOrderId(String paymentId);
    List<Order> findAllByOrderByOrderDateDesc(Pageable page);
    List<Order> findAllByOrderStatus_statusOrderByOrderDateDesc(Status status, Pageable page);

    List<Order> findAllByCustomerIdAndOrderStatusIdOrderByOrderDateDesc(Long customerId, Long orderStatusId);
    List<Order> findAllByCustomer_IdAndOrderStatus_statusOrderByOrderDateDesc(Long customerId, Status status);

    @Query("SELECT o FROM Order o WHERE FUNCTION('date', o.orderDate) = :date order by case when o.orderStatus" +
            ".status= 'CREATED' then 1 when o.orderStatus.status= 'PENDING' then 2 when o.orderStatus.status= " +
            "'PREPARING' then 3 when o.orderStatus.status= 'SHIPPING' then 4 when o.orderStatus.status= 'DELIVERED' " +
            "then 5 else 6 end")
    List<Order> findAllByOrderDateEqualsToDate( Date date);

    Page<Order> findAllByOrderDateBetweenAndOrderStatusOrderByOrderDateDesc(Status status, Date start, Date end, Pageable page);
    List<Order> findAllByOrderDateBetweenOrderByOrderDateDesc(Date start, Date end);
    List<Order> findAllByOrderDateAfter(Date date);
}
