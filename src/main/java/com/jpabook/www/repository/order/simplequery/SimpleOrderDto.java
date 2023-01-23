package com.jpabook.www.repository.order.simplequery;

import com.jpabook.www.domain.Address;
import com.jpabook.www.domain.Order;
import com.jpabook.www.domain.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SimpleOrderDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public SimpleOrderDto(Long orderId,String name,LocalDateTime orderDate , OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name; // Lazy 초기화
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}

