package com.watchbe.watchbedemo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shipping_rate")
public class ShippingRate {
    @Id
    @GeneratedValue
    private Long id;

    private String province;
    private float rate;
    private float freeShippingThreshold;
    private boolean active;
}
