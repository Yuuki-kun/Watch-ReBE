package com.watchbe.watchbedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long watchId;
    private Long customerId;
    private String comment;
    private float ratingStars;
    private Date datePosted;
    private List<ReviewDto> childReviews;
    private CustomerDto customerDto;
    private long loves;
    private Long parentId;
    private Integer totalChildReviews;
    private String wname;
    private String wimage;
}
