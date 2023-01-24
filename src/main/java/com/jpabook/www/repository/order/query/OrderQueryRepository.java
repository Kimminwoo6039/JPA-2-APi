package com.jpabook.www.repository.order.query;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 엔티티를 제외한 비슷한 쿼리를 찾을때 사용함
 */


@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * orderItems 는 일대다 이기때문에 데이터가 뻥튀기대서 일단빼놓는다.
     * <p>
     * 최적화 하기전  1-1
     * <p>
     * 루프를 도는게 단점. v-4
     */

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // query 1번 - > N 번

        result.forEach(o ->
                {
                    List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // Query N+1 번 2개
                    o.setOrderItems(orderItems); // order 안에 ordeItems 에 값넣기
                }
        );
        return result;
    }


    /**
     * 컬렉션 최적화 1-2
     * <p>
     * 한방에 가져옴 루프 안들고
     *
     * 제일 중요함... 컬렉션 조회 최적화 v5
     */

    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();  // order 주문을 다 가져왓다.

        List<Long> orderIds = result.stream().map(o -> o.getOrderId()) // 스트림을써서 OrderQueryDto -> orderId 의 List 가된다. 주문번호 2개니깐 2개있음
                .collect(Collectors.toList());

        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new com.jpabook.www.repository.order.query.OrderItemQueryDto(oi.order.id,i.name,oi.orderPrice,oi.count)" +
                                "from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class) // orderIds 파라미터로 in 절로 넣어버림.
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream().collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));// Order map 형식으로

        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId()))); // 맵을 세팅

        return result;
    }


    /**
     * ToMany 관계는 최적하기 하기 어려워서 findOrdrItems() 메소드로 별도로 조회해서 성능을 높인다.
     * 메소드용
     */

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select new com.jpabook.www.repository.order.query.OrderItemQueryDto(oi.order.id,i.name,oi.orderPrice,oi.count)" +
                        "from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new com.jpabook.www.repository.order.query.OrderQueryDto(o.id,m.name,o.orderDate,o.status,d.address)"
                        + " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }
    
    /**
     * 한방 쿼리 모든DTO 를 몰아넣기
     * 페이징이 힘듬
     * */


    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new com.jpabook.www.repository.order.query.OrderFlatDto(o.id,m.name,o.orderDate,o.status,d.address,i.name,oi.orderPrice,oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i",OrderFlatDto.class)
                .getResultList();
        
    }
}
