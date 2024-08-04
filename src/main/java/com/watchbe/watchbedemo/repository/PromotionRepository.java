package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

}
