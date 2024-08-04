package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface WatchRepository extends JpaRepository<Watch, Long>,
        PagingAndSortingRepository<Watch, Long> {

    @Query("""
          select w from Watch w inner join Brand b on w.brand.id = b.id where b.id = :brandId
           """)
    List<Watch> findAllWatchesByBrand(Long brandId);

    Optional<Watch> findWatchByReference(String reference);
    @Query("SELECT w FROM Watch w WHERE w.gender = ?1 OR w.gender = 'Unisex'")

    List<Watch> findAllWatchByGender(String gender, Pageable pageable);

    List<Watch> findAllWatchByBrandId(Long brandId, Pageable pageable);

    List<Watch> findAllWatchByFamilyId(Long fid, Pageable page);

    Page<Watch> findAllByActiveTrue(Pageable pageable);
    List<Watch> findAllByActiveTrue();

    Page<Watch> findAllByActiveFalse(Pageable pageable);

    List<Watch> findTop4ByActiveTrueAndGenderOrderByCreatedDateDesc(String gender);
}
