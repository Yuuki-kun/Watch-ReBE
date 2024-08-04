package com.watchbe.watchbedemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.OrderRepository;
import com.watchbe.watchbedemo.repository.OrderStatusRepository;
import com.watchbe.watchbedemo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
public class Webhook {
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
//    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    @Value("${stripe.whsec}")
    private  String endpointSecret;

    @PostMapping("/stripe")
    public void handleWebhook (@RequestBody String payload, @RequestHeader("Stripe-Signature" ) String sigHeader) throws Exception {
        System.out.println("payload = "+payload);
        Event event;
        try {
            event = com.stripe.net.Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            // Xử lý khi xác thực không thành công
            System.out.println(e);
            return;
        }
        switch (event.getType()) {
            case "payment_intent.created":
                // Xử lý khi có sự kiện payment_intent.created
//                String payloadJson = payload;
//                JsonObject jsonObject = JsonParser.parseString(payloadJson).getAsJsonObject();
//                JsonObject dataobject = jsonObject.getAsJsonObject("data");
//                String csid = dataobject.getAsJsonObject("object").get("id").getAsString();

                break;
                case "charge.succeeded":
//                    String payloadJsons = payload;
//                    JsonObject jsonObjects = JsonParser.parseString(payloadJsons).getAsJsonObject();
//                    JsonObject dataobjects = jsonObjects.getAsJsonObject("data");
//                    String chid = dataobjects.getAsJsonObject("object").get("id").getAsString();
//                    System.out.println("chid = "+chid);
//                    Charge charge = Charge.retrieve(chid);
////                    Long code = charge.();
//                    String brand = charge.getPaymentMethodDetails().getCard().getBrand();
//                    String cvcCheck = charge.getPaymentMethodDetails().getCard().getChecks().getCvcCheck();
//                    String country = charge.getPaymentMethodDetails().getCard().getCountry();
//                    String last4 = charge.getPaymentMethodDetails().getCard().getLast4();
//                    String network = charge.getPaymentMethodDetails().getCard().getNetwork();
//                    String paymentMethod = charge.getPaymentMethod();
//                    String paymentMethodType = charge.getPaymentMethodDetails().getType();
//                    String receiptEmail = charge.getBillingDetails().getEmail();
//                    Long expMonth = charge.getPaymentMethodDetails().getCard().getExpMonth();
//                    Long expYear = charge.getPaymentMethodDetails().getCard().getExpYear();
//                    String fingerprint = charge.getPaymentMethodDetails().getCard().getFingerprint();
//                    String receiptUrl = charge.getReceiptUrl();
//                    String funding = charge.getPaymentMethodDetails().getCard().getFunding();
//                    Date captureBefore = new Date(charge.getPaymentMethodDetails().getCard().getCaptureBefore()*1000);
//
//                    //log alls
//                    System.out.println("brand = "+brand);
//                    System.out.println("cvcCheck = "+cvcCheck);
//                    System.out.println("country = "+country);
//                    System.out.println("last4 = "+last4);
//                    System.out.println("network = "+network);
//                    System.out.println("paymentMethod = "+paymentMethod);
//                    System.out.println("paymentMethodType = "+paymentMethodType);
//                    System.out.println("receiptEmail = "+receiptEmail);
//                    System.out.println("expMonth = "+expMonth);
//                    System.out.println("expYear = "+expYear);
//                    System.out.println("fingerprint = "+fingerprint);
//                    System.out.println("receiptUrl = "+receiptUrl);
//                    System.out.println("funding = "+funding);
//                    System.out.println("captureBefore = "+captureBefore);

                    break;
            case "checkout.session.completed":
                // Xử lý khi có sự kiện checkout.session.completed
                String payloadJson = payload;
                JsonObject jsonObject = JsonParser.parseString(payloadJson).getAsJsonObject();
                JsonObject dataobject = jsonObject.getAsJsonObject("data");
                String csid = dataobject.getAsJsonObject("object").get("id").getAsString();
                System.out.println("csid = "+csid);
                Session s = Session.retrieve(csid);
                Order order = orderRepository.findByPaymentOrderId(csid);
                OrderStatus orderStatus =
                        orderStatusRepository.findByStatus(Status.PENDING).orElseThrow(() -> new IllegalArgumentException("Order status not found"));

                order.setOrderStatus(orderStatus);
                Session session = Session.retrieve(csid);
                order.setPaymentIntentId(session.getPaymentIntent());

                PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
                PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
                Charge charge = Charge.retrieve(paymentIntent.getLatestCharge());

                String brand = paymentMethod.getCard().getBrand();
                String cvcCheck = paymentMethod.getCard().getChecks().getCvcCheck();
                String country = paymentMethod.getCard().getCountry();
                String last4 = paymentMethod.getCard().getLast4();
                String network = paymentMethod.getCard().getNetworks().getAvailable().get(0);
                String paymentMethodType = paymentMethod.getType();
                String receiptEmail = paymentMethod.getBillingDetails().getEmail();
                Long expMonth = paymentMethod.getCard().getExpMonth();
                Long expYear = paymentMethod.getCard().getExpYear();
                String fingerprint = paymentMethod.getCard().getFingerprint();
                String funding = paymentMethod.getCard().getFunding();
                Date captureBefore = new Date(charge.getPaymentMethodDetails().getCard().getCaptureBefore()*1000);
                String receiptUrl = charge.getReceiptUrl();

                //log alls
                System.out.println("brand = "+brand);
                System.out.println("cvcCheck = "+cvcCheck);
                System.out.println("country = "+country);
                System.out.println("last4 = "+last4);
                System.out.println("network = "+network);
                System.out.println("paymentMethod = "+paymentMethod);
                System.out.println("paymentMethodType = "+paymentMethodType);
                System.out.println("receiptEmail = "+receiptEmail);
                System.out.println("expMonth = "+expMonth);
                System.out.println("expYear = "+expYear);
                System.out.println("fingerprint = "+fingerprint);
                System.out.println("receiptUrl = "+receiptUrl);
                System.out.println("funding = "+funding);
                System.out.println("captureBefore = "+captureBefore);

                Payment payment = order.getPayment();
                payment.setBrand(brand);
                payment.setCvcCheck(cvcCheck);
                payment.setCountry(country);
                payment.setLast4(last4);
                payment.setNetwork(network);
                payment.setPaymentMethod(paymentMethodType);
                payment.setReceiptEmail(receiptEmail);
                payment.setExpMonth(expMonth);
                payment.setExpYear(expYear);
                payment.setFingerprint(fingerprint);
                payment.setFunding(funding);
                payment.setCaptureBefore(captureBefore);

                order.setReceiptUrl(receiptUrl);
                paymentRepository.save(payment);
                orderRepository.save(order);

                //change order status history
                OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                        .order(order)
                        .orderStatus(orderStatus)
                        .changeAt(new Date())
                        .comments("Payment authorised and waiting for capture")
                        .build();


//                orderStatusHistoryRepository.save(orderStatusHistory);
                break;
        }

    }
}
