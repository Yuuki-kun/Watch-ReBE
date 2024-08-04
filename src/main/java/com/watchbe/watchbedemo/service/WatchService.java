package com.watchbe.watchbedemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.watchbe.watchbedemo.dto.CollectionDto;
import com.watchbe.watchbedemo.dto.WatchDto;
import com.watchbe.watchbedemo.dto.WatchNoReview;
import com.watchbe.watchbedemo.model.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface WatchService {
    List<WatchDto> getAll();
    WatchDto findWatchByReference(String reference);
    Page<WatchDto> getAll(Pageable page);

    List<WatchDto> getSimilarWatches(String watchName);

    WatchDto save(List<MultipartFile> images, List<MultipartFile> dialImages, String watchData) throws JsonProcessingException;


    List<WatchDto> getWatchesByPriceRange(double min, double max);

    Page<WatchDto> getWatchesByFilters(List<String> cate, List<String> color, String movement, String start,
                                       String end, String brand, List<String> cs, List<String> bt, String typeF,
                                       Pageable pageable);

    List<WatchNoReview> fetchGenderWatches(String gender, Pageable page);

    List<CollectionDto> fetchCollections();

    List<WatchNoReview> fetchWatchByBrand(Long brandName, Pageable page);

    List<WatchNoReview> fetchWatchByFamily(Long fid, Pageable page);

    List<WatchDto> getPopularWatches(String time);
}
