package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.Customer;
import com.watchbe.watchbedemo.model.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findCustomerByAccountId(Long accountId);
    Optional<Customer> findCustomerByEmail(String email);

    List<Customer> findAllCustomerByAccountCreatedAtBetween(Date start, Date end);
    Page<Customer> findAllCustomerByAccountCreatedAtBetween(Date start, Date end,
                                                                                    Pageable pageable);

}
