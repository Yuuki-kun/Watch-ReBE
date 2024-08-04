package com.watchbe.watchbedemo.mapper;

import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.dto.OrderStatusHistoryDto;
import com.watchbe.watchbedemo.model.OrderStatusHistory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusHisMapperImpl implements MapperDto<OrderStatusHistory, OrderStatusHistoryDto>{
    private final ModelMapper modelMapper;

    @Override
    public OrderStatusHistoryDto mapTo(OrderStatusHistory orderStatusHistory) {

        return modelMapper.map(orderStatusHistory, OrderStatusHistoryDto.class);

    }

    @Override
    public OrderStatusHistory mapFrom(OrderStatusHistoryDto orderStatusHistoryDto) {
        return modelMapper.map(orderStatusHistoryDto, OrderStatusHistory.class);
    }
}
