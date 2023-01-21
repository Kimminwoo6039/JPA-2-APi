package com.jpabook.www.repository;

import com.jpabook.www.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor // @Persistense 어노세티션 안붙혀도 지원하게함
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) { // 아이템은 처음에 데이터가 없음
       if (item.getId() == null) {
           em.persist(item);
       } else {
           em.merge(item);  // update 비슷한거임
       }
    }
    
    public Item findOne(Long id) { // id 값으로 한개의 아이템을 찾음
        return em.find(Item.class,id);
    }
    
    public List<Item> findAll() { // 전체아이템 조회
        return em.createQuery("select i from Item i",Item.class).getResultList();
    }
}
