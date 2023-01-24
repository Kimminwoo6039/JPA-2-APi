package com.jpabook.www.repository;


import com.jpabook.www.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> { // 엔티티,pk 값 타입

    // select m from m where m.name = ? 자동으로 쿼리를 진행
    List<Member> findByName(String name);
}
