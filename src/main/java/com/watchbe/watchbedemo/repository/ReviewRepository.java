package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByCustomer_Id(long userId);
}
