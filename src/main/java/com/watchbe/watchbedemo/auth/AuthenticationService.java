package com.watchbe.watchbedemo.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.watchbe.watchbedemo.auth.email.EmailSender;
import com.watchbe.watchbedemo.config.JwtService;
import com.watchbe.watchbedemo.exception.NotFoundException;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.*;
import com.watchbe.watchbedemo.token.ConfirmationToken;
import com.watchbe.watchbedemo.token.Token;
import com.watchbe.watchbedemo.token.TokenType;
import com.watchbe.watchbedemo.model.account.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
//    private final EmailSender emailSender;
    private final CartRepository cartRepository;


    public AuthenticationResponse register(RegisterRequest registerRequest) throws Exception {
        Cart cart = Cart.builder().build();
//        cartRepository.save(cart);
        var account = Account.builder()
                .email(registerRequest.getEmail())
                .enabled(true)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();
        var customer = Customer.builder()
                .avatarLink("https://ik.imagekit.io/onlineBook/watch/unnamed%20copy.jpg?updatedAt=1709386785313")
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .gender(registerRequest.getGender())
                .account(account)
                .cart(cart)
                .email(registerRequest.getEmail())
                .build();

        List<ShippingAddress> shippingAddresses = new ArrayList<>();
        shippingAddresses.add(
                ShippingAddress.builder()
                        .address("123/4 duong Le Van Sy")
                        .city("Can Tho")
                        .district("Ninh Kieu")
                        .ward("Hung Loi")
                        .phone("0123456789")
                        .type(AddressType.PRIVATE)
                        .name("Tong Cong Minh")
                        .isDefault(true)
                        .build()
        );
        shippingAddresses.add(
                ShippingAddress.builder()
                        .address("123/4 duong 3 thang 2")
                        .city("Can Tho")
                        .district("Ninh Kieu")
                        .ward("An Khanh")
                        .phone("1234567890")
                        .type(AddressType.COMPANY)
                        .name("Cong Ty Det May Minh Tien")
                        .isDefault(false)
                        .build()
        );
        customer.setShippingAddresses(shippingAddresses);

        var savedCustomer = customerRepository.save(customer);
        var savedAccount = savedCustomer.getAccount();

        var jwtToken = jwtService.generateToken(account);
        var refreshToken = jwtService.generateRefreshToken(account);
        var token = Token.builder()
                .account(savedAccount)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);

        List<Integer> roleSPrivateValue = new ArrayList<>();
        //loop through account's roles if there are many roles
        roleSPrivateValue.add(savedAccount.getRole().getValue());
        return AuthenticationResponse.builder()
                .access_token(jwtToken)
                .refresh_token(refreshToken)
                .roles(roleSPrivateValue)
                .build();
    }

    public ResponseEntity<AuthenticationResponse> register(String method,
                                                           Long tempoCartId,
                                                           RegisterRequest registerRequest,
                                                           HttpServletResponse response) {
        boolean accountExists = accountRepository.findByEmail(registerRequest.getEmail()).isPresent();
        System.out.println("es"+registerRequest);
        System.out.println("regis email="+registerRequest.getEmail());
        System.out.println("accountExists="+accountExists);
        if(accountExists){
            log.error("email taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    AuthenticationResponse.builder().message("Email taken.").build()
            );
        }

        var account = Account.builder()
                .email(registerRequest.getEmail())
                .enabled(true)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .createdAt(new Date())
                .build();
        Customer savedCustomer = null;
        try{
            Cart cart = Cart.builder().orderDetails(new ArrayList<>()).build();
//            cartRepository.save(cart);

            var customer = Customer.builder()
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .gender(registerRequest.getGender())
                    .account(account)
                    .email(registerRequest.getEmail())
                    .build();
            customer.setCart(cart);
            savedCustomer = customerRepository.save(customer);
            if(method.equals("checkout")){
                List<OrderDetails> orderDetails = handleItems(tempoCartId);
               orderDetails.forEach(cart::addOrderDetails);
               cartRepository.save(cart);
            }

        }catch (DataIntegrityViolationException e){
              return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    AuthenticationResponse.builder().message("Phone taken.").build()
            );
        }
        var savedAccount = savedCustomer.getAccount();

        var jwtToken = jwtService.generateToken(account);
        var refreshToken = jwtService.generateRefreshToken(account);
        var token = Token.builder()
                .account(savedAccount)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);

