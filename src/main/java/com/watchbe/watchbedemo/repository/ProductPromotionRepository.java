package com.watchbe.watchbedemo.repository;

import com.watchbe.watchbedemo.model.Promotion_Details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


public interface ProductPromotionRepository extends JpaRepository<Promotion_Details, Long>{

    //find current promotion by product id
    @Query("SELECT pd.value FROM Promotion_Details pd " +
            "INNER JOIN pd.watch w " +
            "INNER JOIN pd.promotion pr " +
            "WHERE w.id = :productId AND pd.dateStart <= CURRENT_TIMESTAMP AND pd.dateEnd >= CURRENT_TIMESTAMP " +
            "AND pr.priority = (SELECT MAX(p.priority) FROM Promotion p)"
    )
    List<Float> findCurrentPromotionDetailsByProductId(Long productId);

//    @Query("SELECT pd FROM Promotion_Details pd " +
//            "INNER JOIN pd.watch w " +
//            "INNER JOIN pd.promotion pr " +
//            "WHERE w.id = :productId AND pd.dateStart <= CURRENT_DATE AND pd.dateEnd >= CURRENT_DATE " +
//            "AND pr.priority = (SELECT MAX(p.priority) FROM Promotion p)"
//    )
    @Query("SELECT pd FROM Promotion_Details pd " +
            "INNER JOIN pd.watch w " +
            "INNER JOIN pd.promotion pr " +
            "WHERE w.id = :productId AND pd.dateStart <= CURRENT_TIMESTAMP AND pd.dateEnd >= CURRENT_TIMESTAMP " +
            "AND pr.priority = (SELECT MAX(p.priority) FROM Promotion p) "+
            "AND pr.active = true"
    )
    List<Promotion_Details> findCurrentPromotionDetailsByProductIdForProduct(Long productId);

    //find promotion by product id and order date
    @Query("SELECT pd FROM Promotion_Details pd " +
            "INNER JOIN pd.watch w " +
            "INNER JOIN pd.promotion pr " +
            "WHERE w.id = :productId AND :orderDate BETWEEN pd.dateStart AND pd.dateEnd AND pr.priority = (SELECT MAX(p.priority) FROM Promotion p)"
            )
    List<Promotion_Details> findPromotionDiscountPercentageByProductIdAndOrderDate(@Param("productId") Long productId,
                                                                  @Param("orderDate") Date orderDate);

    List<Promotion_Details> findAllByPromotionIdAndDateEndAfter(Long promotionId, Date dateEnd);

    void deleteAllByPromotionIdAndWatchId(Long promotionId, Long productId);
    void deleteAllByPromotionId(Long promotionId);
}
