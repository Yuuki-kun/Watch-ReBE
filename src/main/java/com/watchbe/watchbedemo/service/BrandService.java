package com.watchbe.watchbedemo.service;

import com.watchbe.watchbedemo.dto.BrandDto;
import com.watchbe.watchbedemo.model.Brand;
import com.watchbe.watchbedemo.repository.BrandRepository;
import com.watchbe.watchbedemo.service.utils.Similarity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    public List<BrandDto> getSimilarBrand(String brandName) {
        List<Brand> brands = brandRepository.findAll();
        List<BrandDto> similarBrands = new ArrayList<>();
        brands.forEach(brand -> {
            double similarity = Similarity.similarity(brand.getBrandName(), brandName);
            if(similarity > 0.3) {
                similarBrands.add(BrandDto.builder().brandName(brand.getBrandName()).build());
            }
        });

        return similarBrands;
    }
}
