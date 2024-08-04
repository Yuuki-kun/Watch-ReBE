package com.watchbe.watchbedemo.service;

import com.watchbe.watchbedemo.dto.ChildReviewDto;
import com.watchbe.watchbedemo.dto.ReviewDto;
import com.watchbe.watchbedemo.dto.WatchDto;
import com.watchbe.watchbedemo.mapper.ChildReviewMapperImpl;
import com.watchbe.watchbedemo.mapper.ReviewMapperImpl;
import com.watchbe.watchbedemo.model.Customer;
import com.watchbe.watchbedemo.model.Image;
import com.watchbe.watchbedemo.model.Review;
import com.watchbe.watchbedemo.model.Watch;
import com.watchbe.watchbedemo.repository.ReviewRepository;
import com.watchbe.watchbedemo.repository.WatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    //    private final EntityManager entityManager;
    private final WatchRepository watchRepository;
    private final ReviewMapperImpl reviewMapper;
    private final ChildReviewMapperImpl childReviewMapper;

    private int calculateTotalReviews(Review review) {
        List<Review> childReviews = review.getChildReviews();
        int total = 0;
        for (Review childReview : childReviews) {
            total += 1 + calculateTotalReviews(childReview);
        }
        return total;
    }

    public int getTotalReviews(long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        int total = calculateTotalReviews(review);
        review.setTotalChildReviews(total);
        reviewRepository.save(review);
        return total;
    }

    public ReviewDto createReview(ReviewDto review) {
        Watch w = Watch.builder().id(review.getWatchId()).build();
        Customer c = Customer.builder().id(review.getCustomerId()).build();
        Review rv =
                Review.builder().childReviews(new ArrayList<>()).loves(0).comment(review.getComment()).customer(c).watch(w).datePosted(new Date()).ratingStars(review.getRatingStars()).build();
        Review rvSaved = reviewRepository.save(rv);
        //update watch rating
        Watch watch = watchRepository.findById(review.getWatchId()).get();
        watch.setStars((watch.getStars() == 0 ?  review.getRatingStars() :
                (watch.getStars() + review.getRatingStars()) / 2));
        watch.setTotalReviews(watch.getTotalReviews() + 1);
        watchRepository.save(watch);

        return reviewMapper.mapTo(rvSaved);
    }

    public  List<ChildReviewDto> replyReview(ReviewDto review) {
        Review parentReview = reviewRepository.findById(review.getParentId()).get();
        Customer c = Customer.builder().id(review.getCustomerId()).build();
        Review rv =
                Review.builder().childReviews(new ArrayList<>()).loves(0).comment(review.getComment()).customer(c).datePosted(new Date()).build();
        reviewRepository.save(rv);
        parentReview.addChildReview(rv);
        parentReview.setTotalChildReviews(parentReview.getTotalChildReviews() + 1);

        reviewRepository.save(parentReview);

        List<ChildReviewDto> reviews = new ArrayList<>();
        parentReview.getChildReviews().forEach(rvs -> {
            reviews.add(childReviewMapper.mapTo(rvs));
        });

        return reviews;    }

    public List<ChildReviewDto> getReviewsByParent(long parentId) {
        Review parentReview = reviewRepository.findById(parentId).get();
        List<ChildReviewDto> reviews = new ArrayList<>();
        parentReview.getChildReviews().forEach(review -> {
            reviews.add(childReviewMapper.mapTo(review));
        });
        reviews.sort((r1, r2) -> r2.getDatePosted().compareTo(r1.getDatePosted()));
        return reviews;
    }

    public int getTotalReviewsById(long reviewId) {
        return  reviewRepository.findById(reviewId).get().getTotalChildReviews();
    }

    String findMainImage(Watch watchDto) {
        for(Image image : watchDto.getImages()){
            if(image.getIsMain()){
                return image.getName();

            }
        }
        return "";
    }

    public List<ReviewDto> getReviewsByUser(long userId) {
        List<Review> reviews = reviewRepository.findAllByCustomer_Id(userId);
        List<ReviewDto> reviewDtos = new ArrayList<>();
        reviews.forEach(review -> {
            if(review.getWatch()!=null){
                Watch w = watchRepository.findById(review.getWatch().getId()).get();
                ReviewDto reviewDto = reviewMapper.mapTo(review);
                reviewDto.setWname(w.getName());
                reviewDto.setWimage(findMainImage(w));
                reviewDtos.add(reviewDto);
            }
        });
        return reviewDtos;
    }
}
