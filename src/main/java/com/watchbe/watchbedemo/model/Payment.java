package com.watchbe.watchbedemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_")
public class Payment {
    @Id
    @GeneratedValue
    private Long id;

    private String paymentIntentId;
    private Date date;
    private String type;
    private String paymentMethod;

    private String brand;
    private String cvcCheck;
    private String country;
    private String last4;
    private String network;
    private String paymentMethodType;
    private String receiptEmail;
    private Long expMonth;
    private Long expYear;
    private String fingerprint;
    private String funding;
    private Date captureBefore;
    private String capturedAt;
}
