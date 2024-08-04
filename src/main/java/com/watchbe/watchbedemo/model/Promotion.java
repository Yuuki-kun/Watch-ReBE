package com.watchbe.watchbedemo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "promotion")
public class Promotion {
    @Id
    @GeneratedValue
    private Long id;

    private Float value;//
    private boolean active;//
    private String name;
    private String description;//
    //type = percentage or fixed
    private String type;//
    private Date dateStart;
    private Date dateEnd;
    private Date createdAt;
    private Integer priority;
    //scope = all or specific
    private String scope;
}
