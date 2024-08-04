package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {
    private final StorageService storageService;
//    @PostMapping("/fileSystem")
//    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam("image") MultipartFile file) throws IOException {
//        String uploadImage = storageService.uploadImageToFileSystem(file);
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(uploadImage);
//    }

    @GetMapping("/fileSystem/{fileName}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String fileName) throws IOException {
        byte[] imageData=storageService.downloadImageFromFileSystem(fileName);
//        List<byte[]> imageDatas = new ArrayList<>();
////        for (String fileName : fileNames) {
//        byte[] imageData = storageService.downloadImageFromFileSystem(fileName);
//        imageDatas.add(imageData);
////        }
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);

    }

    @GetMapping("/fileSystem/avatar/{fileName}")
    public ResponseEntity<?> downloadAvatarFromFileSystem(@PathVariable String fileName) throws IOException {
        byte[] imageData=storageService.downloadAvatarFromFileSystem(fileName);
//        List<byte[]> imageDatas = new ArrayList<>();
////        for (String fileName : fileNames) {
//        byte[] imageData = storageService.downloadImageFromFileSystem(fileName);
//        imageDatas.add(imageData);
////        }
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);

    }
}
