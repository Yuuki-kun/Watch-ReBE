package com.watchbe.watchbedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDto {
    private long id;

    private float value;
    private boolean active;
    private String name;
    private String description;
    //type = percentage or fixed
    private String type;
    private Date dateStart;
    private Date dateEnd;
    private Date createdAt;
    private int priority;
    private String scope;
}
