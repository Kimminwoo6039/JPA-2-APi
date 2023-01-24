package com.jpabook.www.repository;

import com.jpabook.www.domain.*;
import com.jpabook.www.domain.Order;
import com.jpabook.www.repository.order.simplequery.SimpleOrderDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static com.jpabook.www.domain.QOrder.order;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o join o.member m" +
                        " where o.status = :status " +
                        " and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setMaxResults(1000) //최대 1000번
                .getResultList();
    }

    // QueryDsl
    public List<Order> findAll1(OrderSearch orderSearch) {
        return query
                .select(order)
                .from(order)
                .join(order.member, QMember.member)
                .where(statusEq((orderSearch.getOrderStatus())), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private static BooleanExpression nameLike(String orderSearch) {
        if (!StringUtils.hasText(orderSearch)) {
            return null;
        }
        return QMember.member.name.like(orderSearch);
    }

    private BooleanExpression statusEq(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        return order.status.eq(orderStatus);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대1000건
        return query.getResultList();
    }

    /**
     * fetch 조인 성능최적하를 위해 100% 로 이해해야함
     */

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    /**
     * 조인 원하는 Dto 만 가져오기 new (값 넣기) -V2
     * 패키지를 따로 만들어서 사용!! v2
     */

    public List<SimpleOrderDto> findOrderDtos() {
        return em.createQuery("select new com.jpabook.www.repository.SimpleOrderDto(o.id,m.name,o.orderDate,o.status,d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", SimpleOrderDto.class)
                .getResultList();
    }

    /**
     * 패치조인
     * 패키지를 따로 만들어서 사용!! - v3
     * <p>
     * distint ==> 중복 제거 하지만 db 상에서 db는 중복이 잘 안됨...
     * JPA 자체적으로 Order 를 가지고올때 같은 order.id값이면 중복을 제거해줍니다.
     * <p>
     * distint ==> 1.db 에 distint 날리고 , JPA가 걸러서 보내준다.
     * 컬렉션 패치조인 하나만 써야한다. 2개쓰면 안됨
     * 경고로그를 담고 메모리에서 페이징 해버린다.
     */

    public List<Order> findAllWithItem() {

        return em.createQuery(
                        "select distinct o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d" +
                                " join fetch o.orderItems oi" +
                                " join fetch oi.item i", Order.class)
//                .setFirstResult(1)  // 페이징처리 안됨
//                .setMaxResults(100) // 페이징처리  안됨
                .getResultList();

    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                        "select o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
