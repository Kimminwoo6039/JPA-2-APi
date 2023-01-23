package com.jpabook.www.api;

import com.jpabook.www.domain.Address;
import com.jpabook.www.domain.Order;
import com.jpabook.www.domain.OrderSearch;
import com.jpabook.www.domain.OrderStatus;
import com.jpabook.www.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne ( ManyToOne,OneToOne )
 * Order
 * Order -> Member
 * Order = > Delvery
 * @ToOne 관계
 * */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    
    /**
     * 간단한 주문조회 v1 = > 엔티티를 직접 노출
     * */

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화 되서 db 에서 JPA 가 데이터 끌고옴
            order.getDelivery().getOrder(); // Lazy 강제 초기화 되서 db 에서 JPA 가 데이터 끌고옴
        }
        return all;
    }

    /**
     * 간단한 주문조회 v2 = > 엔티티를 DTO 로 변환
     * */

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {

        //ORDER 2개
        // N+1 -> 1+N
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        //2개면 2번돌음
        List<SimpleOrderDto> result = orders.stream().
                map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 간단한 주문조회 v3 = > 패치조인으로 성능 UP  ** Repository 에서 sql 작성
     * 3번이 중요함
     * */

    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
    List<Order> order = orderRepository.findAllWithMemberDelivery();
    List<SimpleOrderDto> result = order.stream()
            .map(o-> new SimpleOrderDto(o))
            .collect(Collectors.toList());

    return new Result(3,result);

    }

    /**
     * 간단한 주문조회 v4 = > dto 로 조회
     * 3번이랑 4번이랑 필드차이일뿐 성능 부분.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<com.jpabook.www.repository.order.simplequery.SimpleOrderDto> ordersV4() {
        return orderRepository.findOrderDtos();
    }


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // Lazy 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // Lazy 초기화
        }
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        //        private int count;  Json 한개 추가하고싶은데 Result 클래스에 추가
        private T data;
    }
}
