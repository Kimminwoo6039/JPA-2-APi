package com.jpabook.www.api;


import com.jpabook.www.domain.*;
import com.jpabook.www.repository.OrderRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne 일대 다 관계 ( 컬렉션 조회 최적화 )
 * Order
 * Order -> Member
 * Order = > Delvery
 * @ToOne 관계
 * */

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * 간단한 주문조회 v1 = > 엔티티를 직접 노출
     * */


    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems(); //엔티티 강제 초기화 프록시 강제초기화..
            orderItems.stream().map(o -> o.getItem().getName()).collect(Collectors.toList()); // 람다식
        }
        return all;
    }

    /**
     * 간단한 주문조회 v2 = > 엔티티를 --> dto --> dto 변환
     * */

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream().map(o -> new OrderDto(o))  // o 는 Order 값!!!
                .collect(Collectors.toList());
        return collect;
    }

    @Data // No serializer found for class com.jpabook.www.api.OrderApiController$OrderDto and no properties discovered 추가해줘야함.
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // dto 이면 엔티티를 절대 쓰면 안도니 엔티티도 dto 로 만들어줘야ㅕ함

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream() // 엔티티 dto 로 전환
                    .map(orderItem -> new OrderItemDto(orderItem)).collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName; // 상품명
        private int orderPrice; // 주문가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
