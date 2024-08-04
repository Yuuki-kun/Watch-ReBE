package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.dto.CustomerDto;
import com.watchbe.watchbedemo.dto.OrderDto;
import com.watchbe.watchbedemo.dto.ShippingAddressDto;
import com.watchbe.watchbedemo.exception.NotFoundException;
import com.watchbe.watchbedemo.mapper.OrderMapperImpl;
import com.watchbe.watchbedemo.model.Customer;
import com.watchbe.watchbedemo.model.Order;
import com.watchbe.watchbedemo.model.ShippingAddress;
import com.watchbe.watchbedemo.model.Status;
import com.watchbe.watchbedemo.model.account.Account;
import com.watchbe.watchbedemo.repository.AccountRepository;
import com.watchbe.watchbedemo.repository.CustomerRepository;
import com.watchbe.watchbedemo.repository.OrderRepository;
import com.watchbe.watchbedemo.repository.ShippingAddressRepository;
import com.watchbe.watchbedemo.service.EmailService;
import com.watchbe.watchbedemo.service.StorageService;
import com.watchbe.watchbedemo.token.Token;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final CustomerRepository customerRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final OrderRepository orderRepository;
    private final OrderMapperImpl orderMapper;
    private final StorageService storageService;
    @GetMapping("/name/{email}")
    public ResponseEntity<Object> getUserName(@PathVariable("email") String email){
        Customer customer= customerRepository.findCustomerByEmail(email).orElseThrow(()-> new NotFoundException((
                "user with " +
                "email "+email+" not found!")));
        //using a Map to return  name and avatar link of the customer instead of creating a new dto
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("fullName", customer.getFirstName() + " " + customer.getLastName());
        userInfo.put("avatar", customer.getAvatarLink());
        //change return to new ResponseEntity
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/cus-info/{cid}")
    //get mapping customer dto by id
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable("cid") Long cid){
        Customer customer= customerRepository.findById(cid).orElseThrow();

        return ResponseEntity.ok(findCustomerDtoById(customer));
    }

    @PostMapping("/cus-info/{cid}/avatar")
    public ResponseEntity<CustomerDto> updateAvatar(@RequestParam("updateAvatar") MultipartFile updateAvatar,
                                                    @PathVariable("cid")Long cid) throws IOException {
        String savedAvatarName = storageService.uploadImage(updateAvatar);
        Customer customer= customerRepository.findById(cid).orElseThrow();
        customer.setAvatarLink(savedAvatarName);
        customerRepository.save(customer);
        return ResponseEntity.ok(findCustomerDtoById(customer));

    }

    private CustomerDto findCustomerDtoById(Customer customer) {
        List<ShippingAddress> shippingAddresses = shippingAddressRepository.findShippingAddressByCustomerId(customer.getId());
        //map shipping address to shipping address dto
        List<ShippingAddressDto> shippingAddressDtos = shippingAddresses.stream().map(shippingAddress -> ShippingAddressDto.builder()
                .id(shippingAddress.getId())
                .name(shippingAddress.getName())
                .phone(shippingAddress.getPhone())
                .address(shippingAddress.getAddress())
                .type(shippingAddress.getType())
                .companyName(shippingAddress.getCompanyName())
                .ward(shippingAddress.getWard())
                .district(shippingAddress.getDistrict())
                .city(shippingAddress.getCity())
                .isDefault(shippingAddress.getIsDefault())
                .build()).collect(Collectors.toList());


        return (CustomerDto.builder()
                .id(customer.getId())
                .avatarLink(customer.getAvatarLink())
                .gender(customer.getGender())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .shippingAddresses(shippingAddressDtos)
                .phoneNumber(customer.getPhoneNumber()).build());
    }

    @GetMapping("/cus-address/{cid}")
    public ResponseEntity<List<ShippingAddressDto>> getCustomerAddress(@PathVariable("cid") Long cid){
        Customer customer= customerRepository.findById(cid).orElseThrow();
        List<ShippingAddress> shippingAddresses = shippingAddressRepository.findShippingAddressByCustomerId(customer.getId());
        //map shipping address to shipping address dto
        List<ShippingAddressDto> shippingAddressDtos = shippingAddresses.stream().map(shippingAddress -> ShippingAddressDto.builder()
                .id(shippingAddress.getId())
                .name(shippingAddress.getName())
                .phone(shippingAddress.getPhone())
                .address(shippingAddress.getAddress())
                .type(shippingAddress.getType())
                .companyName(shippingAddress.getCompanyName())
                .ward(shippingAddress.getWard())
                .district(shippingAddress.getDistrict())
                .city(shippingAddress.getCity())
                .isDefault(shippingAddress.getIsDefault())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(shippingAddressDtos);
    }

    @DeleteMapping("/cus-address/{cid}/{aid}")
    public ResponseEntity<?> deleteAddress(@PathVariable("cid") Long cid, @PathVariable("aid") Long aid){
       ShippingAddress shippingAddress = shippingAddressRepository.findById(aid).orElseThrow();
       shippingAddress.setCustomer(null);
        //map shipping address to shipping address dto

        shippingAddressRepository.save(shippingAddress);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/cus-address/default/{cid}/{aid}")
    public ResponseEntity<List<ShippingAddressDto>> updateDefaultAddress(@PathVariable("cid") Long cid, @PathVariable("aid") Long aid){
        Customer customer= customerRepository.findById(cid).orElseThrow();
        List<ShippingAddress> shippingAddresses = shippingAddressRepository.findShippingAddressByCustomerId(customer.getId());
        //set default address
        shippingAddresses.forEach(shippingAddress -> {
            if(shippingAddress.getId().equals(aid)){
                shippingAddress.setIsDefault(true);
            }else{
                shippingAddress.setIsDefault(false);
            }
            shippingAddressRepository.save(shippingAddress);
        });
//        map shipping address to shipping address dto
        List<ShippingAddressDto> shippingAddressDtos = shippingAddresses.stream().map(shippingAddress -> ShippingAddressDto.builder()
                .id(shippingAddress.getId())
                .name(shippingAddress.getName())
                .phone(shippingAddress.getPhone())
                .address(shippingAddress.getAddress())
                .type(shippingAddress.getType())
                .companyName(shippingAddress.getCompanyName())
                .ward(shippingAddress.getWard())
                .district(shippingAddress.getDistrict())
                .city(shippingAddress.getCity())
                .isDefault(shippingAddress.getIsDefault())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(shippingAddressDtos);
    }

    @GetMapping("/cus-orders/{cid}/{status}")
    public ResponseEntity<List<OrderDto>> getCustomerOrders(@PathVariable("cid") Long cid,
                                                            @PathVariable("status") String status){

//        List<Order> order = orderRepository.findAllByCustomer_IdOrderByOrderDateDesc(cid);
        if(status.equals("ALL")){
            List<Order> order = orderRepository.findAllByCustomer_IdOrderByOrderDateDesc(cid);
            return ResponseEntity.ok(order.stream().map(orderMapper::mapTo).collect(Collectors.toList()));
        }else if(status.equals("SHIPPING")){
            List<Order> order = orderRepository.findAllByCustomer_IdAndOrderStatus_statusOrderByOrderDateDesc(cid, Status.SHIPPING);
            return ResponseEntity.ok(order.stream().map(orderMapper::mapTo).collect(Collectors.toList()));
        }else if(status.equals("DELIVERED")){
            List<Order> order = orderRepository.findAllByCustomer_IdAndOrderStatus_statusOrderByOrderDateDesc(cid, Status.DELIVERED);
            return ResponseEntity.ok(order.stream().map(orderMapper::mapTo).collect(Collectors.toList()));  }
        else if(status.equals("CANCELLED")){
            List<Order> order = orderRepository.findAllByCustomer_IdAndOrderStatus_statusOrderByOrderDateDesc(cid, Status.CANCELLED);
            return ResponseEntity.ok(order.stream().map(orderMapper::mapTo).collect(Collectors.toList()));
        }else if(status.equals("PENDING")){
            List<Order> order = orderRepository.findAllByCustomer_IdAndOrderStatus_statusOrderByOrderDateDesc(cid, Status.PENDING);
            return ResponseEntity.ok(order.stream().map(orderMapper::mapTo).collect(Collectors.toList()));
        }else if(status.equals("CREATED")) {
            List<Order> order = orderRepository.findAllByCustomer_IdAndOrderStatus_statusOrderByOrderDateDesc(cid, Status.CREATED);
            return ResponseEntity.ok(order.stream().map(orderMapper::mapTo).collect(Collectors.toList()));
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{cid}/address")
    public ResponseEntity<List<ShippingAddressDto>> addAddress(@PathVariable("cid") Long cid,
                                                            @RequestBody ShippingAddressDto shippingAddressDto){
        Customer customer= customerRepository.findById(cid).orElseThrow(()-> new NotFoundException((
                "user with " +
                        "id "+cid+" not found!")));
        List<ShippingAddress> shippingAddresses = shippingAddressRepository.findShippingAddressByCustomerId(customer.getId());
        //set default address
        shippingAddresses.forEach(shippingAddress -> {
            if(shippingAddress.getIsDefault())
                shippingAddress.setIsDefault(false);
            shippingAddressRepository.save(shippingAddress);
        });

        ShippingAddress shippingAddress = ShippingAddress.builder()
                .name(shippingAddressDto.getName())
                .phone(shippingAddressDto.getPhone())
                .address(shippingAddressDto.getAddress())
                .type(shippingAddressDto.getType())
                .companyName(shippingAddressDto.getCompanyName())
                .ward(shippingAddressDto.getWard())
                .district(shippingAddressDto.getDistrict())
                .city(shippingAddressDto.getCity())
                .isDefault(shippingAddressDto.getIsDefault())
                .customer(customer)
                .build();
        shippingAddressRepository.save(shippingAddress);


        List<ShippingAddressDto> shippingAddressDtos =
                shippingAddressRepository.findShippingAddressByCustomerId(customer.getId()).stream().map(sd -> ShippingAddressDto.builder()
                .id(sd.getId())
                .name(sd.getName())
                .phone(sd.getPhone())
                .address(sd.getAddress())
                .type(sd.getType())
                .companyName(sd.getCompanyName())
                .ward(sd.getWard())
                .district(sd.getDistrict())
                .city(sd.getCity())
                .isDefault(sd.getIsDefault())
                .build()).collect(Collectors.toList());
        System.out.println("shippingAddressDtos="+shippingAddressDtos);
        return ResponseEntity.ok(shippingAddressDtos);
    }
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    @PostMapping("change-password")
    public ResponseEntity<?> changePassword( @RequestBody ChangePasswordRequest changePasswordRequest
    , Principal connectedUser){

        var user = (Account)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())){
            throw new IllegalStateException("Old password is incorrect");
        }
        if(!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())){
            throw new IllegalStateException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        accountRepository.save(user);
        return ResponseEntity.ok().build();
    }
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
    private final EmailService emailService;

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

    @PutMapping("/cus-info/{cid}")
    public ResponseEntity<CustomerDto> updateCustomerInfo(@PathVariable("cid") Long cid,
                                                          @RequestBody CustomerDto customerDto){
        Customer customer= customerRepository.findById(cid).orElseThrow();
        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setGender(customerDto.getGender());
        customerRepository.save(customer);
        return ResponseEntity.ok(findCustomerDtoById(customer));
    }

}
