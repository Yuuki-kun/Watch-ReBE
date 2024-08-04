package com.watchbe.watchbedemo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PromotionCreationRequest {
    private PromotionDto promotionDto;
    private List<Long> watchIds;
}
