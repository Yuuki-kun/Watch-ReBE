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
@Table(name = "promotion_details")
public class Promotion_Details {
    @Id
    @GeneratedValue
    private Long id;

    private Float value;
    private Date dateApplied;
    private Boolean Applied;
    private Date dateStart;
    private Date dateEnd;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "watch_id")
    private Watch watch;


}
