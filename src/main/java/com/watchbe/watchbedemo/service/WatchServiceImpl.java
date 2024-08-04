package com.watchbe.watchbedemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchbe.watchbedemo.dto.*;
import com.watchbe.watchbedemo.exception.NotFoundException;
import com.watchbe.watchbedemo.mapper.WatchMapperImpl;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.*;
import com.watchbe.watchbedemo.service.utils.Similarity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchServiceImpl implements WatchService {
    private final WatchRepository watchRepository;
    private final WatchMapperImpl watchMapper;
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final CaseRepository caseRepository;
    private final Similarity similarity;
    private final StorageService storageService;
    private final BrandRepository brandRepository;
    private final FamilyRepository familyRepository;
    private final BraceletRepository braceletRepository;
    private final MovementRepository movementRepository;
    private final OrderRepository orderRepository;


    @Override
    public List<WatchDto> getAll() {
        System.out.println("dang goi find all watches");
        List<Watch> watches = watchRepository.findAll();
        List<WatchDto> watchDtos = watches.stream().map(watch -> {
//            watch.getReviews().forEach(review ->
//            {
//                review.setTotalChildReviews(reviewService.getTotalReviews(review.getId()));
//                reviewRepository.save(review);
//            });

            return watchMapper.mapTo(watch);
        }).toList();
        return watchDtos;
    }

    @Override
    public WatchDto findWatchByReference(String reference) {
        Watch watch =
                watchRepository.findWatchByReference(reference)
                        .orElseThrow(() -> new NotFoundException(String.format("Watch with reference %s not found!",
                                reference)));
        watch.getReviews().forEach(review ->
        {
            review.setTotalChildReviews(reviewService.getTotalReviews(review.getId()));
            reviewRepository.save(review);

        });
        watch.getReviews().sort((r1, r2) -> r2.getDatePosted().compareTo(r1.getDatePosted()));
        return watchMapper.mapTo(watch);
    }

    @Override
    public Page<WatchDto> getAll(Pageable page) {
        return watchRepository.findAllByActiveTrue(page).map(watchMapper::mapTo);
//        return watchRepository.findAll(page).map(watchMapper::mapTo);
    }

    @Override
    public List<WatchDto> getSimilarWatches(String watchName) {
        System.out.println("goi find all watches");

        List<WatchDto> watches = getAll();
        return similarity.getSimilarWatchNames(watches, watchName);
    }

    @Override
    public WatchDto save(List<MultipartFile> images, List<MultipartFile> dialImages, String watchData) throws JsonProcessingException {
        System.out.println(watchData);
        ObjectMapper objectMapper = new ObjectMapper();
        WatchDto watchDto = objectMapper.readValue(watchData, WatchDto.class);

        //các dữ liệu về band, brand, family, movement chưa được xử lý

//        Brand brand = Brand.builder().id(watchDto.getBrand().getId()).build();
//        Family family = Family.builder().id(watchDto.getFamily().getId()).build();
//        Band band = Band.builder().id(watchDto.getBand().getId()).build();

        var brand = Brand.builder().build();
        Family family = Family.builder().build();
        Movement movement = Movement.builder().build();
        if (watchDto.getBrand().isAddNew()) {
            brand.setBrandName(watchDto.getBrand().getBrandName());
            brandRepository.save(brand);
        } else {
            brand = brandRepository.findByBrandName(watchDto.getBrand().getBrandName());
        }
        System.out.println("familyname=" + watchDto.getFamily().getFamilyName());
        System.out.println("is f add new = " + watchDto.getFamily().isAddNew());
        System.out.println("cond=" + (watchDto.getFamily().equals("") && watchDto.getFamily() == null));
        if (!(watchDto.getFamily().equals("") && watchDto.getFamily() == null)) {
            if (watchDto.getFamily().isAddNew()) {
                family.setFamilyName(watchDto.getFamily().getFamilyName());
                family.setBrand(brand);
                familyRepository.save(family);
            } else {
                family = familyRepository.findByFamilyName(watchDto.getFamily().getFamilyName());
            }
        }
        if (watchDto.getMovement().isAddNew()) {
            movement.setName(watchDto.getMovement().getName());
            movement.setType(watchDto.getMovement().getType());
            movement.setPower(watchDto.getMovement().getPower());
            movement.setOrigin(watchDto.getMovement().getOrigin());
            movement.setFunctions(watchDto.getMovement().getFunctions());
            movement.setCaliber(watchDto.getMovement().getCaliber());
            movement.setPowerReserve(watchDto.getMovement().getPowerReserve());
            movement.setCalendar(watchDto.getMovement().getCalendar());
            movementRepository.save(movement);
        } else {
            movement = movementRepository.findByName(watchDto.getMovement().getName());
        }
        Case watchCase = Case.builder().material(watchDto.getWatchCase().getMaterial()).diameter(
                watchDto.getWatchCase().getDiameter()).thickness(watchDto.getWatchCase().getThickness()).shape(
                watchDto.getWatchCase().getShape()
        ).back(watchDto.getWatchCase().getBack()).waterResistance(watchDto.getWatchCase().getWaterResistance()).bezel(
                watchDto.getWatchCase().getBezel()).crystal(watchDto.getWatchCase().getCrystal()).build();
        caseRepository.save(watchCase);

        Band watchBand = Band.builder().material(
                        watchDto.getBand().getMaterial()
                ).type(watchDto.getBand().getType()).clasp(watchDto.getBand().getClasp())
                .color(watchDto.getBand().getColor()).width(watchDto.getBand().getWidth()).length(watchDto.getBand().getLength()).build();
        braceletRepository.save(watchBand);

        Watch watch = Watch.builder()
                .gender(watchDto.getGender())
                .createdDate(new Date())
                .active(true)
                .name(watchDto.getName())
                .origin(watchDto.getOrigin())
                .produced(watchDto.getProduced())
                .reference(watchDto.getReference())
                .weight(watchDto.getWeight())
                .warranty(watchDto.getWarranty())
                .limited(watchDto.isLimited())
                .description(watchDto.getDescription())
                .inventoryQuantity(watchDto.getInventoryQuantity())
                .soldQuantity(0L)
                .defaultPrices(watchDto.getDefaultPrices()/1000)
                .stars(0f)
                .brand(brand)
                .family(family)
                .band(watchBand)
                .watchCase(watchCase)
                .movement(movement)
                .totalReviews(0)
                .images(new ArrayList<>())
                .dials(new ArrayList<>())
                .watchStyle(watchDto.getWatchStyle())
                .build();


        List<Image> imageArrayList = new ArrayList<>();
        boolean ismain = false;
        for (MultipartFile image : images) {
            try {
                Image savedImage = storageService.uploadImageToFileSystem(image, !ismain);
                imageArrayList.add(savedImage);
                ismain = true;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        List<Dial> dials = new ArrayList<>();

        int i = 0;

        for (DialDto dialDto : watchDto.getDials()) {
            try {
                if (dialImages != null && dialImages.get(i) != null) {
                    Dial savedDial = storageService.uploadImageToFileSystem(dialImages.get(i), dialDto);
                    i++;
                    dials.add(savedDial);
                } else {
                    Dial savedDial = storageService.uploadImageToFileSystem(null, dialDto);
                    i++;
                    dials.add(savedDial);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

//        if(dialImages != null){
//            for(MultipartFile image: dialImages){
//                try {
//                    Dial savedDial = storageService.uploadImageToFileSystem(image, watchDto.getDials().get(i));
//                    i++;
//                    dials.add(savedDial);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }


        watch.setImages(imageArrayList);
        watch.setDials(dials);
        watchRepository.save(watch);
        return watchMapper.mapTo(watch);
    }

    @Override
    public List<WatchDto> getWatchesByPriceRange(double min, double max) {
        System.out.println("min = " + min + " max = " + max);
        //get all watches and filter by price range
        List<Watch> watches = watchRepository.findAll();
        List<WatchDto> watchDtos = watches.stream().filter(watch -> {
            double price = watch.getDefaultPrices();
            return price >= min && price <= max;
        }).map(watchMapper::mapTo).collect(Collectors.toList());
        return watchDtos;
    }

    @Override
    public Page<WatchDto> getWatchesByFilters(List<String> cate, List<String> color, String movement, String start,
                                              String end, String brand, List<String> cs, List<String> bt,
                                              String typeF, Pageable pageable) {
        System.out.println("cate = " + cate + " color = " + color + " movement = " + movement + " start = " + start + " end = " + end + " brand = " + brand + " cs = " + cs + " bt = " + bt);
//        List<Watch> watches = watchRepository.findAll();
        List<Watch> watches = watchRepository.findAllByActiveTrue();
        List<WatchDto> watchDtos = new ArrayList<>();
//        watches.forEach(watch -> {
//            //filter by category
//            boolean isAdded = false;
//            //filter by color
//            if (color != null && !color.isEmpty()) {
//                if (!color.contains(watch.getBand().getColor())) {
//                    watchDtos.add(watchMapper.mapTo(watch));
//                    isAdded = true;
//                }
//            }
//            //filter by movement
//            if (!isAdded && movement != null && !movement.isEmpty()) {
//                if (watch.getMovement().getName().equals(movement)) {
//                    watchDtos.add(watchMapper.mapTo(watch));  //this line is commented out
//                    isAdded = true;
//                }
//            }
//            //filter by start year
//            if (!isAdded && start != null && !start.isEmpty() && end != null && !end.isEmpty()) {
//                try {
//                    if (watch.getDefaultPrices() >= Float.parseFloat(start) && watch.getDefaultPrices() <= Float
//                    .parseFloat(end)) {
//                        watchDtos.add(watchMapper.mapTo(watch));
//                        isAdded = true;
//                    }
//                } catch (NumberFormatException e) {
////                   e.printStackTrace();
//                }
//            }
//            //filter by end yearx
//
//            //filter by brand
//            if (!isAdded && brand != null && !brand.isEmpty()) {
//                if (watch.getBrand().getBrandName().equals(brand)) {
//                    watchDtos.add(watchMapper.mapTo(watch));
//                    isAdded = true;
//                }
//            }
//            //filter by case size
//            if (!isAdded && cs != null && !cs.isEmpty()) {
//                if (cs.contains(watch.getWatchCase().getShape())) {
//                    watchDtos.add(watchMapper.mapTo(watch));
//                    isAdded = true;
//                }
//            }
//            //filter by band type
//            if (!isAdded && bt != null && !bt.isEmpty()) {
//                if (bt.contains(watch.getBand().getType())) {
//                    watchDtos.add(watchMapper.mapTo(watch));
//                    isAdded = true;
//                }
//            }
//        });
        int numberOfFilters = cate.size() - 1;
        AtomicInteger numberOfFiltersAdded = new AtomicInteger();
        List<WatchDto> ans = new ArrayList<>();
        if (typeF != null && !typeF.isEmpty() && typeF.equals("Any")) {
            watches.forEach(
                    watch -> {
                        boolean isAdded = false;
                        for (String category : cate) {
                            if (!isAdded && category.equals("price")) {
                                System.out.println("start = " + start + " end = " + end);
                                if (watch.getDefaultPrices() >= Double.parseDouble(start) * 1000 && watch.getDefaultPrices() <= Double.parseDouble(end) * 1000) {
                                    watchDtos.add(watchMapper.mapTo(watch));
                                    isAdded = true;
                                    numberOfFiltersAdded.getAndIncrement();
                                    break;
                                }
                            }
                            if (!isAdded && category.equals("color")) {
                                String bandColor = watch.getBand().getColor();
                                int indexOfDash = bandColor.indexOf("-");
                                String substring = indexOfDash != -1 ? bandColor.substring(0, indexOfDash) : bandColor;
                                if (watch.getBand().getColor().length() > 0 && color.contains(watch.getBand().getColor()) ||
                                        color.contains(substring)
                                ) {
                                    watchDtos.add(watchMapper.mapTo(watch));
//                                System.out.println("fetch color = "+watch.getBand().getColor()+" band="+watch
//                                .getBand().getColor().substring(0, watch.getBand().getColor().indexOf("-")));
                                    isAdded = true;
                                    numberOfFiltersAdded.getAndIncrement();
                                    break;

                                }
                            }
                            if (!isAdded && category.equals("bt")) {
                                if (bt.contains(watch.getBand().getType())) {
                                    watchDtos.add(watchMapper.mapTo(watch));
                                    isAdded = true;
                                    numberOfFiltersAdded.getAndIncrement();
                                    break;
                                }
                            }
                            if (!isAdded && category.equals("cs")) {
                                if (cs.contains(watch.getWatchCase().getShape())) {
                                    watchDtos.add(watchMapper.mapTo(watch));
                                    isAdded = true;
                                    numberOfFiltersAdded.getAndIncrement();
                                    break;

                                }
                            }
                            if (!isAdded && category.equals("mvt")) {
                                String[] movementNames = movement.split(",");
                                for (String mvt : movementNames) {
                                    String mvtName = "";
                                    if (mvt.equals("Automatic")) {
                                        mvtName = "AUTOMATIC";
                                    } else if (mvt.equals("Eco-Drive")) {
                                        mvtName = "ECO_DRIVE";
                                    } else if (mvt.equals("Quartz")) {
                                        mvtName = "QUARTZ";
                                    }
                                    if (mvtName.equals(watch.getMovement().getType().name())) {
                                        watchDtos.add(watchMapper.mapTo(watch));
                                        isAdded = true;

                                        break;

                                    }
                                }
                                if (isAdded) {
                                    numberOfFiltersAdded.getAndIncrement();
                                    break;
                                }
                            }
                            if (!isAdded && category.equals("brand")) {
                                if (watch.getBrand().getBrandName().equals(brand)) {
                                    watchDtos.add(watchMapper.mapTo(watch));
                                    isAdded = true;
                                    numberOfFiltersAdded.getAndIncrement();
                                    break;

                                }
                            }
                        }

                    }
            );
        } else if (typeF != null && !typeF.isEmpty() && typeF.equals("Full")) {
            watches.forEach(
                    watch -> {
                        List<Boolean> isAdded = new ArrayList<>();
                        for (String category : cate) {
                            if (category.equals("price")) {
                                System.out.println("start = " + start + " end = " + end);
                                if (watch.getDefaultPrices() >= Double.parseDouble(start) * 1000 && watch.getDefaultPrices() <= Double.parseDouble(end) * 1000) {
                                    isAdded.add(true);
                                } else {
                                    isAdded.add(false);
                                }

                            }
                            if (category.equals("color")) {
                                String bandColor = watch.getBand().getColor();
                                int indexOfDash = bandColor.indexOf("-");
                                String substring = indexOfDash != -1 ? bandColor.substring(0, indexOfDash) : bandColor;
                                if (watch.getBand().getColor().length() > 0 && color.contains(watch.getBand().getColor()) ||
                                        color.contains(substring)
                                ) {
                                    isAdded.add(true);
                                } else {
                                    isAdded.add(false);

                                }
                            }
                            if (category.equals("bt")) {
                                if (bt.contains(watch.getBand().getType())) {
                                    isAdded.add(true);
                                } else {
                                    isAdded.add(false);
                                }
                            }
                            if (category.equals("cs")) {
                                if (cs.contains(watch.getWatchCase().getShape())) {
                                    isAdded.add(true);

                                } else {
                                    isAdded.add(false);
                                }
                            }
                            if (category.equals("mvt")) {
                                String[] movementNames = movement.split(",");
                                for (String mvt : movementNames) {
                                    String mvtName = "";
                                    if (mvt.equals("Automatic")) {
                                        mvtName = "AUTOMATIC";
                                    } else if (mvt.equals("Eco-Drive")) {
                                        mvtName = "ECO_DRIVE";
                                    } else if (mvt.equals("Quartz")) {
                                        mvtName = "QUARTZ";
                                    }
                                    if (mvtName.equals(watch.getMovement().getType().name())) {
                                        isAdded.add(true);
                                    } else {
                                        isAdded.add(false);
                                    }
                                }

                            }
                            if (category.equals("brand")) {
                                if (watch.getBrand().getBrandName().equals(brand)) {
                                    isAdded.add(true);
                                } else {
                                    isAdded.add(false);

                                }
                            }
                        }
                        System.out.println("isAdded = " + isAdded);
                        if (!isAdded.contains(false) && isAdded.size() >= numberOfFilters) {
                            WatchDto watchDto = watchMapper.mapTo(watch);
                            List<Promotion_Details> promotionDetails = productPromotionRepository
                                    .findCurrentPromotionDetailsByProductIdForProduct(watchDto.getId());
                            System.out.println("id=" + watchDto.getId() + " prod=" + promotionDetails);

                            if (promotionDetails != null && !promotionDetails.isEmpty()) {
                                float promotion = 0f;
                                for (Promotion_Details p : promotionDetails) {
                                    if (p.getPromotion().getPriority() == 1 && p.getPromotion().isActive()) {
                                        promotion += p.getValue();
                                    }
                                }
                                watchDto.setDiscount(promotion);
                                watchDto.setEndDiscountDate(promotionDetails.get(0).getPromotion().getDateEnd());
                            } else {
                                watchDto.setDiscount(0f);
                            }
                            watchDtos.add(watchDto);
                        }
                    }
            );
        }

        System.out.println();
//        int pageSize = pageable.getPageSize();
//        int currentPage = pageable.getPageNumber();
//        int startItem = currentPage * pageSize;
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<WatchDto> pageList;

        if (watchDtos.size() < startItem) {
            pageList = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, watchDtos.size());
            pageList = watchDtos.subList(startItem, toIndex);
        }

        return new PageImpl<>(pageList, pageable, watchDtos.size());
    }

    @Override
    public List<WatchNoReview> fetchGenderWatches(String gender, Pageable page) {
        List<Watch> ws = watchRepository.findAllWatchByGender(gender, page);
//        List<WatchDto> ws = watchRepository.findAllWatchByGender(gender, page).stream().map(
//                watch -> {
//
//                    return WatchDto.builder().totalReviews(watch.getTotalReviews()).id(watch.getId()).name(watch
//                    .getName()).reference(watch.getReference())
//                            .defaultPrices(watch.getDefaultPrices()).stars(watch.getStars())
//                            .brand(BrandDto.builder().brandName(watch.getBrand().getBrandName()).build())
//                            .family(FamilyDto.builder().familyName(watch.getFamily().getFamilyName()).build()).band
//                            (BandDto.builder().build()).build();
//                }
//
//        ).toList();
//        List<WatchDto> watchDtos = ws.stream().map(watchMapper::mapTo).collect(Collectors.toList());
        List<WatchNoReview> watchDtos = ws.stream().map(
                watch -> {
                    return WatchNoReview.builder().id(watch.getId()).name(watch.getName()).reference(watch.getReference())
                            .defaultPrices(watch.getDefaultPrices()).stars(watch.getStars())
                            .brand(BrandDto.builder().brandName(watch.getBrand().getBrandName()).build())
                            .images(
                                    watch.getImages().stream().map(image -> ImageDto.builder().image(image.getImage()).name(image.getName()).isMain(image.getIsMain()).build()).collect(Collectors.toList())
                            )
                            .movement(MovementDto.builder().name(watch.getMovement().getName())
                                    .origin(watch.getMovement().getOrigin())
                                    .type(watch.getMovement().getType()).build())
                            .family(FamilyDto.builder().familyName(watch.getFamily().getFamilyName()).build()).band(BandDto.builder().build()).build();
                }
        ).collect(Collectors.toList());
        return watchDtos;
    }

    @Override
    public List<CollectionDto> fetchCollections() {
        List<Brand> brands = brandRepository.findAll();
        List<CollectionDto> collectionDtos = new ArrayList<>();
        for (Brand brand : brands) {
            if (!(brand.getBrandName().equals("") || brand.getBrandName() == null)) {
                List<FamilyDto> familyDtos = new ArrayList<>();
                List<Family> f = familyRepository.findByBrandName(brand.getBrandName());
                f.forEach(family -> {
                    if (!family.getFamilyName().equals("") && family.getFamilyName() != null) {
                        FamilyDto familyDto =
                                FamilyDto.builder().id(family.getId()).familyName(family.getFamilyName()).build();
                        familyDtos.add(familyDto);
                    }
                });
                CollectionDto collectionDto = CollectionDto.builder()
                        .brand(BrandDto.builder().id(brand.getId()).brandName(brand.getBrandName()).build())
                        .family(familyDtos)
                        .build();
                collectionDtos.add(collectionDto);
            }


        }
        return collectionDtos;
    }

    @Override
    public List<WatchNoReview> fetchWatchByBrand(Long brandId, Pageable page) {
        List<Watch> ws = watchRepository.findAllWatchByBrandId(brandId, page);
        List<WatchNoReview> watchDtos = ws.stream().map(
                watch -> {
                    return WatchNoReview.builder().id(watch.getId()).name(watch.getName()).reference(watch.getReference())
                            .defaultPrices(watch.getDefaultPrices()).stars(watch.getStars())
                            .brand(BrandDto.builder().brandName(watch.getBrand().getBrandName()).build())
                            .images(
                                    watch.getImages().stream().map(image -> ImageDto.builder().image(image.getImage()).name(image.getName()).isMain(image.getIsMain()).build()).collect(Collectors.toList())
                            )
                            .movement(MovementDto.builder().name(watch.getMovement().getName())
                                    .origin(watch.getMovement().getOrigin())
                                    .type(watch.getMovement().getType()).build())
                            .family(FamilyDto.builder().familyName(watch.getFamily().getFamilyName()).build()).band(BandDto.builder().build()).build();
                }
        ).collect(Collectors.toList());
        return watchDtos;
    }

    @Override
    public List<WatchNoReview> fetchWatchByFamily(Long fid, Pageable page) {
        List<Watch> ws = watchRepository.findAllWatchByFamilyId(fid, page);
        List<WatchNoReview> watchDtos = ws.stream().map(
                watch -> {
                    return WatchNoReview.builder().id(watch.getId()).name(watch.getName()).reference(watch.getReference())
                            .defaultPrices(watch.getDefaultPrices()).stars(watch.getStars())
                            .brand(BrandDto.builder().brandName(watch.getBrand().getBrandName()).build())
                            .images(
                                    watch.getImages().stream().map(image -> ImageDto.builder().image(image.getImage()).name(image.getName()).isMain(image.getIsMain()).build()).collect(Collectors.toList())
                            )
                            .movement(MovementDto.builder().name(watch.getMovement().getName())
                                    .origin(watch.getMovement().getOrigin())
                                    .type(watch.getMovement().getType()).build())
                            .family(FamilyDto.builder().familyName(watch.getFamily().getFamilyName()).build()).band(BandDto.builder().build()).build();
                }
        ).collect(Collectors.toList());
        return watchDtos;
    }
    private final ProductPromotionRepository productPromotionRepository;
    @Override
    public List<WatchDto> getPopularWatches(String time) {
        if(time.equals("week")){
            //get all orders in the last week
            //get all watches in the orders
            //count the number of watches
            //sort the watches by the number of watches
            //return the top 10 watches
            long currentTimeMillis = System.currentTimeMillis();
            long sevenDaysAgoMillis = currentTimeMillis - 7L * 24L * 60L * 60L * 1000L;
            System.out.println("sevenDaysAgoMillis days ago in milliseconds: " + sevenDaysAgoMillis);

            Date seven = new Date(sevenDaysAgoMillis);
            List<Order> orders = orderRepository.findAllByOrderDateAfter(seven);
            List<OrderDetails> orderDetails = new ArrayList<>();
//            orders.forEach(order -> {
//                orderDetails.addAll(order.getOrderDetails());
//            });
//
//            Map<Long, Long> watchCount = new HashMap<>();
//            orderDetails.forEach(orderDetail -> {
//                Long watchId = orderDetail.getWatch().getId();
//                if(watchCount.containsKey(watchId)){
//                    watchCount.put(watchId, watchCount.get(watchId) + orderDetail.getQuantity());
//                }else{
//                    watchCount.put(watchId, orderDetail.getQuantity());
//                }
//            });
            Map<Long, Long> watchCount = new HashMap<>();
            orders.forEach(order -> {
                order.getOrderDetails().forEach(orderDetail -> {
                    Long watchId = orderDetail.getWatch().getId();
                    watchCount.put(watchId, watchCount.getOrDefault(watchId, 0L) + orderDetail.getQuantity());
                });
            });


            List<Map.Entry<Long, Long>> sorted = watchCount.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).collect(Collectors.toList());
            List<WatchDto> watches = new ArrayList<>();
            int size = sorted.size();
            for(int i = 0; i < 9 && i < size; i++){
                Watch watch = watchRepository.findById(sorted.get(i).getKey()).orElseThrow(() -> new NotFoundException("Watch not found!"));
                //get discount now on product
                WatchDto watchDto = watchMapper.mapTo(watch);

                List<Promotion_Details> promotionDetails = productPromotionRepository
                        .findCurrentPromotionDetailsByProductIdForProduct(watchDto.getId());
                System.out.println("id=" + watchDto.getId() + " prod=" + promotionDetails);

                if (promotionDetails != null && !promotionDetails.isEmpty()) {
                    float promotion = 0f;
                    for (Promotion_Details p : promotionDetails) {
                        if (p.getPromotion().getPriority() == 1 && p.getPromotion().isActive()) {
                            promotion += p.getValue();
                        }
                    }
                    watchDto.setDiscount(promotion);
                    watchDto.setEndDiscountDate(promotionDetails.get(0).getPromotion().getDateEnd());
                } else {
                    watchDto.setDiscount(0f);
                }

                watches.add(watchDto);


            }

            return watches;

        }else{
            //get all orders in the last month
            //get all watches in the orders
            //count the number of watches
            //sort the watches by the number of watches
            //return the top 10 watches
            long currentTimeMillis = System.currentTimeMillis();
            long thirtyDaysAgoMillis = currentTimeMillis - 30L * 24L * 60L * 60L * 1000L;
            System.out.println("Thirty days ago in milliseconds: " + thirtyDaysAgoMillis);

            Date thirtyDaysAgoDate = new Date(thirtyDaysAgoMillis);
            List<Order> orders =
                    orderRepository.findAllByOrderDateAfter(thirtyDaysAgoDate);
            System.out.println("date = "+thirtyDaysAgoDate);
            List<OrderDetails> orderDetails = new ArrayList<>();
//            orders.forEach(order -> {
//                System.out.println("orderId="+order.getId());
//
//                orderDetails.addAll(order.getOrderDetails());
//            });
//
//            Map<Long, Long> watchCount = new HashMap<>();
//            orderDetails.forEach(orderDetail -> {
//                Long watchId = orderDetail.getWatch().getId();
//                if(watchCount.containsKey(watchId)){
//                    watchCount.put(watchId, watchCount.get(watchId) + orderDetail.getQuantity());
//                }else{
//                    watchCount.put(watchId, orderDetail.getQuantity());
//                }
//            });

            Map<Long, Long> watchCount = new HashMap<>();
            orders.forEach(order -> {
                order.getOrderDetails().forEach(orderDetail -> {
                    Long watchId = orderDetail.getWatch().getId();
                    watchCount.put(watchId, watchCount.getOrDefault(watchId, 0L) + orderDetail.getQuantity());
                });
            });


            List<Map.Entry<Long, Long>> sorted = watchCount.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).collect(Collectors.toList());
            List<WatchDto> watches = new ArrayList<>();
            for(int i = 0; i < 9; i++){
                Watch watch = watchRepository.findById(sorted.get(i).getKey()).orElseThrow(() -> new NotFoundException("Watch not found!"));
                WatchDto watchDto = watchMapper.mapTo(watch);
                List<Promotion_Details> promotionDetails = productPromotionRepository
                        .findCurrentPromotionDetailsByProductIdForProduct(watchDto.getId());
                System.out.println("id=" + watchDto.getId() + " prod=" + promotionDetails);

                if (promotionDetails != null && !promotionDetails.isEmpty()) {
                    float promotion = 0f;
                    for (Promotion_Details p : promotionDetails) {
                        if (p.getPromotion().getPriority() == 1 && p.getPromotion().isActive()) {
                            promotion += p.getValue();
                        }
                    }
                    watchDto.setDiscount(promotion);
                    watchDto.setEndDiscountDate(promotionDetails.get(0).getPromotion().getDateEnd());
                } else {
                    watchDto.setDiscount(0f);
                }
                watches.add(watchDto);
            }
            return watches;
        }
    }


}
