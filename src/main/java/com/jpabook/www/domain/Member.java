package com.jpabook.www.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty(message = "이름을 빈값입니다.")
    private String name;

    @Embedded // 내장타입을 포함했다.
    private Address address;

    @JsonIgnore // JSON 형태에서 안보이게 만들어줌
    @OneToMany(mappedBy = "member") // order 테이블에 member 필드에 됨
    private List<Order> orders = new ArrayList<>();
}
