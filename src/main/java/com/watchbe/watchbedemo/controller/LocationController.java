package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.model.ShippingRate;
import com.watchbe.watchbedemo.model.provinces.District;
import com.watchbe.watchbedemo.model.provinces.Province;
import com.watchbe.watchbedemo.model.provinces.Ward;
import com.watchbe.watchbedemo.repository.DistrictRepository;
import com.watchbe.watchbedemo.repository.ProvinceRepository;
import com.watchbe.watchbedemo.repository.ShippingRepository;
import com.watchbe.watchbedemo.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationController {
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final ShippingRepository shippingRateRepository;

    @PostMapping("/shipping-rates")
    public ResponseEntity<ShippingRate> createShippingRate(@RequestBody ShippingRate shippingRate){
        return ResponseEntity.ok(shippingRateRepository.save(shippingRate));
    }
    @DeleteMapping("/shipping-rates/{id}")
    public ResponseEntity<Void> deleteShippingRate(@PathVariable Long id){
        shippingRateRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/shipping-rates")
    public ResponseEntity<List<ShippingRate>> getShippingRates(){
        return ResponseEntity.ok(shippingRateRepository.findAll());
    }
    @GetMapping("/provinces")
    public ResponseEntity<List<Province>> getProvinces() {
        List <Province> provinces = provinceRepository.findAll();
        return ResponseEntity.ok(provinces);
    }
    @GetMapping("/provinces/{province}/districts")
    public ResponseEntity<List<District>> getDistrictsByProvince(@PathVariable String province) {
        System.out.println("province="+province);
        Province p = provinceRepository.getProvinceCodeByName(province);
        List<District> districts = districtRepository.findAllByProvinceCode(p.getCode());
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/districts/{districtc}/wards")
    public ResponseEntity<List<Ward>> getWardsByDistrict(@PathVariable String districtc) {
        District district = districtRepository.findByName(districtc);
        List <Ward> wards = wardRepository.findAllByDistrictCode(district.getCode());
        return ResponseEntity.ok(wards);
    }

}
