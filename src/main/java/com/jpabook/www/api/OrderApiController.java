package com.jpabook.www.api;


import com.jpabook.www.domain.*;
import com.jpabook.www.repository.OrderRepository;
import com.jpabook.www.repository.order.query.OrderFlatDto;
import com.jpabook.www.repository.order.query.OrderItemQueryDto;
import com.jpabook.www.repository.order.query.OrderQueryDto;
import com.jpabook.www.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * xToOne 일대 다 관계 ( 컬렉션 조회 최적화 ) oneToMany
 * Order
 * Order -> Member
 * Order = > Delvery
 *
 * @ToOne 관계
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 간단한 주문조회 v1 = > 엔티티를 직접 노출
     */


    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems(); //엔티티 강제 초기화 프록시 강제초기화..
            orderItems.stream().map(o -> o.getItem().getName()).collect(toList()); // 람다식
        }
        return all;
    }

    /**
     * 간단한 주문조회 v2 = > 엔티티를 --> dto --> dto 변환
     */

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream().map(o -> new OrderDto(o))  // o 는 Order 값!!!
                .collect(toList());
        return collect;
    }

    /**
     * 간단한 주문조회 v3 = > order 가 2개면 2배 증가 뻥티기가 된다..
     * <p>
     * 페치조인은 많은 엔티티를 결합할때 아주 좋다.
     * 중복 값이 들어온다.
     * 일대 다 하는순간 ** 페이징 불가능 ****
     */

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order = " + order + " id= " + order.getId()); // 참조값이 똑같아서 order 의 개수만큼 중복을 합니다. distint를사용
        }
        List<OrderDto> collect = orders.stream().map(o -> new OrderDto(o))  // o 는 Order 값!!!
                .collect(toList());
        return collect;
    }

    /**
     * 간단한 주문조회 v3-1 = > 엔티티 dto 변환 - 페이징 한계 돌파
     * 컬렉션은 지연로딩으로 조회한다.. - 페치조인 안하고 그냥 놔둔다.
     * 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size , @BatchSize 를 적용한다
     * 1. hibernate.default_batch_fetch_size: 글로벌 설정
     * 2. @BatchSize: 개별 최적화
     * 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.
     * <p>
     * <p>
     * ** 페이징 가능 *** 좋은성능 ㅎ 강추 !!
     */

    @GetMapping("/api/v3-1/orders")
    public List<OrderDto> ordersV3_Page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);  // order랑 member 랑 delivery 패치 조인 Toone 관계는 언제든 패치조인 가능하니

        // OrderItems >> item 2개 있으니 = orderItem =1번 , item 2번 총 3번 한개의 상품에
        // 상품이 총 2개이니 6번의 쿼리가 실행된다.

        List<OrderDto> collect = orders.stream().map(o -> new OrderDto(o))  // o 는 Order 값!!!
                .collect(toList());
        return collect;
    }


    /**
     * Dto 에서 직접 조회
     * <p>
     * 쿼리 3번 나감 N + 1 = 문제생김
     *
     *
     * 장점 : 코드가 단순한다, 특정주문 한건만 조회하면 이 방식을 사용해도 성능이 잘나온다.
     *
     */

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }


    /**
     * Dto 에서 직접 조회
     * <p>
     * 쿼리 2번 나감 1대 1
     *
     *        이걸 사용하자.. 페이징 O
     *        컬렉션 조회 최적화
     *
     *   장점 : 코드가 복잡하다 , 여러 주문을 한꺼번에 조회하는 경우 V4 대신 이것을 최적화한 V5 방식을 사용해야한다.
     *
     */

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * Dto 에서 직접 조회
     * <p>
     * 페이징이 안됨
     * <p>
     * 쿼리 1번으로 최적화 1번
     *
     * 단점
     * 애플리케이션 추가작업이크다
     * 페이징불가능
     * v5 보다 느릴수도 있다 상황에따라
     * 
     * 플랫데이터 최적화
     */

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    @Data
    // No serializer found for class com.jpabook.www.api.OrderApiController$OrderDto and no properties discovered 추가해줘야함.
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
                    .map(orderItem -> new OrderItemDto(orderItem)).collect(toList());
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


