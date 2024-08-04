package com.watchbe.watchbedemo.mapper;

import com.watchbe.watchbedemo.dto.ChildReviewDto;
import com.watchbe.watchbedemo.dto.ReviewDto;
import com.watchbe.watchbedemo.model.Review;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChildReviewMapperImpl implements MapperDto<Review, ChildReviewDto>{
    private final ModelMapper modelMapper;

    @Override
    public ChildReviewDto mapTo(Review review) {
        return modelMapper.map(review, ChildReviewDto.class);
    }

    @Override
    public Review mapFrom(ChildReviewDto childReviewDto) {
        return modelMapper.map(childReviewDto, Review.class);
    }
}
