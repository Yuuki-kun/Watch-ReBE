package com.watchbe.watchbedemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.watchbe.watchbedemo.model.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class MovementDto {
    private String name;//ex: Citizen Caliber Eco-Drive F900; ok
    //Automatic, Quartz, Manual, Mechanical
    private Long id;
    private MovementType type;
    private String power;  //
//    private long jewels;
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
    private String functions;
    private String calendar;
    private String caliber;

    private String reference;
    private boolean addNew;
}
