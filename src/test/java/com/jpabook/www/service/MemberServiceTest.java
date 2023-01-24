package com.jpabook.www.service;

import com.jpabook.www.domain.Member;
import com.jpabook.www.repository.MemberRepositoryOld;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // 스프링 엮어서 할래
@SpringBootTest
@Transactional // 기본값 롤백임.. 없애줘야ㅕ함
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepositoryOld memberRepository;
    @Autowired
    EntityManager em;


    @Test
//    @Rollback(false) // 이거안쓰면 DB 에 안들어감..
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        em.flush(); // 영속성 저장해줘야함.
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        //when
        Member member2 = new Member();
        member2.setName("kim");

        //when

        memberService.join(member1);
        memberService.join(member2);
        //thena
        fail("예외가 발생해야 한다.");
    }

}