package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.dto.ChildReviewDto;
import com.watchbe.watchbedemo.dto.ReviewDto;
import com.watchbe.watchbedemo.model.Review;
import com.watchbe.watchbedemo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/reviews/parent/{parentId}")
    public ResponseEntity<List<ChildReviewDto>> getReviewsByParent(@PathVariable("parentId") long parentId){
        List<ChildReviewDto> reviews = reviewService.getReviewsByParent(parentId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/create-review")
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto review){
        ReviewDto rvDtoCreated = reviewService.createReview(review);
        return ResponseEntity.ok(rvDtoCreated);
    }

    @PostMapping("/create-multi-review")
    public ResponseEntity<String> createMultiReview(@RequestBody List<ReviewDto> review){
//        ReviewDto rvDtoCreated = reviewService.createReview(review);
        System.out.println(review);
        review.forEach(r -> {
            if (r.getComment() != null && r.getComment().length() > 0 && r.getRatingStars() > 0)
                reviewService.createReview(r);

        });
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/reply-review")
    public ResponseEntity<List<ChildReviewDto>> replyReview(@RequestBody ReviewDto review){
        System.out.println(review);
        List<ChildReviewDto>  rvDtoCreateds = reviewService.replyReview(review);
        return ResponseEntity.ok(rvDtoCreateds);
    }

    @GetMapping("/total-reviews/{reviewId}")
    public ResponseEntity<Integer> getTotalReviews(@PathVariable long reviewId){
        int total = reviewService.getTotalReviews(reviewId);
        return ResponseEntity.ok(total);
    }

    //get reviews by user
    @GetMapping("/reviews-by-user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable long userId){
        List<ReviewDto> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

}
