package com.jpabook.www.repository;
import com.jpabook.www.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList(); // JPA 쿼리실행문 엔티티객체 조회함,sql 테이블1
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name= :name ", Member.class).setParameter("name",name).getResultList();
    }


}
