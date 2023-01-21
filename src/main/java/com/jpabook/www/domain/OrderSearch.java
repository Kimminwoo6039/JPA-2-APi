package com.jpabook.www.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearch {
    
    private String memberName; // 회원 이름
    private OrderStatus orderStatus; // 주문생태 [ ORDER,CANCEL]
}
