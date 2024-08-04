package com.watchbe.watchbedemo.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchbe.watchbedemo.dto.WatchDto;
import com.watchbe.watchbedemo.model.Watch;
import com.watchbe.watchbedemo.service.WatchServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin-watch-mgt")
@RequiredArgsConstructor
public class WatchManagementController {
    private final WatchServiceImpl watchService;
//    @PostMapping("/add-watch")
//    public ResponseEntity<?> addWatch(@RequestBody WatchDto watchDto) {
//        return ResponseEntity.ok(watchService.save(watchDto));
//    }

    @PostMapping(value = "/add-watch")
    public ResponseEntity<?> addWatch(@RequestParam("img") List<MultipartFile> images,
                                      @RequestParam("formData") String data,
                                      @RequestParam(value = "dialImg", required = false) List<MultipartFile> dialImages
                                      ) throws JsonProcessingException {
        System.out.println("data="+data);
        WatchDto savedWatch =  watchService.save(images, dialImages,data);
        System.out.println("watchDto = " + savedWatch);
        return ResponseEntity.ok(savedWatch);
    }

    //find popular watches
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularWatches(@RequestParam("time") String time) {
        List<WatchDto> watches = watchService.getPopularWatches(time);
        return ResponseEntity.ok(watches);
    }
}
