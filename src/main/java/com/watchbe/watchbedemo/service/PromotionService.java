package com.watchbe.watchbedemo.service;

import com.watchbe.watchbedemo.dto.PromotionCreationRequest;
import com.watchbe.watchbedemo.dto.PromotionDto;
import com.watchbe.watchbedemo.dto.WatchDto;
import com.watchbe.watchbedemo.mapper.WatchMapperImpl;
import com.watchbe.watchbedemo.model.Promotion;
import com.watchbe.watchbedemo.model.Promotion_Details;
import com.watchbe.watchbedemo.model.Watch;
import com.watchbe.watchbedemo.repository.ProductPromotionRepository;
import com.watchbe.watchbedemo.repository.PromotionRepository;
import com.watchbe.watchbedemo.repository.WatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final WatchRepository watchRepository;
    private final ProductPromotionRepository promotionDetailsRepository;
    private final WatchMapperImpl watchMapper;
    public List<PromotionDto> createPromotion(PromotionDto promotionDto) {
        if(promotionDto.getScope().equals("all")){

            Promotion promotion = Promotion.builder()
                    .name(promotionDto.getName())
                    .description(promotionDto.getDescription())
                    .type(promotionDto.getType())
                    .createdAt(promotionDto.getCreatedAt())
                    .dateStart(promotionDto.getDateStart())
                    .dateEnd(promotionDto.getDateEnd())
                    .priority(promotionDto.getPriority())
                    .active(true)
                    .createdAt(new Date())
                    .value(promotionDto.getValue())
                    .scope(promotionDto.getScope())
                    .build();
            promotionRepository.save(promotion);

            List<Watch> watches = watchRepository.findAll();
            for (Watch watch : watches) {
                Promotion_Details promotion_details = Promotion_Details.builder()
                        .watch(watch)
                        .promotion(promotion)
                        .value(promotionDto.getValue())
                        .Applied(false)
                        .dateStart(promotionDto.getDateStart())
                        .dateEnd(promotionDto.getDateEnd())
                        .build();
                promotionDetailsRepository.save(promotion_details);
            }
        }


        List<Promotion> promotions = promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PromotionDto> promotionDtos = promotions.stream().map(promotion1 -> PromotionDto.builder()
                .id(promotion1.getId())
                .name(promotion1.getName())
                .description(promotion1.getDescription())
                .type(promotion1.getType())
                .createdAt(promotion1.getCreatedAt())
                .dateStart(promotion1.getDateStart())
                .dateEnd(promotion1.getDateEnd())
                .priority(promotion1.getPriority())
                .active(promotion1.isActive())
                .value(promotion1.getValue())
                .scope(promotion1.getScope())

                .build()).toList();

        return promotionDtos;
    }

    public List<PromotionDto> getPromotions() {
        List<Promotion> promotions = promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PromotionDto> promotionDtos = promotions.stream().map(promotion1 -> PromotionDto.builder()
                .id(promotion1.getId())
                .name(promotion1.getName())
                .description(promotion1.getDescription())
                .type(promotion1.getType())
                .createdAt(promotion1.getCreatedAt())
                .dateStart(promotion1.getDateStart())
                .dateEnd(promotion1.getDateEnd())
                .priority(promotion1.getPriority())
                .active(promotion1.isActive())
                .value(promotion1.getValue())
                .scope(promotion1.getScope())
                .build()).toList();
        return promotionDtos;
    }

    public ResponseEntity<List<PromotionDto>> createSpecificPromotion(PromotionDto promotionDto, List<Long> watchIds) {
        Promotion promotion = Promotion.builder()
                .name(promotionDto.getName())
                .description(promotionDto.getDescription())
                .type(promotionDto.getType())
                .createdAt(promotionDto.getCreatedAt())
                .dateStart(promotionDto.getDateStart())
                .dateEnd(promotionDto.getDateEnd())
                .priority(promotionDto.getPriority())
                .active(true)
                .createdAt(new Date())
                .value(promotionDto.getValue())
                .scope(promotionDto.getScope())
                .build();
        promotionRepository.save(promotion);

            for (Long watchId : watchIds) {
                Watch watch = watchRepository.findById(watchId).orElse(null);
                if(watch == null){
                    continue;
                }
                Promotion_Details promotion_details = Promotion_Details.builder()
                        .watch(watch)
                        .promotion(promotion)
                        .value(promotionDto.getValue())
                        .Applied(false)
                        .dateStart(promotionDto.getDateStart())
                        .dateEnd(promotionDto.getDateEnd())
                        .build();
                promotionDetailsRepository.save(promotion_details);
            }

            List<Promotion> promotions = promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            List<PromotionDto> promotionDtos = promotions.stream().map(promotion1 -> PromotionDto.builder()
                    .id(promotion1.getId())
                    .name(promotion1.getName())
                    .description(promotion1.getDescription())
                    .type(promotion1.getType())
                    .createdAt(promotion1.getCreatedAt())
                    .dateStart(promotion1.getDateStart())
                    .dateEnd(promotion1.getDateEnd())
                    .priority(promotion1.getPriority())
                    .active(promotion1.isActive())
                    .value(promotion1.getValue())
                    .scope(promotion1.getScope())
                    .build()).toList();

            return ResponseEntity.ok(promotionDtos);
    }

    //get all products in promotion and it's active
    public List<WatchDto> getProductsPromotion(Long promotionId) {
//        Promotion promotion = promotionRepository.findById(promotionId).orElse(null);
//        if(promotion == null){
//            return null;
//        }
        List<Promotion_Details> promotion_details =
                promotionDetailsRepository.findAllByPromotionIdAndDateEndAfter(promotionId,
                new Date());
        promotion_details.removeIf(promotion_details1 -> promotion_details1.getPromotion().isActive() == false);
//        List<PromotionDto> promotionDtos = promotion_details.stream().map(promotion_details1 -> PromotionDto.builder()
//                .id(promotion_details1.getId())
//                .name(promotion_details1.getPromotion().getName())
//                .description(promotion_details1.getPromotion().getDescription())
//                .type(promotion_details1.getPromotion().getType())
//                .createdAt(promotion_details1.getPromotion().getCreatedAt())
//                .dateStart(promotion_details1.getPromotion().getDateStart())
//                .dateEnd(promotion_details1.getPromotion().getDateEnd())
//                .priority(promotion_details1.getPromotion().getPriority())
//                .active(promotion_details1.getPromotion().isActive())
//                .value(promotion_details1.getPromotion().getValue())
//                .scope(promotion_details1.getPromotion().getScope())
//                .build()).toList();

        List<Watch> watches = promotion_details.stream().map(Promotion_Details::getWatch).toList();
        List<WatchDto> watchDtos =  watches.stream().map(watchMapper::mapTo).toList();
        return watchDtos;
    }

    public PromotionDto updatePromotion(Long promotionId, PromotionCreationRequest promotionDto) {
        System.out.println(promotionDto);
        System.out.println(promotionId);
        System.out.println("list id = " + promotionDto.getWatchIds());
        Promotion promotion = promotionRepository.findById(promotionId).orElse(null);
        //update promotion if exist and have different field
        if(promotion != null){
            promotion.setName(promotionDto.getPromotionDto().getName());
            promotion.setDescription(promotionDto.getPromotionDto().getDescription());
            promotion.setType(promotionDto.getPromotionDto().getType());
            promotion.setDateStart(promotionDto.getPromotionDto().getDateStart());
            promotion.setDateEnd(promotionDto.getPromotionDto().getDateEnd());
            promotion.setPriority(promotionDto.getPromotionDto().getPriority());
            promotion.setActive(promotionDto.getPromotionDto().isActive());
            promotion.setValue(promotionDto.getPromotionDto().getValue());
            promotion.setScope(promotionDto.getPromotionDto().getScope());
            promotionRepository.save(promotion);

            //update value in promotion details
            List<Long> productIdInPromotionDetails = new ArrayList<>();
            List<Promotion_Details> promotion_details = promotionDetailsRepository.findAllByPromotionIdAndDateEndAfter(promotionId, new Date());
            for (Promotion_Details promotion_details1 : promotion_details) {
                promotion_details1.setValue(promotionDto.getPromotionDto().getValue());
                promotionDetailsRepository.save(promotion_details1);
                productIdInPromotionDetails.add(promotion_details1.getWatch().getId());
            }

            //3 4 5 6
            //3 7
            if(promotionDto.getWatchIds().size()!=0){
                for(Long productId : productIdInPromotionDetails){
                    if(!promotionDto.getWatchIds().contains(productId)){
                        promotionDetailsRepository.deleteAllByPromotionIdAndWatchId(promotionId, productId);
                    }
                }
                for (Long productId : promotionDto.getWatchIds()) {
                    if(productIdInPromotionDetails.contains(productId)){
                        continue;
                    }
                    Watch watch = Watch.builder().id(productId).build();
                    if(watch == null){
                        continue;
                    }
                    Promotion_Details promotion_details1 = Promotion_Details.builder()
                            .watch(watch)
                            .promotion(promotion)
                            .value(promotionDto.getPromotionDto().getValue())
                            .Applied(false)
                            .dateStart(promotionDto.getPromotionDto().getDateStart())
                            .dateEnd(promotionDto.getPromotionDto().getDateEnd())
                            .build();
                    promotionDetailsRepository.save(promotion_details1);
                }
            }else{
                promotionDetailsRepository.deleteAllByPromotionId(promotionId);
            }
            return PromotionDto.builder()
                    .id(promotion.getId())
                    .name(promotion.getName())
                    .description(promotion.getDescription())
                    .type(promotion.getType())
                    .createdAt(promotion.getCreatedAt())
                    .dateStart(promotion.getDateStart())
                    .dateEnd(promotion.getDateEnd())
                    .priority(promotion.getPriority())
                    .active(promotion.isActive())
                    .value(promotion.getValue())
                    .scope(promotion.getScope())
                    .build();
        }
        return null;


    }
}
