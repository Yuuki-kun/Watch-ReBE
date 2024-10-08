package com.watchbe.watchbedemo.controller;

import com.watchbe.watchbedemo.dto.CartItem;
import com.watchbe.watchbedemo.dto.OrderDetailsDto;
import com.watchbe.watchbedemo.exception.NotFoundException;
import com.watchbe.watchbedemo.mapper.OrderDetailsMapperImpl;
import com.watchbe.watchbedemo.model.Cart;
import com.watchbe.watchbedemo.model.OrderDetails;
import com.watchbe.watchbedemo.model.Promotion_Details;
import com.watchbe.watchbedemo.model.Watch;
import com.watchbe.watchbedemo.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/user-cart")
@RequiredArgsConstructor
public class CartController {
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderDetailsMapperImpl orderDetailsMapper;
    private final ProductPromotionRepository promotionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping
    public ResponseEntity<List<OrderDetailsDto>> getAllCart(@RequestParam("cart") Long cartId){
        List<OrderDetailsDto> orderDetailsDto =
                cartRepository.findById(cartId).orElseThrow().getOrderDetails().stream().map(
                        orderDetailsMapper::mapTo
                ).collect(Collectors.toList());

        for (OrderDetailsDto o:
             orderDetailsDto) {

            List<Promotion_Details> promotionDetails =
                    promotionRepository
                            .findPromotionDiscountPercentageByProductIdAndOrderDate(o.getWatch().getId(),new java.util.Date());
            float promotion = 0f;
            for(Promotion_Details p: promotionDetails){
                if(p.getPromotion().getPriority()==1){
                    promotion += p.getValue();
                }
            }
            if(promotionDetails!=null){
                o.getWatch().setDiscount(promotion);
            }
            else{
                o.getWatch().setDiscount(0f);
            }
        }

        return ResponseEntity.ok(orderDetailsDto);
    }


    /**@apiNote
     *
     * @param cartItem product wanna add to cart
     * @return OrderDetailsDto
     */
    @PostMapping("/add-to-cart")
    public ResponseEntity<OrderDetailsDto> addToCart(@RequestBody CartItem cartItem){
        System.out.println("cartItem="+cartItem);
        Cart cart = cartRepository.findById((long) cartItem.getCartId()).orElseThrow(()->new NotFoundException("cart " +
                "not found, " +
                "id="+cartItem.getCartId()));
        Watch watch = entityManager.getReference(Watch.class, cartItem.getWatchId());

        List<OrderDetails> orderDetails = cart.getOrderDetails();

        for (OrderDetails o:
             orderDetails) {
            if(o.getWatch().getId().equals((long) cartItem.getWatchId())){
                o.setQuantity(o.getQuantity()+1);
                cartRepository.save(cart);
                return ResponseEntity.ok(orderDetailsMapper.mapTo(o));
            }
        }

        float discount = 0f;

        List<Promotion_Details> promotionDetails =
                promotionRepository
                        .findPromotionDiscountPercentageByProductIdAndOrderDate(cartItem.getWatchId(),new java.util.Date());
        float promotion = 0f;
        for(Promotion_Details p: promotionDetails){
            if(p.getPromotion().getPriority()==1){
                promotion += p.getValue();
            }
        }

        OrderDetails orderDetail = OrderDetails.builder()
                .price(cartItem.getPrice()*(1-promotion/100)*cartItem.getQuantity())
                .quantity(cartItem.getQuantity())
                .watch(watch)
                .build();
        cart.addOrderDetails(orderDetail);
        cartRepository.save(cart);
        return ResponseEntity.ok(orderDetailsMapper.mapTo(orderDetail));
    }

    @GetMapping("/tempo-cart")
    public ResponseEntity<String> getTempoCart(){
        Cart cart = Cart.builder().build();
        cartRepository.save(cart);
        return ResponseEntity.ok(String.valueOf(cart.getId()));
    }

    /**
     * http..//delete-items?cart=1&item=1
     * @param
     *
     * @return
     */
    @DeleteMapping("/delete-items")
    public ResponseEntity<List<OrderDetailsDto>> removeFromCart(@RequestParam("cart") String cartId,
                                                                @RequestParam("item") String orderDetailsId){
        Cart cart = cartRepository.findById(Long.valueOf(cartId)).orElseThrow(()-> new NotFoundException("cart " +
                "not found, " +
                "id="+cartId));
        List<OrderDetails> orderDetails = cart.getOrderDetails();
//        cart.setOrderDetails(new ArrayList<>());
        for (OrderDetails o:
             orderDetails) {
            if(o.getId().equals(Long.valueOf(orderDetailsId))){
                System.out.println("o="+o.getId()+"id="+orderDetailsId);
                cart.removeOrderDetails(o);
//                orderDetailsRepository.delete(o);
                break;
            }
        }
        cartRepository.save(cart);
        List<OrderDetailsDto> orderDetailsDto =
                cart.getOrderDetails().stream().map(
                        orderDetailsMapper::mapTo
                ).collect(Collectors.toList());
        return ResponseEntity.ok(orderDetailsDto);
    }

    @PutMapping("/update-quantity")
    public ResponseEntity<List<OrderDetailsDto>> updateItemQuantity(@RequestParam("cart") String cartId,
                                                                    @RequestParam("item") String orderDetailsId,
                                                                    @RequestParam("method") String method){

        Cart cart = cartRepository.findById(Long.valueOf(cartId)).orElseThrow(()-> new NotFoundException("cart " +
                "not found, " +
                "id="+cartId));
        List<OrderDetails> orderDetails = cart.getOrderDetails();

        for (OrderDetails o:
                orderDetails) {
            if(o.getId().equals(Long.valueOf(orderDetailsId))){
                if(method.equals("increase")){
                    System.out.println("o="+o.getId()+"id="+orderDetailsId);
                    o.setQuantity(o.getQuantity()+1);
                }else if(method.equals("decrease")){
                    if(o.getQuantity()==1){
                        cart.removeOrderDetails(o);
                    }else{
                        o.setQuantity(o.getQuantity()-1);
                    }
                }
                break;
            }
        }
        cartRepository.save(cart);
        List<OrderDetailsDto> orderDetailsDto =
                cart.getOrderDetails().stream().map(
                        orderDetailsMapper::mapTo
                ).collect(Collectors.toList());
        return ResponseEntity.ok(orderDetailsDto);
    }

    @PostMapping("/checkout-items")
    public ResponseEntity<List<OrderDetailsDto>> getAsList(@RequestBody List<CartItem> cartItems){
        System.out.println("cartItems="+cartItems);
        List<OrderDetailsDto>  orderDetailsDtos = new ArrayList<>();
        cartItems.forEach(cartItem -> {
           OrderDetails orderDetail =
                   orderDetailsRepository.findById(cartItem.getCartId()).orElseThrow(()-> new NotFoundException("order " +
                           "details not found, " +
                           "id="+cartItem.getCartId()));
        OrderDetailsDto orderDetailsDto = orderDetailsMapper.mapTo(orderDetail);
        orderDetailsDto.getWatch().setDials(null);
        orderDetailsDtos.add(orderDetailsDto);
        });

        return ResponseEntity.ok(orderDetailsDtos);
    }
}
