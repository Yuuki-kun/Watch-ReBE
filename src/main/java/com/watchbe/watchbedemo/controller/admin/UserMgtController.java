package com.watchbe.watchbedemo.controller.admin;

import com.watchbe.watchbedemo.dto.CustomerDto;
import com.watchbe.watchbedemo.service.admin.impl.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin-users-mgt")
@RequiredArgsConstructor
public class UserMgtController {

    private final UserServices userServices;

    @GetMapping
    public ResponseEntity<Page<CustomerDto>> getUsers(Pageable page){
        return ResponseEntity.ok(userServices.getAllUsers(page));
    }

    @GetMapping("/all-info/{cusId}")
    public ResponseEntity<?> getAllInfo(@PathVariable Long cusId,
                                        Pageable page){

        return ResponseEntity.ok(userServices.getAllUserRelatedData(cusId, page));
    }

    @GetMapping("/all-info-date/{cusId}/{fromDate}/{toDate}")
    public ResponseEntity<?> getAllInfo(@PathVariable Long cusId,
                                        @PathVariable Long fromDate,
                                        @PathVariable Long toDate,
                                        Pageable page){

        return ResponseEntity.ok(userServices.getAllUserRelatedData(cusId, fromDate, toDate,page));
    }

    @PutMapping("/change-account-status/{cusId}/{status}")
    public ResponseEntity<?> changeAccountStatus(@PathVariable Long cusId, @PathVariable String status){
        return ResponseEntity.ok(userServices.changeAccountStatus(cusId, status));
    }

    @DeleteMapping("/delete-account/{cusId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long cusId){
        return ResponseEntity.ok(userServices.deleteAccount(cusId));
    }

    @GetMapping("/get-user-created-by-date-to-date/{fromDate}/{toDate}")
    public ResponseEntity<?> getUserCreatedByDateToDate(@PathVariable Long fromDate, @PathVariable Long toDate
                                                   ){
        return ResponseEntity.ok(userServices.getUserCreatedByDateToDate(fromDate, toDate));
    }

    @GetMapping("/get-user-created-by-date-to-date-page/{fromDate}/{toDate}")
    public ResponseEntity<?> getUserCreatedByDateToDatePage(@PathVariable Long fromDate, @PathVariable Long toDate, Pageable page
    ){
        return ResponseEntity.ok(userServices.getUserCreatedByDateToDate(fromDate, toDate, page));
    }
}
