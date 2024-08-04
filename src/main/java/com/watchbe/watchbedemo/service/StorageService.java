package com.watchbe.watchbedemo.service;

import com.watchbe.watchbedemo.dto.DialDto;
import com.watchbe.watchbedemo.model.Dial;
import com.watchbe.watchbedemo.model.Image;
import com.watchbe.watchbedemo.repository.DialRepository;
import com.watchbe.watchbedemo.repository.ImageRepository;
import com.watchbe.watchbedemo.service.utils.ImageUtils;
import jakarta.activation.FileDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final ImageRepository imageRepository;
    private final String FOLDER_PATH="/Users/tongcongminh/watch-img/";
    private final DialRepository dialRepository;

    //does not relate to the entity
    public String uploadImage(MultipartFile file) throws IOException {
        String filePath=FOLDER_PATH+file.getOriginalFilename();
        File isExist=new File(filePath);
        if(isExist.exists()){
            //image.png -> image1.png
            int lastDot = filePath.lastIndexOf(".");
            filePath = filePath.substring(0, lastDot) + "1" + filePath.substring(lastDot);
        }
        //save image to file system
        file.transferTo(new File(filePath));

        int lashSlash=filePath.lastIndexOf("/");
        return filePath.substring(lashSlash+1);
    }

    public Image uploadImageToFileSystem(MultipartFile file, boolean isMain) throws IOException {
        String filePath=FOLDER_PATH+file.getOriginalFilename();

        Image fileData=imageRepository.save(Image.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                //image as file path
                        .isMain(isMain)
                .image(filePath).build());

        file.transferTo(new File(filePath));

        if (fileData != null) {
            return fileData;
        }
        return null;
    }

    public Dial uploadImageToFileSystem(MultipartFile file, DialDto dial) throws IOException {
        if(file!=null){
            String filePath=FOLDER_PATH+file.getOriginalFilename();
            Dial savedDial =dialRepository.save(Dial.builder()
                    .type(dial.getType())
                    .color(dial.getColor())
                    .indexes(dial.getIndexes())
                    .hands(dial.getHands())
                    .luminescence(dial.getLuminescence())
                    //image as file path
                    .img(filePath).build());


            file.transferTo(new File(filePath));

            return savedDial;
        }else{
            Dial savedDial =dialRepository.save(Dial.builder()
                    .type(dial.getType())
                    .color(dial.getColor())
                    .indexes(dial.getIndexes())
                    .hands(dial.getHands())
                    .luminescence(dial.getLuminescence())
                    .build());
            return savedDial;
        }

    }



    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<Image> fileData = imageRepository.findByName(fileName);
        String filePath=fileData.get().getImage();
        byte[] images = Files.readAllBytes(new File(filePath).toPath());
        return images;
    }

    public byte[] downloadAvatarFromFileSystem(String fileName) throws IOException {
        return Files.readAllBytes(new File(FOLDER_PATH+"/"+fileName).toPath());
    }
}
