package com.watchbe.watchbedemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movement")
public class Movement {
    @Id
    @GeneratedValue
    private Long id;
    private String reference;

    private String name;//ex: Citizen Caliber Eco-Drive F900; ok
    //Automatic, Quartz, Manual, Mechanical
    @Enumerated(EnumType.STRING)
    private MovementType type;//
    private String power;  //
    private long jewels;
    //millimeter
//    private float diameter;
    private String powerReserve;
    //Hz, bph(beat per hour)
//    private float frequency;
    private String origin;
//    private String display;
//    private String date;
//    private String chronograph;
//    private String hands;
//    private String features;
    @Column(length = 1000)
    private String functions;
    private String calendar;
    private String caliber;
//    private String acoustic;
//    private String additionalFunctions;
//    private String precision;

}
