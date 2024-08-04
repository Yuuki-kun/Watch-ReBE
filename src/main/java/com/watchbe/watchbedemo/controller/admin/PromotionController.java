package com.watchbe.watchbedemo.controller.admin;

import com.watchbe.watchbedemo.dto.PromotionCreationRequest;
import com.watchbe.watchbedemo.dto.PromotionDto;
import com.watchbe.watchbedemo.dto.WatchDto;
import com.watchbe.watchbedemo.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin-promotions-mgt")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;

    @GetMapping("/get-promotions")
    public ResponseEntity<List<PromotionDto>> getPromotions() {
        return ResponseEntity.ok(promotionService.getPromotions());
    }

    @PostMapping("/create-promotions")
    public ResponseEntity<List<PromotionDto>> getPromotions(@RequestBody PromotionDto promotionDto) {
        System.out.println(promotionDto);
        return ResponseEntity.ok(promotionService.createPromotion(promotionDto));
    }

    @PostMapping("/create-promotions/specific")
    public ResponseEntity<List<PromotionDto>> createSpecificPromotions(@RequestBody PromotionCreationRequest request
                                                                       ) {
        PromotionDto promotionDto = request.getPromotionDto();
        List<Long> watchIds = request.getWatchIds();
        System.out.println(promotionDto);
        System.out.println(watchIds);

        return promotionService.createSpecificPromotion(promotionDto, watchIds);
    }

    //get products applied promotion
    @GetMapping("/get-products-promotion/{promotionId}")
    public ResponseEntity<List<WatchDto>> getProductsPromotion(@PathVariable Long promotionId) {
        return ResponseEntity.ok(promotionService.getProductsPromotion(promotionId));
    }

    @PutMapping("/update/{promotionId}")
    public ResponseEntity<PromotionDto> updatePromotion(@PathVariable Long promotionId, @RequestBody PromotionCreationRequest promotionDto) {
        System.out.println(promotionDto);
        return ResponseEntity.ok(promotionService.updatePromotion(promotionId, promotionDto));
    }
}
