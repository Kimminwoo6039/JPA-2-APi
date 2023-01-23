package com.jpabook.www.api;
import com.jpabook.www.domain.Member;
import com.jpabook.www.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne ( ManyToOne,OneToOne )
 * Order
 * Order -> Member
 * Order = > Delvery
 * @ToOne 관계
 * */
@RestController // controller responsbody 합친것
@RequiredArgsConstructor
@Slf4j
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 회원 조회 API
     * */

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                // .map(m -> new MemberDto(m.getName(),m.getId()))
                .collect(Collectors.toList());



        return new Result(collect);

//        return new Result(3,collect);  Json 한개 추가하고싶은데 Result 클래스에 추가
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
//        private int count;  Json 한개 추가하고싶은데 Result 클래스에 추가
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
//        private Long id; 추가로 값을 가져올수 있게함. JSON
    }


    /**
     * 회원 등록 API
     * */
    
    

    /**
     * Api 할때 Entity 노출 시키는 예 
     * */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * dto 클래스를 만들어서 Entitiy 노출 안되게함 V2 방법을 사용해야함
     * */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);

    }

    /**
     * data 임
     * */
    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class CreateMemberRequest {

        @NotEmpty
        private String name;

    }

    /**
     * 회원 수정 API
     * PUT
     * */

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id
            ,@RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id,request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getName(),findMember.getId());
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private String name;
        private Long id;

    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }








}
