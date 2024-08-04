package com.watchbe.watchbedemo;

import com.watchbe.watchbedemo.auth.AuthenticationService;
import com.watchbe.watchbedemo.auth.RegisterRequest;
import com.watchbe.watchbedemo.model.account.Role;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.model.provinces.Province;
import com.watchbe.watchbedemo.repository.*;
import com.watchbe.watchbedemo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
@RequiredArgsConstructor
public class DemoWatchApplication {
    private final EmailService emailService;
    private final AuthenticationService authenticationService;
    private final ProvinceRepository provinceRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    //start stripe listen event
    //stripe listen --forward-to localhost:8080/api/v1/webhook/stripe

    public static void main(String[] args) {
        SpringApplication.run(DemoWatchApplication.class, args);
//        ApplicationContext context = SpringApplication.run(DemoWatchApplication.class, args);
//
//        DataSource dataSource = context.getBean(DataSource.class);
//        executeSqlScript(dataSource, "script/ImportData_vn_units.sql");

    }
//    public static Date generateRandomDate() {
//        // Tạo ngày bắt đầu (20/4)
//        Calendar startCal = Calendar.getInstance();
//        startCal.set(Calendar.MONTH, Calendar.APRIL);
//        startCal.set(Calendar.DAY_OF_MONTH, 20);
//
//        // Tạo ngày kết thúc (9/5)
//        Calendar endCal = Calendar.getInstance();
//        endCal.set(Calendar.MONTH, Calendar.MAY);
//        endCal.set(Calendar.DAY_OF_MONTH, 9);
//
//        // Chuyển đổi sang milliseconds
//        long startMillis = startCal.getTimeInMillis();
//        long endMillis = endCal.getTimeInMillis();
//
//        // Tạo ngày ngẫu nhiên trong khoảng thời gian
//        long randomMillisSinceEpoch = ThreadLocalRandom
//                .current()
//                .nextLong(startMillis, endMillis);
//
//        // Chuyển đổi ngày ngẫu nhiên thành kiểu Date
//        return new Date(randomMillisSinceEpoch);
//    }
//    @EventListener(ApplicationReadyEvent.class)
//    public void init(){
//        List<Order> orders = new ArrayList<>();
//        orders = orderRepository.findAll();
//        for(Order o : orders){
//            o.setOrderDate(generateRandomDate());
//            orderRepository.save(o);
//        }
//    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void init() throws  Exception{
//        //random shipping address
////        for(int j = 0; j<491; j++){
////            Random rand = new Random();
////            String phoneNumber = "+84";
////            for (int i = 0; i < 9; i++) {
////                phoneNumber += rand.nextInt(10);
////            }
////
////            //random city
////            List<Province> provinces = new ArrayList<>();
////            provinces = provinceRepository.findAll();
////            Random randCity = new Random();
////            Province city = provinces.get(randCity.nextInt(provinces.size()));
////            ShippingAddress shippingAddress = ShippingAddress.builder()
////                    .city(city.getName()).build();
////            shippingAddressRepository.save(shippingAddress);
////
////        }
//        //set shipping address for order
//        List<Order> orders = new ArrayList<>();
//        orders = orderRepository.findAll();
//        List<ShippingAddress> shippingAddresses = new ArrayList<>();
//        shippingAddresses = shippingAddressRepository.findAll();
//        List<ShippingAddress> finalShippingAddresses = shippingAddresses;
//        orders.forEach(
//                order -> {
//                    if(order.getAddress() != null){
//                        return;
//                    }
//                    Random rand = new Random();
//                    ShippingAddress shippingAddress = finalShippingAddresses.get(rand.nextInt(finalShippingAddresses.size()));
//                    while (shippingAddress.getCustomer()!=null){
//                        shippingAddress = finalShippingAddresses.get(rand.nextInt(finalShippingAddresses.size()));
//                    }
//                    order.setAddress(shippingAddress);
//                    orderRepository.save(order);
//                }
//        );
//
//    }
//    public void init() throws Exception {
//        emailService.sendEmail("tongcongminh2021@gmail.com",
//                "[TIMEFLOW] Your order has been refused",
//                sendRefuseEmail("Hết hàng", 12313L));
//    }
    public String sendRefuseEmail( String reason, Long orderId) {
        return
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "    <meta charset='UTF-8'>" +
                        "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "    <title>Your Order Refused</title>" +
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
                        "        <h1 style='text-align: center;'>[TIMEFLOW] Your order has been refused</h1>" +
                        "        <p class='apology'>We apologize for any inconvenience caused.</p>" +
                        "        <p>Your order has been refused for the following reason:</p>" +
                        "        <p class='reason'>" + reason + "</p>" +
                        "        <p>Order ID: " + orderId + "</p>" +
                        "        <p>Please contact us for further information.</p>" +
                        "        <p class='footer'>This email was sent from [TIMEFLOW]. Please do not reply to this " +
                        "email.</p>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";
    }
//    @EventListener(ApplicationReadyEvent.class)
//    public void init() throws Exception {
//            var admin = RegisterRequest.builder()
//
//                    .firstName("admin")
//                    .lastName("admin")
//                    .email("admin@admin.com")
//                    .password("password")
//                    .gender("NONE")
//                    .phoneNumber("123456")
//                    .role(Role.ADMIN).build();
//            System.out.println("Admin token: " + authenticationService.register(admin).getAccess_token());
//    }
private static final String[] FIRST_NAMES = {"John", "Emma", "Michael", "Sophia", "Daniel", "Olivia", "James", "Ava", "William", "Isabella", "Kuki"};
    private static final String[] LAST_NAMES = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Kakiji", "Ijika"};
    private static final String[] EMAIL_DOMAINS = {"gmail.com", "yahoo.com", "hotmail.com", "outlook.com"};
    private static final String[] GENDERS = {"Male", "Female"};
    private static String randomString(String[] array) {
        Random rand = new Random();
        return array[rand.nextInt(array.length)];
    }
    private static String randomPhoneNumber() {
        Random rand = new Random();
        StringBuilder phoneNumber = new StringBuilder("+");
        for (int i = 0; i < 10; i++) {
            phoneNumber.append(rand.nextInt(10));
        }
        return phoneNumber.toString();
    }
    private final WatchRepository watchRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
//    public static int generateWeightedRandom(Random random) {
//        int randomNumber = random.nextInt(100) + 1;
//        if (randomNumber <= 10) { // 10% chance
//            return 1;
//        } else if (randomNumber <= 20) { // 20% chance
//            return 2;
//        } else if (randomNumber <= 30) { // 20% chance
//            return 3;
//        } else if (randomNumber <= 60) { // 30% chance
//            return 4;
//        } else { // 40% chance
//            return 5;
//        }
//    }
        public static int generateWeightedRandom(Random random) {
            int randomNumber = random.nextInt(100) + 1;
            if (randomNumber <= 5) { // 10% chance
                return 1;
            } else if (randomNumber <= 10) { // 20% chance
                return 2;
            } else if (randomNumber <= 40) { // 20% chance
                return 3;
            } else if (randomNumber <= 80) { // 30% chance
                return 4;
            } else { // 20% chance
                return 5;
            }
        }

//    public static int generateWeightedRandom(Random random) {
//        int randomNumber = random.nextInt(100) + 1;
//        if (randomNumber <= 40) { // 40% chance
//            return 1;
//        } else if (randomNumber <= 70) { // 30% chance
//            return 2;
//        } else if (randomNumber <= 80) { // 10% chance
//            return 3;
//        } else if (randomNumber <= 90) { // 10% chance
//            return 4;
//        } else { // 10% chance
//            return 5;
//        }
//    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void initUser() throws Exception {
//        //all users will have the same password: password: "1"
////        for(int i=0; i<42; i++){
////            String firstName = randomString(FIRST_NAMES);
////            String lastName = randomString(LAST_NAMES);
////            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + randomString(EMAIL_DOMAINS);
////            String password = "1"; // Bạn có thể tạo một mật khẩu ngẫu nhiên ở đây nếu cần
////            String gender = randomString(GENDERS);
////            String phoneNumber = randomPhoneNumber();
////            var user = RegisterRequest.builder()
////                    .firstName(firstName)
////                    .lastName(lastName)
////                    .email(email)
////                    .password(password)
////                    .gender(gender)
////                    .phoneNumber(phoneNumber)
////                    .role(Role.USER)
////                    .build();
////            authenticationService.register("none",null,user,null);
////        }
//
//        Map<Long, Integer> map = new HashMap<>();
//        map.put(1L,0);
//        map.put(2L,0);
//        map.put(3L,0);
//        map.put(4L,0);
//        map.put(5L,0);
//        for(int j = 0; j<340; j++){
//            Random randb = new Random();
//            Customer customer = customerRepository.findById((long)j).orElseThrow();
//            int randbought = randb.nextInt(10);
//            int oldrandw = -1;
//            for(int k=0; k<randbought; k++){
//                Random rand = new Random();
//
//                //random rating
//                int randwatch = rand.nextInt(61);
//                if(randwatch == oldrandw){
//                    randwatch = rand.nextInt(61);
//                    if(randwatch == oldrandw){
//                        if(randwatch == 60){
//                            randwatch = 0;
//                        }
//                        randwatch +=1;
//
//                    }
//                }
//                oldrandw = randwatch;
//                Watch w = watchRepository.findById((long)randwatch).get();
//                System.out.println("watch="+w.getDefaultPrices());
//                Order order =
//                        Order.builder().orderDate(new Date()).orderStatus(OrderStatus.builder().id(6L).build()).amount(0).tax(0).shipping(0f).customer(customer).build();
//                orderRepository.save(order);
//                OrderDetails orderDetails = OrderDetails.builder().watch(w).price(w.getDefaultPrices()).quantity(1).order(order).build();
//                orderDetailsRepository.save(orderDetails);
//
//
//                Random randrv = new Random();
//                int randomValue = generateWeightedRandom(randrv);
//                String comment = "";
//                if(randomValue == 1){
//                    comment = "this watch is very bad";
//                }
//                if(randomValue == 2){
//                    comment = "this watch is bad";
//                }
//                if(randomValue == 3){
//                    comment = "this watch is ok";
//                }
//                if(randomValue == 4){
//                    comment = "this watch is good";
//                }
//                if(randomValue == 5){
//                    comment = "this watch is very good";
//                }
//                Review r =
//                        Review.builder().comment(comment).ratingStars(randomValue).loves(0).watch(w).customer(customer).datePosted(new Date()).build();
//
//                reviewRepository.save(r);
//                map.put((long)randomValue, map.getOrDefault((long)randomValue, 0) + 1);
//            }
//        }
//        for(Map.Entry<Long, Integer> entry : map.entrySet()) {
//            System.out.println("key=" + entry.getKey() + " value=" + entry.getValue());
//        }
//
//    }
    private static void executeSqlScript(DataSource dataSource, String scriptPath) {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(scriptPath));
//            ScriptUtils.executeSqlScript(connection, new ClassPathResource("script/import_orderdata.sql"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(
//            AuthenticationService authenticationService,
//            BrandRepository brandRepository,
//            WatchRepository watchRepository,
//            FamilyRepository familyRepository,
//            BraceletRepository braceletRepository,
//            DialRepository dialRepository,
//            CaseRepository caseRepository,
//            MovementRepository movementRepository,
//            CustomerRepository customerRepository,
//            ImageRepository imageRepository,
//            AccountRepository accountRepository,
//            ReviewRepository reviewRepository,
//            CartRepository cartRepository
//    ) {
//        return args -> {
//
//            var admin = RegisterRequest.builder()
//                    .firstName("admin")
//                    .lastName("admin")
//                    .email("admin@gmail.com")
//                    .password("password")
//                    .gender("1")
//                    .phoneNumber("123456")
//                    .role(Role.ADMIN).build();
//            System.out.println("Admin token: " + authenticationService.register(admin).getAccess_token());
//
//            var manager = RegisterRequest.builder()
//                    .firstName("manager")
//                    .lastName("manager")
//                    .email("manager@gmail.com")
//                    .password("password")
//                    .role(Role.MANAGER).build();
//            System.out.println("manager token: " + authenticationService.register(manager).getAccess_token());
//
//            //first watch
//            Brand b = Brand.builder().brandName("Citizen").build();
//            brandRepository.save(b);
//            Family f = Family.builder().familyName("Promaster").build();
//            familyRepository.save(f);
//
//            //band size
//            List<BraceletSize> braceletSizes = new ArrayList<>();
//            braceletSizes.add(BraceletSize.builder().size("5 1/2 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("5 3/4 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("6 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("6 1/4 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("6 1/2 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("6 3/4 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("7 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("7 1/4 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("7 1/2 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("7 3/4 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("8 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("8 1/4 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("8 1/2 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("8 3/4 inches").build());
//            braceletSizes.add(BraceletSize.builder().size("9 inches").build());
//            System.out.println("braceletSizes=" + braceletSizes);
//            Band band = Band.builder()
//                    .clasp("Safety Fold Over Clasp with Push Buttons")
//                    .type("Bracelet").color("Silver-tone")
//                    .width(22f).material("Stainless Steel").build();
//            band.setBraceletSizes(braceletSizes);
//            braceletRepository.save(band);
//            Dial dial = Dial.builder()
//                    .color("Black")
//                    .type("Analog")
//                    .hands("sword, Luminous Silver-tone").indexes("stick / Dot")
//                    .subDials("Three - 1/20th of a Second, 24 Hour and Alarm")
//                    .luminescence("Hands and Markers").build();
////            dialRepository.save(dial);
//            Case acase = Case.builder()
//                    .crystal("Scratch Resistant Sapphire")
//                    .crystalDescription("Superior scratch resistance and hardness")
//                    .material("Stainless Steel")
//                    .diameter(47f).thickness(15f)
//                    .shape("Round").back("Solid")
//                    .bezel("Sliderule (Pilot's Rotating Slide Rule Bezel)")
//                    .lugWidth(22f).waterResistance("WR200/20Bar/666ft [Swimming, Showering & Snorkeling]").build();
//            caseRepository.save(acase);
//
//            Movement m1 = Movement.builder()
//                    .brand("Citizen")
//                    .type("Quartz")
//                    .name("Caliber Eco-Drive F900")
//                    .display("Analog").date("Date, Day")
//                    .chronograph("Chronograph, Countdown, Flyback")
//                    .hands("Additional 12 Hour Hand (adjustable), Additional 24 Hour Hand (fixed), Hours, Minutes, " +
//                            "World Time")
//                    .acoustic("Alarm").powerReserve("Virtually unlimited").power("Solar-Powered")
//                    .additionalFunctions("GPS, Power Reserve Indicator, Solar Charging").build();
//            movementRepository.save(m1);
//            List<Dial> dials = new ArrayList<>();
//            dials.add(dial);
//
//            List<Image> images = new ArrayList<>();
//            images.add(Image.builder()
//                    .image("https://citizenwatch.widen.net/content/m9nog4gr8q/png/Promaster+Navihawk+GPS" +
//                            ".png?u=41zuoe&width=500&height=625&quality=80&crop=false&keep=c&color=FFFFFF00")
//                    .build());
//            images.add(Image.builder()
//                    .image("https://citizenwatch.widen.net/content/zukuxmblmp/png/Promaster+Navihawk+GPS" +
//                            ".png?u=41zuoe&width=500&height=625&quality=80&crop=false&keep=c&color=FFFFFF00")
//                    .build());
//            images.add(Image.builder()
//                    .image("https://citizenwatch.widen.net/content/1gfwjnoubk/png/Promaster+Navihawk+GPS" +
//                            ".png?u=41zuoe&width=500&height=625&quality=80&crop=false&keep=c&color=FFFFFF00")
//                    .build());
//            images.add(Image.builder()
//                    .image("https://citizenwatch.widen.net/content/jw5cfkfb8t/png/Promaster+Navihawk+GPS" +
//                            ".png?u=41zuoe&width=500&height=625&quality=80&crop=false&keep=c&color=FFFFFF00")
//                    .build());
//            images.add(Image.builder()
//                    .image("https://citizenwatch.widen.net/content/odeapwy2up/png/Promaster+Navihawk+GPS" +
//                            ".png?u=41zuoe&width=500&height=625&quality=80&crop=false&keep=c&color=FFFFFF00")
//                    .build());
//
//            Watch w = Watch.builder()
//                    .name("Navihawk GPS Stainless Steel / Black / Band")
//                    .brand(b).family(f)
//                    .reference("CC9030-51E")
//                    .gender("Men")
//                    .produced(LocalDate.of(2017, 10, 8))
//                    .origin("Japan")
//                    .description("Travel around the world with the newest addition to the Promaster collection." +
//                            " The Citizen® Promaster Navihawk GPS features Satellite GPS Timekeeping technology " +
//                            "with synchronized time adjustment available in 27 cities. Pilot's rotating slide rule " +
//                            "bezel, " +
//                            "chronograph, perpetual calendar, alarm, light level indicator,dual time and stainless " +
//                            "steel band. " +
//                            "Featuring our Eco-Drive technology – powered by light, any light. Never needs a battery." +
//                            " Caliber number F900.")
//                    .defaultPrices(1395f)
//                    .limited(false)
//                    .inventoryQuantity(100L)
//                    .warranty(5L).soldQuantity(50L)
//                    .band(band)
//                    .watchCase(acase)
//                    .movement(m1)
//                    .stars(4.9f)
//                    .build();
//            w.setImages(images);
//            w.setDials(dials);
//            watchRepository.save(w);
//
//            //review// it work!
//            Review review = Review.builder().review(null).watch(w).customer(
//                    customerRepository.findById(1L).orElseThrow()
//            ).ratingStars(5).datePosted(new Date()).comment("I bought this watch a month ago (early Christmas gift) " +
//                    "while on vacation in PR. I loved the idea of both the Ecco-Drive and the GPS time setting. It " +
//                    "took a little while to understand the owners manual, but once I was able to make sense of it, I " +
//                    "have a number of quarts watches and I am not a fan of having to get batteries replaced, even " +
//                    "only once a year and since they are dive watches, they have to be sent away to qualified service" +
//                    " techs. Up until this watch, my day to day was a Tag Heuer automatic, but it's original cost " +
//                    "relegated it to a dress/special occasion watch. \n" +
//                    " \n" +
//                    " This is a good looking and functional watch, great for everyday wear.\n").loves(10L).build();
//            //add new review with watch
//            reviewRepository.save(review);
//
//            //add child review for first review
//            Review reply1 = Review.builder()
//                    .comment("Thank you for sharing your experience with the watch. Indeed, transitioning to using an" +
//                            " Eco-Drive watch with GPS time setting may require some time to understand the user " +
//                            "manual, but afterwards, you found it to be very satisfactory in terms of functionality " +
//                            "and utility. The difference in not needing to frequently change batteries and not having" +
//                            " to send it away for servicing is a major advantage of Eco-Drive watches compared to " +
//                            "traditional quartz ones. This watch is not only aesthetically pleasing but also very " +
//                            "suitable for everyday use.\n"
//                            )
//                    .review(review)
//                    .customer(customerRepository.findById(2L).orElseThrow()
//                    )
//                    .build();
//            reviewRepository.save(reply1);
//
//            //save another review
//            for (int i = 1; i <= 21; i++) {
//                Review review2 = Review.builder().review(null).watch(w).customer(
//                        customerRepository.findById(1L).orElseThrow()
//                ).ratingStars(5).datePosted(new Date()).comment("test review").build();
//                //add new review with watch
//                reviewRepository.save(review2);
//            }
//
//            for (int i = 1; i <= 2; i++) {
//                Review review2 = Review.builder().review(null).watch(w).customer(
//                        customerRepository.findById(1L).orElseThrow()
//                ).ratingStars(4).comment("first comment").build();
//                //add new review with watch
//                reviewRepository.save(review2);
//            }
//
//            for (int i = 1; i <= 1; i++) {
//                Review review2 = Review.builder().review(null).watch(w).customer(
//                        customerRepository.findById(1L).orElseThrow()
//                ).ratingStars(3).comment("first comment").build();
//                //add new review with watch
//                reviewRepository.save(review2);
//            }
//            for (int i = 1; i <= 2; i++) {
//                Review review2 = Review.builder().review(null).watch(w).customer(
//                        customerRepository.findById(1L).orElseThrow()
//                ).ratingStars(1).comment("first comment").build();
//                //add new review with watch
//                reviewRepository.save(review2);
//            }
//
//
////            try another way // => work
////            Review review = Review.builder().review(null).childReviews(new ArrayList<>()).watch(w).customer(
////                    customerRepository.findById(1L).orElseThrow()
////            ).comment("first comment").build();
////            //add new review with watch
////            reviewRepository.save(review);
////
//////            and then in controller class: => it work
//////            try to add new reply to review
////            Review r = reviewRepository.findById(1L).orElseThrow();
////            Review reply1 = Review.builder()
////                    .comment("reply first comment")
////                    .build();
////            r.addChildReview(reply1);
//
//            //--**review**--//
//
//            //--add product details to cart--//
//
//
//            /*watch 2*/
//            Brand b2 = Brand.builder().brandName("Rolex").build();
//            brandRepository.save(b2);
//            Family f2 = Family.builder().familyName("Datejust").build();
//            familyRepository.save(f2);
//            Band band2 = Band.builder()
//                    .clasp("Concealed folding Crownclasp")
//                    .type("Band").color("Gold/Steel")
//                    .width(22f).material("Yellow Rolesor - combination of Oystersteel and yellow gold").build();
//            braceletRepository.save(band2);
//            Dial dial2 = Dial.builder()
//                    .color("Olive green set with diamonds")
//                    .type("Analog")
//                    .hands("Stick").indexes("Roman Numerals")
//                    .gemSetting("Large VI set with 11 diamonds")
//                    .img("https://content.rolex.com/v7/dam/2023-06/configurator/raw-dial-with-shadow/51734_y_31" +
//                            ".png?impolicy=v7-main-configurator&imwidth=320")
//                    .build();
//            Dial dial21 = Dial.builder()
//                    .color("Silver set with diamonds")
//                    .type("Analog")
//                    .hands("Stick").indexes("Roman Numerals")
//                    .img("https://content.rolex.com/v7/dam/2023-06/configurator/raw-dial-with-shadow/50740_y_31" +
//                            ".png?impolicy=v7-main-configurator&imwidth=320")
//                    .gemSetting("Large VI set with 11 diamonds")
//                    .build();
//            Dial dial22 = Dial.builder()
//                    .color("Olive green, floral motif set with diamonds")
//                    .type("Analog")
//                    .hands("Stick").indexes("Roman Numerals")
//                    .img("https://content.rolex.com/v7/dam/2023-06/configurator/raw-dial-with-shadow/52882_y_31" +
//                            ".png?impolicy=v7-main-configurator&imwidth=320")
//                    .gemSetting("Large VI set with 11 diamonds")
//                    .build();
//
//            Case acase2 = Case.builder()
//                    .crystal("Sapphire")
//                    .crystalDescription("Scratch-resistant sapphire, Cyclops lens over the date")
//                    .material("Yellow Gold, Stainless Steel")
//                    .diameter(31f).thickness(15f)
//                    .shape("Round")
//                    .bezel("Set with diamonds")
//                    .back("Closed")
//                    .lugWidth(16f).waterResistance("Waterproof to 100 metres / 330 feet").build();
//            caseRepository.save(acase2);
//
//
//            Movement m2 = Movement.builder()
//                    .brand("Rolex")
//                    .type("Automatic")
//                    .name("Rolex caliber 2236")
//                    .display("Analog").date("Date")
//                    .diameter(20f)
//                    .jewels(31)
//                    .powerReserve("55")
//                    .hands("Hours, Minutes, Seconds")
//                    .precision("-2/+2 sec/day, after casing")
//                    .additionalFunctions("Centre hour, minute and seconds hands. Instantaneous date with rapid " +
//                            "setting. Stop-seconds for precise time setting\n")
//                    .frequency(28800)
//                    .power("Winding\n" +
//                            "Bidirectional self-winding via Perpetual rotor")
//                    .build();
//            movementRepository.save(m2);
//
//            Watch w2 = Watch.builder()
//                    .name("Datejust 31 Stainless Steel / Yellow Gold / Diamond / Olive - Roman / Jubilee")
//                    .brand(b2).family(f2)
//                    .reference("278383rbr-0016")
//                    .gender("Men")
//                    .produced(LocalDate.of(2018, 1, 1))
//                    .origin("Swiss")
//                    .description("The updated Rolex Datejust 31 was introduced in 2018 in either rose, white or " +
//                            "yellow gold - all powered by the new caliber 2236. The two tone 'Rolesor' variations " +
//                            "were added to the collection one year later.\n" +
//                            "Ref. 278383rbr has a stainless steel case with a diamond-set yellow gold bezel. It is " +
//                            "available with a number of different dials and can be fitted with either an Oyster or " +
//                            "Jubilee band.")
//                    .defaultPrices(21f)
//                    .limited(false)
//                    .inventoryQuantity(10)
//                    .warranty(5L).soldQuantity(50L)
//                    .band(band2)
//                    .watchCase(acase2)
//                    .movement(m2)
//                    .stars(4.5f)
//                    .build();
//
//            List<Image> images1 = new ArrayList<>();
//            images1.add(Image.builder().image("https://cdn.watchbase.com/watch/lg/origin:png/rolex/datejust-31" +
//                    "/278383rbr-0016-b1.webp").build());
//            images1.add(Image.builder().image("https://content.rolex.com/v7/dam/2023-06/upright-c/m278383rbr-0016" +
//                    ".png?impolicy=v7-main-configurator&imwidth=320").build());
//            List<Dial> dials1 = new ArrayList<>();
//            dials1.add(dial2);
//            dials1.add(dial21);
//            dials1.add(dial22);
//
//            w2.setImages(images1);
//            w2.setDials(dials1);
//            watchRepository.save(w2);
////
////
////            //paypal note:
////            //Tạo url để lấy access token sau đó gắn access token vào header để gửi request
////            //intent = authorize : tạo order và authorize (ủy quyền đặt cọc (chưa lấy tiền trong tk khách hàng)
////            //intent = capture : tạo order và capture (lấy tiền trong tk khách hàng)
////            //nếu dùng authorize thì phải gửi request authorize để lấy tiền
////            //nếu dùng capture thì sau đó phải capture để lấy tiền
////            //PayPal-Request-Id: có thể tự sinh 1 cái id
////            //response sẽ trả data lại trong đó có cái link để redirect qua trang thanh toán của paypal
////            //sau khi thanh toán xong thì paypal sẽ redirect lại trang return_url hoặc cancel_url
////            //sau khi thanh toán trạng thái order trở thành approved
////            //nếu không thanh toán thì trạng thái order trở thành voided?
////            //nếu thanh toán mà không đủ tiền thì trạng thái order trở thành failed
////            //Sử dụng id của order để lấy thông tin chi tiết của order và thực hiện capture, authorize
////            //Có thể sử dụng capture id để thực hiện refund
////
////
////            /*
////            URL url = new URL("https://api-m.sandbox.paypal.com/v2/checkout/orders");
////            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
////            httpConn.setRequestMethod("POST");
////
////            httpConn.setRequestProperty("Content-Type", "application/json");
////            httpConn.setRequestProperty("PayPal-Request-Id", "7b92603e-77ed-4896-8e78-5dsea2050476ab");
////            httpConn.setRequestProperty("Authorization", "Bearer A21AAJW3Xm9mxGB1gM5Vcw0U2b3LyQV9UfcMNWjLiZw6OQWADup9OBG3aNAFXdtXDrWxLeRbZgvzAejNCFZEn_OK4knQmdZaw");
////
////            httpConn.setDoOutput(true);
////            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
////            writer.write("{ \"intent\": \"AUTHORIZE\", \"purchase_units\": [ { \"reference_id\": " +
////                    "\"d9f80740-38f0-11e8-b467-0ed5f89f718b\", \"amount\": { \"currency_code\": \"USD\", \"value\": \"1000.00\" }, \"shipping\": { \"address\": { \"address_line_1\": \"123 Main Street\", \"address_line_2\": \"Apt 101\", \"admin_area_1\": \"CA\", \"admin_area_2\": \"San Jose\", \"postal_code\": \"95131\", \"country_code\": \"US\" } } } ], \"payment_source\": { \"paypal\": { \"experience_context\": { \"payment_method_preference\": \"IMMEDIATE_PAYMENT_REQUIRED\", \"brand_name\": \"EXAMPLE INC\", \"locale\": \"en-US\", \"landing_page\": \"LOGIN\", \"shipping_preference\": \"SET_PROVIDED_ADDRESS\", \"user_action\": \"PAY_NOW\", \"return_url\": \"https://example.com/returnUrl\", \"cancel_url\": \"https://example.com/cancelUrl\" } } } }");
////            writer.flush();
////            writer.close();
////            httpConn.getOutputStream().close();
////
////            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
////                    ? httpConn.getInputStream()
////                    : httpConn.getErrorStream();
////            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
////            String response = s.hasNext() ? s.next() : "";
////
////            System.out.println(response);
////
////             */
//        };
//    }
}
