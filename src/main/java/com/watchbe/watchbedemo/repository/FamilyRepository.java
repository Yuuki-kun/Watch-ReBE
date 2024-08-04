package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    @Query("SELECT f FROM Family f WHERE f.brand.brandName = :brandName")
    List<Family> findByBrandName(@Param("brandName") String brandName);

    Family findByFamilyName(String familyName);
}
