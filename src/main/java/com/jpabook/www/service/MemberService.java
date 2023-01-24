package com.jpabook.www.service;

import com.jpabook.www.domain.Member;
import com.jpabook.www.repository.MemberRepository;
import com.jpabook.www.repository.MemberRepositoryOld;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //// readonly 사용하면 DB 성능 UP 읽기용 모드
@RequiredArgsConstructor // 이것을 사용하면 final 생성자를 이용해서 기본생성자 생성해줌. 필요한것은 필드 만들어서 사용
public class MemberService {


    private final MemberRepository memberRepository; // 필드 생성자 비추천

//    @Autowired // 생성자 인젝션 ( 많이 추천 ) 어노테이션 삭제가능 // 생성자 한개면 자동주입
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    /**
    * 회원가입
    * */
    @Transactional // 등록하는 부분에서 readOnly = true 하면 값이 안바뀜
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복회원검증
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 회원 중복 검증
     * */
     public void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName()); // DB 에 name 값을 유니크제약조건을 잡아줘야함
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     * */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 한명 조회
     * */
    @Transactional(readOnly = true)
    public Member findOne(Long id) {
        return memberRepository.findById(id).get();
    }

    /**
     * 회원 수정 API
     */

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findById(id).get();
        member.setName(name);
    }
}
