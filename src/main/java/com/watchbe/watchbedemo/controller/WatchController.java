package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.dto.*;
import com.watchbe.watchbedemo.exception.NotFoundException;
import com.watchbe.watchbedemo.mapper.WatchMapperImpl;
import com.watchbe.watchbedemo.model.Customer;
import com.watchbe.watchbedemo.model.Image;
import com.watchbe.watchbedemo.model.Promotion_Details;
import com.watchbe.watchbedemo.model.Watch;
import com.watchbe.watchbedemo.model.account.Account;
import com.watchbe.watchbedemo.repository.*;
import com.watchbe.watchbedemo.service.BrandService;
import com.watchbe.watchbedemo.service.EmailService;
import com.watchbe.watchbedemo.service.WatchService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class WatchController {
    /*services*/
    private final WatchService watchService;
    private final ReviewRepository reviewRepository;
    private final BrandService brandService;
    private final PromotionRepository promotionRepository;
    private final  WatchRepository watchRepository;
    private final WatchMapperImpl watchMapper;
    private final ImageRepository imageRepository;
//    @GetMapping
//    public ResponseEntity<List<WatchDto>> getAllWatch(){
//        return ResponseEntity.ok(watchService.getAll());
//    }
    @PostMapping("/product-by-list-id")
    public ResponseEntity<List<WatchDto>> getAsList(@RequestBody List<Long> ids){
        System.out.println("OKAY");
        List<Watch> watch = watchRepository.findAllById(ids);
        List<WatchDto> watchDtos = watch.stream().map(watchMapper::mapTo).collect(Collectors.toList());
        watchDtos.forEach(
                watchDto -> {
                    List<Promotion_Details> promotionDetails =
                            productPromotionRepository
                                    .findPromotionDiscountPercentageByProductIdAndOrderDate(watchDto.getId(),new Date());
                    float promotion = 0f;
                    for(Promotion_Details p: promotionDetails){
                        if(p.getPromotion().getPriority()==1){
                            promotion += p.getValue();
                        }
                    }
                    if(promotionDetails!=null){
                        watchDto.setDiscount(promotion);
                    }
                    else{
                        watchDto.setDiscount(0f);
                    }
                }
        );
        return ResponseEntity.ok(watchDtos);
    }
    @GetMapping("/page")
    public ResponseEntity<Page<WatchDto>> getAllWatchByPage(Pageable page,
                                                            @RequestParam("type") String type){
        Page<WatchDto> watchDtos;
        System.out.println("Type: "+type);
        if(type.equals("all")){
            System.out.println("tim tat ca sp");
            watchDtos = watchRepository.findAll(page).map(watchMapper::mapTo);
        } else if (type.equals("archived")) {
            watchDtos = watchRepository.findAllByActiveFalse(page).map(watchMapper::mapTo);
        } else watchDtos = watchService.getAll(page);
//        List<WatchDto> doubleList =new ArrayList<>();
//        for(int i=0; i<10; i++){
//            watchDtos.forEach(watchDto -> doubleList.add(watchDto));
//        }
//        return ResponseEntity.ok(doubleList);
        for (WatchDto watchDto : watchDtos) {
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
        }

        return ResponseEntity.ok(watchDtos);
    }
    
    @GetMapping("/details/{reference}")
    public ResponseEntity<WatchDto> getWatchDetailsByReference(@PathVariable String reference){
        System.out.println(reference);
        //try to add new reply to review
//        Review r = reviewRepository.findById(1L).orElseThrow();
//                    Review reply1 = Review.builder()
//                    .comment("reply first comment")
//                    .build();
//            r.addChildReview(reply1);
        WatchDto watchDto = watchService.findWatchByReference(reference);
        List<Promotion_Details> promotionDetails = productPromotionRepository
                .findCurrentPromotionDetailsByProductIdForProduct(watchDto.getId());
        System.out.println("id=" + watchDto.getId() + " prod=" + promotionDetails);

        if (promotionDetails != null && promotionDetails.size() > 0){
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

        return new ResponseEntity<>(watchDto, HttpStatus.OK);



    }

    @GetMapping("/change-main-image/{ref}")
    public ResponseEntity<WatchDto> changeMainImage(@PathVariable String ref,
                                                    @RequestParam("imageName") String imgName
                                                    ){
        Watch watch =
                watchRepository.findWatchByReference(ref)
                        .orElseThrow(() -> new NotFoundException(String.format("Watch with reference %s not found!",
                                ref)));
        List<Image> images = watch.getImages();
        for (Image image : images) {
            if(image.getName().equals(imgName)){
                image.setIsMain(true);
            }
            else{
                image.setIsMain(false);
            }
        }
        imageRepository.saveAll(images);

        watch.getReviews().sort((r1, r2) -> r2.getDatePosted().compareTo(r1.getDatePosted()));
        WatchDto watchDto = watchMapper.mapTo(watch);
        List<Promotion_Details> promotionDetails = productPromotionRepository
                .findCurrentPromotionDetailsByProductIdForProduct(watchDto.getId());
        System.out.println("id=" + watchDto.getId() + " prod=" + promotionDetails);

        if (promotionDetails != null && promotionDetails.size() > 0){
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
        return ResponseEntity.ok(watchDto);
    }

    //ham nay da bi loi -> chay qua lau
    @GetMapping("/similar/{watchName}")
    public ResponseEntity<List<WatchDto>> getSimilarWatches(@PathVariable String watchName){
        return ResponseEntity.ok(watchService.getSimilarWatches(watchName));
    }

    @GetMapping("/filters")
    public Page<WatchDto> getWatchesByFilters(@RequestParam(required = false) List<String> cate,
                                                    @RequestParam(required = false) List<String> color,
                                                    @RequestParam(required = false) String movement,
                                                    @RequestParam(required = false) String start,
                                                    @RequestParam(required = false) String end,
                                                    @RequestParam(required = false) String brand,
                                                    @RequestParam(required = false) List<String> cs,
                                                    @RequestParam(required = false) List<String> bt,
                                                    @RequestParam(required =false) String typeF,
                                                    Pageable page,
                                                    HttpServletResponse response
                                                              ){
        System.out.println("TypeF: "+typeF);
        System.out.println(cate+" "+color+" "+movement+" "+start+" "+end+" "+brand+" "+cs+" "+bt);
        return watchService.getWatchesByFilters(cate, color, movement, start, end, brand, cs, bt,
                typeF,page);




    }

    private int getTotalPages(List<?> list, int page, int size) {
        int totalItems = list.size();
        return (int) Math.ceil((double) totalItems / size);
    }

    @GetMapping("/price-filter/{min}/{max}")
    public ResponseEntity<List<WatchDto>> getWatchesByPriceRange(@PathVariable double min, @PathVariable double max){
        System.out.println(min+" "+max);
        return ResponseEntity.ok(watchService.getWatchesByPriceRange(min, max));
    }

    @GetMapping("/search-similar/brands/{brandName}")
    public ResponseEntity<List<BrandDto>> getSimilarWatchesByBrand(@PathVariable String brandName){
        return ResponseEntity.ok(brandService.getSimilarBrand(brandName));
    }

    private final ProductPromotionRepository productPromotionRepository;
    @GetMapping("/promotions/{productId}")
    public List<Float> getCurrentPromotionDetails(@PathVariable Long productId) {
        return productPromotionRepository.findCurrentPromotionDetailsByProductId(productId);
    }

    @GetMapping("/promotions-by-orderdate/{productId}")
    public List<Promotion_Details> getPromotionDiscountPercentageByProductIdAndOrderDate(@PathVariable Long productId) {
        Date date = new Date();
//        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return productPromotionRepository.findPromotionDiscountPercentageByProductIdAndOrderDate(productId, date);
    }


    @GetMapping("/fetch-watches/{gender}")
    public ResponseEntity<List<WatchNoReview>> fetchMenWatches(@PathVariable String gender,Pageable page){
        return ResponseEntity.ok(watchService.fetchGenderWatches(gender,page));
    }

    //fetch brand, collections data
    @GetMapping("/fetch-collections")
    public ResponseEntity<List<CollectionDto>> fetchCollections(){
        return ResponseEntity.ok(watchService.fetchCollections());
    }

    @GetMapping("/fetch-watches/brand/{brandId}")
    public ResponseEntity<List<WatchNoReview>> fetchCollections(@PathVariable Long brandId, Pageable page){
        return ResponseEntity.ok(watchService.fetchWatchByBrand(brandId,page));
    }

    @GetMapping("/fetch-watches/family/{fid}")
    public ResponseEntity<List<WatchNoReview>> fetchCollectionsFamily(@PathVariable Long fid, Pageable page){
        return ResponseEntity.ok(watchService.fetchWatchByFamily(fid,page));
    }

    List<String> getPromotionNames(List<Promotion_Details> promotions_details){
        List<String> promotionNames = new ArrayList<>();
        for (Promotion_Details p  : promotions_details) {
            promotionNames.add(p.getPromotion().getName());
        }
        return promotionNames;
    }
    //get all watch and promotion name using for watch
    @GetMapping("/get-all/watch")
    public ResponseEntity<List<WatchDto>> getAllWatches(){
        List<Watch> watches = watchRepository.findAllByActiveTrue();
        List<WatchDto> watchDtos= watches.stream().map(
                watch -> WatchDto.builder().id(watch.getId()).name(watch.getName()).images(
                        watch.getImages().stream().map(
                                image -> ImageDto.builder().isMain(image.getIsMain()).name(image.getName()).build()
                        ).collect(Collectors.toList())
                ).promotion(getPromotionNames(productPromotionRepository.findCurrentPromotionDetailsByProductIdForProduct(watch.getId())))
                        .defaultPrices(watch.getDefaultPrices()).build()
        ).collect(Collectors.toList());



        return ResponseEntity.ok(watchDtos);
    }

    //find popular watches
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularWatches(@RequestParam("time") String time) {
        List<WatchDto> watches = watchService.getPopularWatches(time);
        return ResponseEntity.ok(watches);
    }

    @GetMapping("/get-four-new-watches")
    public ResponseEntity<List<WatchDto>> getFourNewWatches(@RequestParam("gender") String gender ){
        List<Watch> watches = watchRepository.findTop4ByActiveTrueAndGenderOrderByCreatedDateDesc(gender);
        List<WatchDto> watchDtos = watches.stream().map(watchMapper::mapTo).collect(Collectors.toList());
        return ResponseEntity.ok(watchDtos);
    }



    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";

    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Password length must be at least 4 characters");
        }

        StringBuilder password = new StringBuilder(length);

        // Ensure at least one character from each category
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));

        // Fill the rest of the password with random characters
        for (int i = 4; i < length; i++) {
            password.append(PASSWORD_ALLOW_BASE.charAt(random.nextInt(PASSWORD_ALLOW_BASE.length())));
        }

        // Shuffle the characters in the password
        char[] passwordChars = password.toString().toCharArray();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(length);
            char temp = passwordChars[i];
            passwordChars[i] = passwordChars[randomIndex];
            passwordChars[randomIndex] = temp;
        }

        return new String(passwordChars);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) throws MessagingException {
        Account user = accountRepository.findByEmail(email).orElseThrow();
        if (user == null) {
            throw new NotFoundException("User with provided email not found");
        }

        // Generate a random password
        String newPassword = generateRandomPassword(8);

        // Set the new password for the user
        user.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(user);
        // Send the new password to the user's email
//        emailService.sendNewPasswordEmail(email, newPassword);
        Customer customer = customerRepository.findCustomerByEmail(email).orElseThrow();
        emailService.sendEmail("tongcongminh2021@gmail.com", "New password", sendRefuseEmail(newPassword,
                customer.getFirstName()+" "+customer.getLastName()));
        return ResponseEntity.ok().build();
    }

    public String sendRefuseEmail( String newPassword, String customerName) {
        return
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "    <meta charset='UTF-8'>" +
                        "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "    <title>Mật khẩu mới</title>" +
                        "    <style>" +
                        "        body {" +
                        "            font-family: 'Arial', sans-serif;" +
                        "            margin: 0;" +
                        "            padding: 0;" +
                        "            background-color: #f4f4f4;" +
                        "        }" +
                        "        .container {" +
                        "            max-width: 600px;" +
                        "            margin: 20px auto;" +
                        "            padding: 20px;" +
                        "            background-color: #f6f6f6;" +
                        "            border-radius: 10px;" +
                        "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);" +
                        "        }" +
                        "        h1 {" +
                        "            color: #333333;" +
                        "            margin-bottom: 20px;" +
                        "        }" +
                        "        p {" +
                        "            color: #666666;" +
                        "            margin-bottom: 10px;" +
                        "        }" +
                        "        .reason {" +
                        "            color: #ff0000;" +
                        "            font-weight: bold;" +
                        "        }" +
                        "        .apology {" +
                        "            color: #333333;" +
                        "            font-style: italic;" +
                        "            margin-top: 20px;" +
                        "        }" +
                        "        .footer {" +
                        "            margin-top: 20px;" +
                        "            font-size: 12px;" +
                        "            color: #999999;" +
                        "        }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <h1 style='text-align: center;'>[TIMEFLOW] Your password</h1>" +
                        "        <p class='apology'>Mật khẩu của bạn đã được khôi phục </p>" +
                        "        <p>Chào "+customerName+" đây là mật khẩu mới của bạn:</p>" +
                        "        <p class='reason'>" + newPassword + "</p>" +
                        "        <p>Please contact us for further information.</p>" +
                        "        <p class='footer'>This email was sent from [TIMEFLOW]. Please do not reply to this " +
                        "email.</p>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";
    }
}

