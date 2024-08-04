package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.dto.BrandDto;
import com.watchbe.watchbedemo.dto.FamilyDto;
import com.watchbe.watchbedemo.dto.MovementDto;
import com.watchbe.watchbedemo.repository.BrandRepository;
import com.watchbe.watchbedemo.repository.FamilyRepository;
import com.watchbe.watchbedemo.repository.MovementRepository;
import com.watchbe.watchbedemo.repository.WatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/general-info")
@RequiredArgsConstructor
public class GeneralInfoController {
    private final MovementRepository movementRepository;
    private final WatchRepository watchRepository;
    private final BrandRepository brandRepository;
    private final FamilyRepository familyRepository;

    @GetMapping("/movements")
    public ResponseEntity<List<MovementDto>> getMovements() {
        List<MovementDto> movements = movementRepository.findAll().stream()
                .map(movement -> MovementDto.builder()
                        .id(movement.getId())
                        .name(movement.getName())
                        .type(movement.getType())
                        .power(movement.getPower())
                        .powerReserve(movement.getPowerReserve())
                        .origin(movement.getOrigin())
//                        .display(movement.getDisplay())
//                        .chronograph(movement.getChronograph())
//                        .hands(movement.getHands())
//                        .features(movement.getFeatures())
                        .functions(movement.getFunctions())
                        .calendar(movement.getCalendar())
                        .caliber(movement.getCaliber())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/brands")
    public ResponseEntity<List<BrandDto>> getBrands() {
        List<BrandDto> brands = brandRepository.findAll().stream()
                .map(brand -> BrandDto.builder()
                        .id(brand.getId())
                        .brandName(brand.getBrandName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(brands);
    }
    @GetMapping("/families/{brandName}")
    public ResponseEntity<List<FamilyDto>> getFamiliesByBrandName(@PathVariable String brandName) {
        List<FamilyDto> families = familyRepository.findByBrandName(brandName).stream()
                .map(family -> FamilyDto.builder()
                        .id(family.getId())
                        .familyName(family.getFamilyName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(families);
    }

}