//        String confirmTokenGeneration = UUID.randomUUID().toString();
//        ConfirmationToken confirmationToken = ConfirmationToken.builder()
//                .token(confirmTokenGeneration)
//                .createdAt(LocalDateTime.now())
//                .expiresAt(LocalDateTime.now().plusMinutes(15))
//                .account(savedAccount).build();
//        confirmationTokenRepository.save(confirmationToken);

        List<Integer> roleSPrivateValue = new ArrayList<>();
        //loop through account's roles if there are many roles
        roleSPrivateValue.add(savedAccount.getRole().getValue());

//        String activeLink = "http://localhost:3000/active-account/" + confirmTokenGeneration;
//        emailSender.send(registerRequest.getEmail(), buildEmail(registerRequest.getFirstName(), activeLink));

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .email(account.getEmail())
                .access_token(jwtToken)
                .refresh_token(refreshToken)
                .roles(roleSPrivateValue)
                        .userId(savedCustomer.getId())
                        .cartId(savedCustomer.getCart().getId())
                .build());
    }

    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest
                                            ) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );
            var account = accountRepository.findByEmail(authenticationRequest.getEmail()).orElseThrow();

            var jwtToken = jwtService.generateToken(account);
            var refreshToken = jwtService.generateRefreshToken(account);

            //all tokens must be revoked
            revokeAllUserTokens(account);

            var token = Token.builder()
                    .account(account)
                    .token(jwtToken)
                    .tokenType(TokenType.BEARER)
                    .expired(false)
                    .revoked(false)
                    .build();
            tokenRepository.save(token);
            System.out.println("authenticate at: "+ LocalDateTime.now());
            //update account last login
//            LocalDateTime newLogin = LocalDateTime.now();
            account.setLastLogin(new Date());
            accountRepository.save(account);

            List<Integer> roleSPrivateValue = new ArrayList<>();
            //loop through account's roles if there are many roles
            roleSPrivateValue.add(account.getRole().getValue());

            Customer customer = customerRepository.findCustomerByAccountId(account.getId());
            Cart cart = customer.getCart();

            return ResponseEntity.ok(AuthenticationResponse
                    .builder()
                            .email(account.getEmail())
                            .access_token(jwtToken)
                            .refresh_token(refreshToken)
                            .cartId(cart.getId())
                            .userId(customer.getId())
                    .roles(roleSPrivateValue).build());
        }catch (DisabledException e){
            //return a response instead
//            throw new RuntimeException("Your account is disabled. Please contact support for assistance.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthenticationResponse.builder()
                            .message("Your account is disabled. Please contact support for assistance.")
                            .build());

        }catch (AuthenticationException e){
//            throw new RuntimeException("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder()
                            .message("Authentication failed: " + e.getMessage())
                            .build());
        }
    }

    public ResponseEntity<AuthenticationResponse> authenticateCheckout(Long tempoCartId,
                                                                       AuthenticationRequest authenticationCheckoutRequest) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationCheckoutRequest.getEmail(),
                            authenticationCheckoutRequest.getPassword()
                    )
            );
            var account = accountRepository.findByEmail(authenticationCheckoutRequest.getEmail()).orElseThrow();

            var jwtToken = jwtService.generateToken(account);
            var refreshToken = jwtService.generateRefreshToken(account);

            //all tokens must be revoked
            revokeAllUserTokens(account);

            var token = Token.builder()
                    .account(account)
                    .token(jwtToken)
                    .tokenType(TokenType.BEARER)
                    .expired(false)
                    .revoked(false)
                    .build();
            tokenRepository.save(token);
            System.out.println("authenticate at: "+ LocalDateTime.now());
            //update account last login
//            LocalDateTime newLogin = LocalDateTime.now();
            account.setLastLogin(new Date());
            accountRepository.save(account);

            List<Integer> roleSPrivateValue = new ArrayList<>();
            //loop through account's roles if there are many roles
            roleSPrivateValue.add(account.getRole().getValue());

            Customer customer = customerRepository.findCustomerByAccountId(account.getId());
            Cart cart = customer.getCart();

            //handle old order details list


            //set the new items for current customer
