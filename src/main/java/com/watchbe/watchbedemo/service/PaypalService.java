package com.watchbe.watchbedemo.service;

import com.google.gson.*;
import com.stripe.exception.StripeException;
import com.watchbe.watchbedemo.config.PaypalConfig;
import com.watchbe.watchbedemo.model.Order;
import com.watchbe.watchbedemo.model.OrderStatus;
import com.watchbe.watchbedemo.model.OrderStatusHistory;
import com.watchbe.watchbedemo.model.Status;
import com.watchbe.watchbedemo.repository.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service

public class PaypalService extends CheckoutService {
    private final PaypalConfig paypalConfig;
    private final PaypalAccessTokenStore paypalAccessTokenStore;
    private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private String getAuth(){
        return Base64.getEncoder().encodeToString((paypalConfig.getClientId()+":"+paypalConfig.getClientSecret()).getBytes());
    }

    public PaypalService(OrderRepository orderRepository, OrderDetailsRepository orderDetailsRepository,
                         CustomerRepository customerRepository, OrderStatusRepository orderStatusRepository,
                         PaymentRepository paymentRepository,
                         PaypalConfig paypalConfig,
                         PaypalAccessTokenStore paypalAccessTokenStore, WatchRepository watchRepository,
                         ShippingRepository shippingRateRepository
    ) {
        super(orderRepository, orderDetailsRepository, customerRepository, orderStatusRepository, paymentRepository, watchRepository, shippingRateRepository);
        this.paypalConfig = paypalConfig;
        this.paypalAccessTokenStore = paypalAccessTokenStore;
    }

