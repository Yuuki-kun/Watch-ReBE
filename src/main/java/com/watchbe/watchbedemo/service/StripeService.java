package com.watchbe.watchbedemo.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.watchbe.watchbedemo.model.*;
import com.watchbe.watchbedemo.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
//public class StripeService implements StripeCheckoutService{
//    private final OrderDetailsRepository orderDetailsRepository;
//    private final CustomerRepository customerRepository;
//    private final OrderRepository orderRepository;
//    private final OrderStatusRepository orderStatusRepository;
//    private final PaymentRepository paymentRepository;
public class StripeService extends CheckoutService{

    public StripeService(OrderRepository orderRepository, OrderDetailsRepository orderDetailsRepository,
                         CustomerRepository customerRepository, OrderStatusRepository orderStatusRepository,
                         PaymentRepository paymentRepository, WatchRepository watchRepository, ShippingRepository shippingRateRepository
){
        super(orderRepository, orderDetailsRepository, customerRepository, orderStatusRepository, paymentRepository,
                watchRepository,shippingRateRepository);
    }

//    @Override
//    public Order createOrder(CreateCheckoutRequest checkOutRequest) {
//        if (checkOutRequest.getOrderDetailsId() == null || checkOutRequest.getOrderDetailsId().isEmpty()) {
//            throw new IllegalArgumentException("order items cannot be empty");
//        }
//        if (checkOutRequest.getCustomerId() == 0) {
//            throw new IllegalArgumentException("Customer id cannot be empty");
//        }
//
//        List<OrderDetails> orderDetailsList =
//                orderDetailsRepository.findAllById(checkOutRequest.getOrderDetailsId());
//
//        double totalAmount =  orderDetailsList.stream()
//                .mapToDouble(orderDetails -> orderDetails.getPrice() * orderDetails.getQuantity())
//                .sum();
//
//        Customer customer =
//                customerRepository.findById(checkOutRequest.getCustomerId()).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
//
//
//
//        Payment payment = Payment.builder()
//                .date(new Date())
//                .type("Stripe")
//                .build();
//        paymentRepository.save(payment);
//
//        ShippingAddress shippingAddress =
//                customer.getShippingAddresses().stream().filter(ShippingAddress::getIsDefault).findFirst().get();
//
//        Order order =
//                Order.builder().customer(customer).payment(payment).address(shippingAddress).orderDate(new Date()).tax(100f).amount(totalAmount).shipping(100f).build();
//        order.setOrderDetails(orderDetailsList);
//
//        //find in customer.getShippingAddresses if shipping address is default address then set it to order
//        //if not then set the first address to order
////        order.setAddress(customer.getShippingAddresses().stream().filter(ShippingAddress::getIsDefault).findFirst().get());
//        orderRepository.save(order);
//        return order;
//    }

    @Override
    protected String createPaymentIntent(Order order) throws StripeException {

        List<SessionCreateParams.LineItem> lineItems  =
                order.getOrderDetails().stream().map(
                        this::createLineItem
                ).collect(Collectors.toList());


        SessionCreateParams params = new SessionCreateParams.Builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/user-info/user-orders")
                .setCancelUrl("https://localhost:3000/checkout/cancel")
                .setCustomerEmail(order.getCustomer().getEmail())
//                .setShippingAddressCollection(SessionCreateParams.ShippingAddressCollection.builder()
//                        .addAllAllowedCountry(
//                        new ArrayList<>(
//                                Arrays.asList(SessionCreateParams.ShippingAddressCollection.AllowedCountry.US,
//                                        SessionCreateParams.ShippingAddressCollection.AllowedCountry.VN)
//                        ))
//                        .build())

                .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                        .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.MANUAL)
                        .setShipping(
                                SessionCreateParams.PaymentIntentData.Shipping.builder()
                                        .setName(order.getCustomer().getFirstName()+" "+order.getCustomer().getLastName())
                                        .setAddress(
                                                SessionCreateParams.PaymentIntentData.Shipping.Address.builder()
                                                        .setCountry("VN")
                                                        .setCity(order.getAddress().getCity())
                                                        .setLine1(order.getAddress().getAddress() + ", Phường " + order.getAddress().getWard())
                                                        .setPostalCode("900000")
                                                        .setState(order.getAddress().getDistrict())
                                                        .build()
                                        )
                                        .build()
                        )
                        .build())
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);
        OrderStatus orderStatus = orderStatusRepository.findByStatus(Status.CREATED)
                .orElseThrow(() -> new RuntimeException("Order status not found"));
        order.setOrderStatus(orderStatus);
        order.setPaymentOrderId(session.getId());
        orderRepository.save(order);

        //change the order status history
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                .order(order)
                .orderStatus(orderStatus)
                .changeAt(new Date())
                .comments("Order created and waiting for customer action.")
                .build();
//        orderStatusHistoryRepository.save(orderStatusHistory);

        return session.getUrl();
    }

    @Override
    public void capturePayment(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        String stripePaymentIntentId = order.getPaymentIntentId();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(stripePaymentIntentId);
        paymentIntent.capture();

        // Retrieve the existing OrderStatus or create a new one if it doesn't exist
        OrderStatus orderStatus = orderStatusRepository.findByStatus(Status.CONFIRMED)
                .orElseGet(() -> {
                    OrderStatus newOrderStatus = OrderStatus.builder()
                            .status(Status.CONFIRMED)
                            .build();
                    return orderStatusRepository.save(newOrderStatus);
                });
        order.setOrderStatus(orderStatus); // Associate the Order with the OrderStatus
        orderRepository.save(order); // Save the Order
        order.getOrderDetails().forEach(or->{
            Watch w = or.getWatch();
            w.setInventoryQuantity(w.getInventoryQuantity()-or.getQuantity());
            w.setSoldQuantity(w.getSoldQuantity()+or.getQuantity());
            watchRepository.save(w);
        });
            OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                .order(order)
                .orderStatus(orderStatus)
                .changeAt(new Date())
                .comments("Order captured. Payment successful.")
                .build();
//        orderStatusHistoryRepository.save(orderStatusHistory);

    }


    public SessionCreateParams.LineItem createLineItem(OrderDetails orderDetails) {
//        return SessionCreateParams.LineItem.builder()
//                .setQuantity(orderDetails.getQuantity())
//
//                .setPriceData(
//                        SessionCreateParams.LineItem.PriceData.builder()
//                                .setCurrency("vnd")
//                                .setUnitAmountDecimal(BigDecimal.valueOf((orderDetails.getPrice() * 1000))) //
//                                // assuming the amount isin cents
//                                .setProductData(
//                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
//
//                                                .setName(orderDetails.getWatch().getName())
//                                                .build()
//                                )
//
//                                .build()
//                )
//
//                .build();
        //put a image url to the line item
        double priceInVND = orderDetails.getPrice() * 1000; // Convert to VND

// Round the price to two decimal places
        BigDecimal unitAmount = BigDecimal.valueOf(Math.round(priceInVND * 100) / 100.0);
        return SessionCreateParams.LineItem.builder()

                .setQuantity(orderDetails.getQuantity())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("vnd")
                                .setUnitAmountDecimal(unitAmount) //
                                // assuming the amount is in cents
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(orderDetails.getWatch().getName())
//                                                .addImage(orderDetails.getWatch().getImages().get(0).getImage())

                                                .build()
                                )
                                .build()
                )
                //put a image url to the line item
                .build();
    }
}
