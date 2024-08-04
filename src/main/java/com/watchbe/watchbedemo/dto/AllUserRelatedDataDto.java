package com.watchbe.watchbedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllUserRelatedDataDto {
    private CustomerDto customerDto;
    private List<ShippingAddressDto> shippingAddressDtos;
    private List<OrderDto> orderDtos;
}