    @Override
    protected String createPaymentIntent(Order order) {
        String accessToken = paypalAccessTokenStore.getAccessToken("access_token");
        if(accessToken == null) {
            System.out.println("token is expired");
            accessToken = generateAccessToken();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

//        String requestBody = "{ \"intent\": \"CAPTURE\", \"purchase_units\": [ { \"reference_id\": \"d9f80740-38f0-11e8-b467-0ed5f89f718b\", \"amount\": { \"currency_code\": \"USD\", \"value\": \"100.00\" } } ], \"payment_source\": { \"paypal\": { \"experience_context\": { \"payment_method_preference\": \"IMMEDIATE_PAYMENT_REQUIRED\", \"brand_name\": \"EXAMPLE INC\", \"locale\": \"en-US\", \"landing_page\": \"LOGIN\", \"shipping_preference\": \"SET_PROVIDED_ADDRESS\", \"user_action\": \"PAY_NOW\", \"return_url\": \"https://example.com/returnUrl\", \"cancel_url\": \"https://example.com/cancelUrl\" } } } }";

        Gson gson = new Gson();

// Tạo một đối tượng JsonObject và đưa dữ liệu vào đó
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("intent", "AUTHORIZE");

// Tạo một mảng JsonObject cho purchase_units
        JsonObject purchaseUnit = new JsonObject();
//        purchaseUnit.addProperty("reference_id", "d9f80740-38f0-11e8-b467-0ed5f89af718b");

        JsonObject amount = new JsonObject();
        amount.addProperty("currency_code", "USD");
        amount.addProperty("value", order.getAmount());
        purchaseUnit.add("amount", amount);

        JsonObject shipping = new JsonObject();
        JsonObject address = new JsonObject();
        address.addProperty("address_line_1", order.getAddress().getAddress());
//        address.addProperty("address_line_2", "Apt 101");
        address.addProperty("admin_area_1", order.getAddress().getCity());
        address.addProperty("admin_area_2", order.getAddress().getDistrict());
        address.addProperty("postal_code", "900000");
        address.addProperty("country_code", "VN");
        shipping.add("address", address);
        purchaseUnit.add("shipping", shipping);

// Tạo mảng purchase_units và thêm purchaseUnit vào đó
        JsonArray purchaseUnitsArray = new JsonArray();
        purchaseUnitsArray.add(purchaseUnit);

// Đưa mảng purchase_units vào requestBody
        requestBody.add("purchase_units", purchaseUnitsArray);

// Tạo một đối tượng payment_source
        JsonObject paymentSource = new JsonObject();
        JsonObject paypal = new JsonObject();
        JsonObject experienceContext = new JsonObject();
        experienceContext.addProperty("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED");
        experienceContext.addProperty("brand_name", "EXAMPLE INC");
        experienceContext.addProperty("locale", "en-US");
        experienceContext.addProperty("landing_page", "LOGIN");
        experienceContext.addProperty("shipping_preference", "SET_PROVIDED_ADDRESS");
        experienceContext.addProperty("user_action", "PAY_NOW");
        experienceContext.addProperty("return_url", "https://example.com/returnUrl");
        experienceContext.addProperty("cancel_url", "https://example.com/cancelUrl");
        paypal.add("experience_context", experienceContext);
        paymentSource.add("paypal", paypal);

// Đưa payment_source vào requestBody
        requestBody.add("payment_source", paymentSource);


        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                paypalConfig.getBaseURL()+"/v2/checkout/orders",
                HttpMethod.POST,
                entity,
                String.class
        );
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED){
            LOGGER.log(Level.INFO, "ORDER CAPTURE");
            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
            String orderId =  jsonResponse.get("id").getAsString();
            order.setPaymentOrderId(orderId);
            orderRepository.save(order);
            JsonArray linksArray = jsonResponse.getAsJsonArray("links");
            System.out.println(jsonResponse);
            String checkoutUrl = null;
            for (JsonElement linkElement : linksArray) {
                JsonObject linkObject = linkElement.getAsJsonObject();
                if (linkObject.has("rel") && linkObject.get("rel").getAsString().equals("payer-action")) {
                    checkoutUrl = linkObject.get("href").getAsString();
                    break;
                }
            }

            if (checkoutUrl != null) {
                System.out.println("Checkout URL: " + checkoutUrl);

                OrderStatus orderStatus = orderStatusRepository.findByStatus(Status.CREATED)
                        .orElseThrow(() -> new RuntimeException("Order status not found"));
                order.setOrderStatus(orderStatus);
                orderRepository.save(order);
                //change order status history
                OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                        .order(order)
                        .orderStatus(orderStatus)
                        .changeAt(new Date())
                        .comments("Order created and waiting for customer action")
                        .build();
//                orderStatusHistoryRepository.save(orderStatusHistory);
                return checkoutUrl;
            } else {
                System.out.println("Checkout URL not found!");
                throw new RuntimeException("Checkout URL not found!");
            }

        } else {
            LOGGER.log(Level.INFO, "FAILED CAPTURING ORDER");
            return "Unavailable to get CAPTURE ORDER, STATUS CODE " + response.getStatusCode();
        }
    }

    @Override
    public void capturePayment(Long orderId)  {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        String accessToken = paypalAccessTokenStore.getAccessToken("access_token");
        if(accessToken == null) {
            System.out.println("token is expired");
            accessToken = generateAccessToken();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                paypalConfig.getBaseURL()+"/v2/checkout/orders/"+order.getPaymentOrderId()+"/authorize",
                HttpMethod.POST,
                entity,
                String.class
        );
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED){
            LOGGER.log(Level.INFO, "ORDER CAPTURED");
            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
            System.out.println(jsonResponse);

            OrderStatus orderStatus = orderStatusRepository.findByStatus(Status.CONFIRMED)
                    .orElseGet(() -> {
                        OrderStatus newOrderStatus = OrderStatus.builder()
                                .status(Status.CONFIRMED)
                                .build();
                        return orderStatusRepository.save(newOrderStatus);
                    });

            order.setOrderStatus(orderStatus); // Associate the Order with the OrderStatus
            orderRepository.save(order); // Save the Order

            //change order status history
            OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                    .order(order)
                    .orderStatus(orderStatus)
                    .changeAt(new Date())
                    .comments("Order captured")
                    .build();
//            orderStatusHistoryRepository.save(orderStatusHistory);

        } else {
            LOGGER.log(Level.INFO, "FAILED CAPTURING ORDER");
            System.out.println("FAILED CAPTURING ORDER");
        }
    }

    public void checkOrder(String orderId) throws IOException {
        String accessToken = paypalAccessTokenStore.getAccessToken("access_token");
        if(accessToken == null) {
            System.out.println("token is expired");
            accessToken = generateAccessToken();
        }
        URL url = new URL("https://api-m.sandbox.paypal.com/v2/checkout/orders/"+orderId);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        System.out.println(response);
    }

    public String generateAccessToken(){
        String auth = getAuth();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setAcceptLanguageAsLocales(Collections.singletonList(Locale.US));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic "+auth);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                paypalConfig.getBaseURL()+"/v1/oauth2/token",
                HttpMethod.POST,
                request,
                String.class
        );
        JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();

        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.log(Level.INFO, "Access token generated successfully");
            String accessToken = jsonResponse.get("access_token").getAsString();
            System.out.println("Access Token: " + accessToken);
                paypalAccessTokenStore.saveAccessToken("access_token",accessToken, jsonResponse.get("expires_in").getAsInt());
            return paypalAccessTokenStore.getAccessToken("access_token");
        } else {
            System.err.println("Failed to get access token. Status code: " + response.getStatusCode());
            return ("Error: " + jsonResponse.get("error"));
        }

    }

}