//            cart.setOrderDetails(orderDetails); // => the method set will not working if find and update operations
//            are in the same transaction
//            System.out.println("order details = "+orderDetails);
            List<OrderDetails> orderDetails = handleItems(tempoCartId);
            orderDetails.forEach(cart::addOrderDetails);
            cartRepository.save(cart);

            //after that the user will be redirected to check out page

            return ResponseEntity.ok(AuthenticationResponse
                    .builder()
                    .email(account.getEmail())
                    .access_token(jwtToken)
                    .refresh_token(refreshToken)
                    .cartId(cart.getId())
                    .userId(customer.getId())
                    .roles(roleSPrivateValue).build());
        }catch (DisabledException e){
            //return a response instead
//            throw new RuntimeException("Your account is disabled. Please contact support for assistance.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthenticationResponse.builder()
                            .message("Your account is disabled. Please contact support for assistance.")
                            .build());

        }catch (AuthenticationException e){
//            throw new RuntimeException("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder()
                            .message("Authentication failed: " + e.getMessage())
                            .build());
        }
    }

    private List<OrderDetails> handleItems (Long tempoCartId){
        Cart cart =
                cartRepository.findById(tempoCartId).orElseThrow(()-> new NotFoundException("cart with id "+tempoCartId+" not found!"));

        //get items of tempo cart set for new account
        //return cart item list //=> bad process => should move OrderDetails to another list before delete cart
//        List<OrderDetails> orderDetails = cart.getOrderDetails();
        List<OrderDetails> orderDetails = new ArrayList<>(cart.getOrderDetails());

        //delete cart and order items in cart

//        cartRepository.delete(cart);

        //return cart item list //=> bad process => should move OrderDetails to another list before delete cart
        return orderDetails;
    }

    private void revokeAllUserTokens(Account account) {
        var validUserTokens = tokenRepository.findAllValidTokensByAccount(account.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final  String refreshToken;
        final String userEmail;
        System.out.println("header = "+authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);

        System.out.println("refresh token receive = "+refreshToken);

        System.out.println(refreshToken);

        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            //user account
            var userDetails = this.accountRepository.findByEmail(userEmail).orElseThrow();

            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                log.info("refresh at: "+ LocalDateTime.now());
                var accessToken = jwtService.generateToken(userDetails);

                revokeAllUserTokens(userDetails);

                var token = Token.builder()
                        .account(userDetails)
                        .token(accessToken)
                        .tokenType(TokenType.BEARER)
                        .expired(false)
                        .revoked(false)
                        .build();
                tokenRepository.save(token);

                //roles
                List<Integer> roles = new ArrayList<>();
                roles.add(userDetails.getRole().getValue());

                Customer customer = customerRepository.findCustomerByAccountId(userDetails.getId());
                Cart cart = customer.getCart();

                var authResponse = AuthenticationResponse.builder()
                        .email(userEmail)
                        .roles(roles)
                        .cartId(cart.getId())
                        .userId(customer.getId())
                        .access_token(accessToken)
                        .refresh_token(refreshToken).build();


                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    @Transactional
    public ResponseEntity<AuthenticationResponse> confirm(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(()-> new IllegalStateException("token not found"));
        if(confirmationToken.getConfirmedAt()!=null){
            throw new IllegalStateException("email already confirmed");

        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        var account = confirmationToken.getAccount();
        var customer = customerRepository.findCustomerByAccountId(account.getId());

        if (expiredAt.isBefore(LocalDateTime.now())) {
            String confirmTokenGeneration = UUID.randomUUID().toString();
            ConfirmationToken newConfirmationToken = ConfirmationToken.builder()
                    .token(confirmTokenGeneration)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .account(account).build();

            confirmationTokenRepository.save(newConfirmationToken);

            String activeLink = "http://localhost:3000/active-account/" + confirmTokenGeneration;
//            emailSender.send(account.getEmail(), buildEmail(customer.getFirstName(), activeLink));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthenticationResponse.builder().email(account.getEmail()).message("Confirmation token expired. Please check your email again. You have three time left.").build());
        }

        confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
        accountRepository.enableUser(confirmationToken.getAccount().getEmail());


        var jwtToken = jwtService.generateToken(account);
        var refreshToken = jwtService.generateRefreshToken(account);

        //all tokens must be revoked
        revokeAllUserTokens(account);

        var newToken = Token.builder()
                .account(account)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(newToken);
        log.info("confirm at: "+ LocalDateTime.now());

        //after active account, the customer will be provided a shopping cart
        Cart cart = Cart.builder().build();
        customer.setCart(cart);
        customerRepository.save(customer);

        List<Integer> roleSPrivateValue = new ArrayList<>();
        //loop through account's roles if there are many roles
        roleSPrivateValue.add(account.getRole().getValue());

        return ResponseEntity.ok(AuthenticationResponse
                .builder()
                .email(account.getEmail())
                .access_token(jwtToken)
                .refresh_token(refreshToken)
                        .cartId(customer.getCart().getId())
                        .userId(customer.getId())
                .roles(roleSPrivateValue).build());
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }



}