/**
 * 엔티티 조회
 * 엔티티를 조회해서 그대로 반환: V1
 * 엔티티 조회 후 DTO로 변환: V2
 * 페치 조인으로 쿼리 수 최적화: V3
 * 컬렉션 페이징과 한계 돌파: V3.1
 * 컬렉션은 페치 조인시 페이징이 불가능
 * ToOne 관계는 페치 조인으로 쿼리 수 최적화
 * 컬렉션은 페치 조인 대신에 지연 로딩을 유지하고, hibernate.default_batch_fetch_size ,
 * @BatchSize 로 최적화
 * DTO 직접 조회
 * JPA에서 DTO를 직접 조회: V4
 * 컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 IN 절을 활용해서 메모리에 미리 조회해서 최적화: V5
 * 플랫 데이터 최적화 - JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환: V6
 * 권장 순서
 * 1. 엔티티 조회 방식으로 우선 접근
 * 1. 페치조인으로 쿼리 수를 최적화
 * 2. 컬렉션 최적화
 * 1. 페이징 필요 hibernate.default_batch_fetch_size , @BatchSize 로 최적화
 * 2. 페이징 필요X 페치 조인 사용
 * 2. 엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용
 * 3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate
 * > 참고: 엔티티 조회 방식은 페치 조인이나, hibernate.default_batch_fetch_size , @BatchSize 같이
 * 코드를 거의 수정하지 않고, 옵션만 약간 변경해서, 다양한 성능 최적화를 시도할 수 있다. 반면에 DTO를
 * 직접 조회하는 방식은 성능을 최적화 하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다.
 * > 참고: 개발자는 성능 최적화와 코드 복잡도 사이에서 줄타기를 해야 한다. 항상 그런 것은 아니지만, 보통
 * 성능 최적화는 단순한 코드를 복잡한 코드로 몰고간다.
 * > 엔티티 조회 방식은 JPA가 많은 부분을 최적화 해주기 때문에, 단순한 코드를 유지하면서, 성능을 최적화 할
 * 수 있다.
 * > 반면에 DTO 조회 방식은 SQL을 직접 다루는 것과 유사하기 때문에, 둘 사이에 줄타기를 해야 한다.
 * DTO 조회 방식의 선택지
 * DTO로 조회하는 방법도 각각 장단이 있다. V4, V5, V6에서 단순하게 쿼리가 1번 실행된다고 V6이 항상
 * 좋은 방법인 것은 아니다.
 * V4는 코드가 단순하다. 특정 주문 한건만 조회하면 이 방식을 사용해도 성능이 잘 나온다. 예를 들어서
 * 조회한 Order 데이터가 1건이면 OrderItem을 찾기 위한 쿼리도 1번만 실행하면 된다.
 * V5는 코드가 복잡하다. 여러 주문을 한꺼번에 조회하는 경우에는 V4 대신에 이것을 최적화한 V5 방식을
 * 사용해야 한다. 예를 들어서 조회한 Order 데이터가 1000건인데, V4 방식을 그대로 사용하면, 쿼리가 총
 * 1 + 1000번 실행된다. 여기서 1은 Order 를 조회한 쿼리고, 1000은 조회된 Order의 row 수다. V5
 * 방식으로 최적화 하면 쿼리가 총 1 + 1번만 실행된다. 상황에 따라 다르겠지만 운영 환경에서 100배
 * 이상의 성능 차이가 날 수 있다.
 * V6는 완전히 다른 접근방식이다. 쿼리 한번으로 최적화 되어서 상당히 좋아보이지만, Order를 기준으로
 * 페이징이 불가능하다. 실무에서는 이정도 데이터면 수백이나, 수천건 단위로 페이징 처리가 꼭 필요하므로,
 * 이 경우 선택하기 어려운 방법이다. 그리고 데이터가 많으면 중복 전송이 증가해서 V5와 비교해서 성능
 * 차이도 미비하다.
 * */