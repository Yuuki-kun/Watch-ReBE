package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.ShippingRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingRepository extends JpaRepository<ShippingRate, Long> {
    ShippingRate findByProvince(String province);
}
