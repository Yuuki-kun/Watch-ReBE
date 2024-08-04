package com.watchbe.watchbedemo.service.admin.impl;

import com.watchbe.watchbedemo.dto.*;
import com.watchbe.watchbedemo.exception.NotFoundException;
import com.watchbe.watchbedemo.model.Customer;
import com.watchbe.watchbedemo.model.Order;
import com.watchbe.watchbedemo.model.ShippingAddress;
import com.watchbe.watchbedemo.model.account.Account;
import com.watchbe.watchbedemo.repository.AccountRepository;
import com.watchbe.watchbedemo.repository.CustomerRepository;
import com.watchbe.watchbedemo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServices {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    public Page<CustomerDto> getAllUsers(Pageable page){
        Page<Customer> cus = customerRepository.findAll(page);
        Page<CustomerDto> resultPage = cus.map(customer ->{
                Account account = accountRepository.findByEmail(customer.getEmail()).get();
                return CustomerDto.builder()
                        .id(customer.getId())
                        .email(customer.getEmail())
                        .gender(customer.getGender())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .avatarLink(customer.getAvatarLink())
                        .phoneNumber(customer.getPhoneNumber())
                        .lastLogin(account.getLastLogin())
                        .createdAt(account.getCreatedAt())
                        .enabled(account.isEnabled() ? "Enabled" : "Disabled")
                        .build();});
        List<CustomerDto> rsl = resultPage.stream()
                .filter(customerDto -> customerDto.getId() != 99999)
                .collect(Collectors.toList());


        return new PageImpl<>(rsl, page, cus.getTotalElements());
    }

    public AllUserRelatedDataDto getAllUserRelatedData(Long cusId, Pageable page){
        Customer customer = customerRepository.findById(cusId).orElseThrow( ()->new RuntimeException("Customer not found"));
        List<ShippingAddress> shippingAddress = customer.getShippingAddresses();
        List<Order> orders = orderRepository.findAllByCustomer_IdOrderByOrderDateAsc(cusId,page);

        return AllUserRelatedDataDto.builder()
                .customerDto(CustomerDto.builder().id(customer.getId()).dob(customer.getDob()).email(customer.getEmail())
                        .gender(customer.getGender())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .phoneNumber(customer.getPhoneNumber())
                        .avatarLink(customer.getAvatarLink())

                        .build())
                .shippingAddressDtos(shippingAddress.stream().map(shippingAddress1 -> ShippingAddressDto.builder()
                        .id(shippingAddress1.getId())
                        .address(shippingAddress1.getAddress())
                        .city(shippingAddress1.getCity())
                        .ward(shippingAddress1.getWard())
                        .district(shippingAddress1.getDistrict())
                        .type(shippingAddress1.getType())
                        .isDefault(shippingAddress1.getIsDefault())
                        .phone(shippingAddress1.getPhone())
                        .name(shippingAddress1.getName())
                        .build()).collect(Collectors.toList()))
                .orderDtos(
                        orders.stream().map(order -> OrderDto.builder()
                                .id(order.getId())
                                .orderDate(order.getOrderDate())
                                .orderStatus(OrderStatusDto.builder().status(order.getOrderStatus().getStatus()).build())
                                .amount(order.getAmount())

                                .build()).collect(Collectors.toList())
                )
                .build();

    }

    public AllUserRelatedDataDto getAllUserRelatedData(Long cusId, Long startDate, Long endDate, Pageable page){
        Customer customer = customerRepository.findById(cusId).orElseThrow( ()->new RuntimeException("Customer not found"));
        List<ShippingAddress> shippingAddress = customer.getShippingAddresses();
        Date start = new Date(startDate);
        Date end = new Date(endDate);
        List<Order> orders = orderRepository.findAllByCustomer_IdAndOrderDateBetweenOrderByOrderDateAsc(cusId, start, end,page);

        return AllUserRelatedDataDto.builder()
                .customerDto(CustomerDto.builder().id(customer.getId()).dob(customer.getDob()).email(customer.getEmail())
                        .gender(customer.getGender())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .phoneNumber(customer.getPhoneNumber())
                        .avatarLink(customer.getAvatarLink())

                        .build())
                .shippingAddressDtos(shippingAddress.stream().map(shippingAddress1 -> ShippingAddressDto.builder()
                        .id(shippingAddress1.getId())
                        .address(shippingAddress1.getAddress())
                        .city(shippingAddress1.getCity())
                        .ward(shippingAddress1.getWard())
                        .district(shippingAddress1.getDistrict())
                        .type(shippingAddress1.getType())
                        .isDefault(shippingAddress1.getIsDefault())
                        .phone(shippingAddress1.getPhone())
                        .name(shippingAddress1.getName())
                        .build()).collect(Collectors.toList()))
                .orderDtos(
                        orders.stream().map(order -> OrderDto.builder()
                                .id(order.getId())
                                .orderDate(order.getOrderDate())
                                .orderStatus(OrderStatusDto.builder().status(order.getOrderStatus().getStatus()).build())
                                .amount(order.getAmount())

                                .build()).collect(Collectors.toList())
                )
                .build();

    }


    public CustomerDto changeAccountStatus(Long cusId, String status){
        Customer customer = customerRepository.findById(cusId).orElseThrow( ()->new NotFoundException("Customer not found"));
        Account account = customer.getAccount();

        account.setEnabled(status.equals("enable"));
        accountRepository.save(account);
        return  CustomerDto.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .gender(customer.getGender())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .avatarLink(customer.getAvatarLink())
                .phoneNumber(customer.getPhoneNumber())
                .enabled(status.equals("enable") ? "Enabled" : "Disabled")
                .build();
    }

    public Object deleteAccount(Long cusId) {
        Customer customer = customerRepository.findById(cusId).orElseThrow( ()->new NotFoundException("Customer not found"));
        Account account = customer.getAccount();
        accountRepository.delete(account);
        return "Deleted";
    }

    public List<CustomerDto> getUserCreatedByDateToDate(Long fromDate, Long toDate) {
        Date d = new Date(fromDate);
        Date d1 = new Date(toDate);
        System.out.println("fromDate = " + d);
        System.out.println("toDate = " + d1);
        List<Customer> cus = customerRepository.findAllCustomerByAccountCreatedAtBetween(d, d1);
        List<CustomerDto> rs = new ArrayList<>();
        cus.forEach(customer -> {
            if(customer.getId() != 99999) {
                Account account = accountRepository.findByEmail(customer.getEmail()).get();

                CustomerDto c = CustomerDto.builder()
                        .id(customer.getId())
                        .email(customer.getEmail())
                        .gender(customer.getGender())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .avatarLink(customer.getAvatarLink())
                        .phoneNumber(customer.getPhoneNumber())
                        .createdAt(account.getCreatedAt())
                        .build();
                rs.add(c);
            }
        });
        return rs;
    }

    public Page<CustomerDto> getUserCreatedByDateToDate(Long fromDate, Long toDate, Pageable page) {
        Date d = new Date(fromDate);
        Date d1 = new Date(toDate);
        System.out.println("fromDate = " + d);
        System.out.println("toDate = " + d1);
//        Account accountAD = Account.builder().id(99999L).build();
        Page<Customer> cus = customerRepository.findAllCustomerByAccountCreatedAtBetween(d, d1, page);
        List<CustomerDto> rs = new ArrayList<>();

//        cus.forEach(customer -> {
//            if(customer.getId() != 99999) {
//                Account account = accountRepository.findByEmail(customer.getEmail()).get();
//
//                CustomerDto c = CustomerDto.builder()
//                        .id(customer.getId())
//                        .email(customer.getEmail())
//                        .gender(customer.getGender())
//                        .firstName(customer.getFirstName())
//                        .lastName(customer.getLastName())
//                        .avatarLink(customer.getAvatarLink())
//                        .phoneNumber(customer.getPhoneNumber())
//                        .createdAt(account.getCreatedAt())
//                        .build();
//                rs.add(c);
//            }
//        });
        return cus.map(customer ->{
            Account account = accountRepository.findByEmail(customer.getEmail()).get();
            return CustomerDto.builder()
                    .id(customer.getId())
                        .email(customer.getEmail())
                        .gender(customer.getGender())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .avatarLink(customer.getAvatarLink())
                        .phoneNumber(customer.getPhoneNumber())
                        .createdAt(account.getCreatedAt())
                        .enabled(account.isEnabled() ? "Enabled" : "Disabled")
                        .build();
        });
    }

}
