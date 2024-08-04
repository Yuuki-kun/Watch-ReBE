package com.watchbe.watchbedemo.mapper;

import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.dto.ReviewDto;
import com.watchbe.watchbedemo.model.Order;
import com.watchbe.watchbedemo.model.Review;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapperImpl implements MapperDto<Review, ReviewDto>{
    private final ModelMapper modelMapper;

    @Override
    public ReviewDto mapTo(Review review) {
        return modelMapper.map(review, ReviewDto.class);
    }

    @Override
    public Review mapFrom(ReviewDto reviewDto) {
        return modelMapper.map(reviewDto, Review.class);
    }
}
