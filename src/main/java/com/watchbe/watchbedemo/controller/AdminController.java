package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.dto.UserDto;
import com.watchbe.watchbedemo.dto.WatchDto;
import com.watchbe.watchbedemo.mapper.WatchMapperImpl;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.*;
import com.watchbe.watchbedemo.model.account.Account;
import com.watchbe.watchbedemo.service.admin.impl.OrderManagementServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AccountRepository accountRepository;
    private final OrderManagementServiceImpl orderManagementService;
    private final WatchRepository watchRepository;
    private final WatchMapperImpl watchMapper;
    private final MovementRepository movementRepository;
    private final BrandRepository brandRepository;
    private final FamilyRepository familyRepository;
    private final ShippingRepository shippingRateRepository;


    @PostMapping("/products/update/price/{id}/{price}")
    public ResponseEntity<WatchDto> updatePrice(@PathVariable("id") Long id, @PathVariable Float price){
        Watch watch = watchRepository.findById(id).orElse(null);
        if(watch == null){
            return ResponseEntity.ok(null);
        }
        watch.setDefaultPrices(price/1000);
        watchRepository.save(watch);
        return ResponseEntity.ok(watchMapper.mapTo(watch));
    }
    @GetMapping("/get-all-brands")
    public ResponseEntity<List<Brand>> getAllBrands(){
        return ResponseEntity.ok(brandRepository.findAll());
    }
    @GetMapping("/get-family-by-brand/{brandName}")
    public ResponseEntity<List<Family>> getFamilyByBrand(@PathVariable String brandName){
        return ResponseEntity.ok(familyRepository.findByBrandName(brandName));
    }

    @PostMapping("/products/change-status/{id}/{status}")
    public ResponseEntity<WatchDto> changeProductStatus(@PathVariable Long id, @PathVariable String status){
        Watch watch = watchRepository.findById(id).orElse(null);
        if(status.equals("active")){
            watch.setActive(true);
        } else if(status.equals("archive")){
            watch.setActive(false);
        }
        watchRepository.save(watch);
        return ResponseEntity.ok(watchMapper.mapTo(watch));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){

        //find if the product exists in any order?
        //if it does, then return a message that the product cannot be deleted
        //else delete the product
        Watch watch = watchRepository.findById(id).orElse(null);
        if(watch == null){
            return ResponseEntity.ok("Product not found");
        }
        if(watch.getSoldQuantity()>0){
            return ResponseEntity.ok("cant");
        }else{
            watchRepository.delete(watch);
        }
        return ResponseEntity.ok("success");
    }
        static class UpdateItem {
            private String key;
            private String value;

            // Constructors, getters, setters
        }
    @PostMapping("/products/update/{id}")
    public ResponseEntity<WatchDto> updateProduct(@PathVariable Long id, @RequestBody UpdateItem[] updateList){
        System.out.println("updateList="+updateList);
        Watch watch = watchRepository.findById(id).orElse(null);
        if(watch == null){
            return ResponseEntity.ok(null);
        }
        for(UpdateItem item: updateList){
            if(item.key.equals("name")){
                watch.setName(item.value);
            } else if(item.key.equals("description")){
                watch.setDescription(item.value);
            } else if(item.key.equals("brand")){
                Brand b = brandRepository.findByBrandName(item.value);
                if(b == null){
                    b = new Brand();
                    b.setBrandName(item.value);
                    brandRepository.save(b);
                    watch.setBrand(b);
                }
            } else if(item.key.equals("family")){
                Family f = familyRepository.findByFamilyName(item.value);
                if(f == null){
                    f = new Family();
                    f.setFamilyName(item.value);
                    familyRepository.save(f);
                    watch.setFamily(f);
                }
            } else if(item.key.equals("gender")){
                watch.setGender(item.value);
            }else if(item.key.equals("origin")){
                watch.setOrigin(item.value);
            } else if (item.key.equals("movement")) {
                Movement m = watch.getMovement();
                m.setName(item.value);
                movementRepository.save(m);
            } else if (item.key.equals("functions")) {
                Movement m = watch.getMovement();
                m.setFunctions(item.value);
                movementRepository.save(m);
            }else if (item.key.equals("calendar")) {
                Movement m = watch.getMovement();
                m.setCalendar(item.value);
                movementRepository.save(m);
            }
        }
        watchRepository.save(watch);
        return null;
    }
    @GetMapping("/orders/today")
    public ResponseEntity<List<OrderDto>> getTodayOrders(){
        return ResponseEntity.ok(orderManagementService.getTodayOrders());
    }

    @GetMapping("/orders-by-date-range/{start}/{end}")
    public ResponseEntity<Page<OrderDto>> getOrdersByDateRange(@PathVariable Long start, @PathVariable Long end,
                                                               Pageable page){
        return ResponseEntity.ok(orderManagementService.getOrdersByDateRange(start, end,page));
    }

    @GetMapping("/total-orders-by-date-range/{start}/{end}")
    public ResponseEntity<List<OrderDto>> getTotalOrdersByDateRange(@PathVariable Long start, @PathVariable Long end
                                                            ){
        return ResponseEntity.ok(orderManagementService.getTotalOrdersByDateRange(start, end));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getAllOrders(Pageable page, @RequestParam(value = "query", required = false) String query){
        System.out.println("query="+query);
        if(query != null && query.equals("completed")){
            return ResponseEntity.ok(orderManagementService.getCompletedOrders(page));
        } else if (query != null && query.equals("uncaptured")) {
            return ResponseEntity.ok(orderManagementService.getUncapturedOrders(page));

        } else if (query != null && query.equals("preparing")) {
            return ResponseEntity.ok(orderManagementService.getPreparingOrders(page));

        } else if (query != null && query.equals("shipping")) {
            return ResponseEntity.ok(orderManagementService.getShippingOrders(page));

        } else if (query != null && query.equals("refunded")) {
            return ResponseEntity.ok(orderManagementService.getRefundedOrders(page));

        } else if (query != null && query.equals("cancelled")) {
            return ResponseEntity.ok(orderManagementService.getCancelledOrders(page));

        }
        return ResponseEntity.ok(orderManagementService.getAll(page));
    }


    @GetMapping
    public String get(){
        return "GET-ADMIN Controler";
    }

    @PostMapping
    public String post(){
        return "POST-ADMIN Controler";
    }

    @PutMapping
    public String put(){
        return "PUT-ADMIN Controler";
    }
    @DeleteMapping
    public String delete(){
        return "DELETE-ADMIN Controler";
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers(){
        List<Account> users = accountRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        users.forEach(user -> {
            UserDto u = UserDto.builder().email(user.getEmail()).build();
            userDtos.add(u);
        });
        return ResponseEntity.ok(userDtos);
    }

}
