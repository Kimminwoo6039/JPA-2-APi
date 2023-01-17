package com.jpabook.www.domain.item;

import com.jpabook.www.domain.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 한테이블에 때려박아서 돌림 앨범 무비
@DiscriminatorColumn(name = "dtype") // dtpe 이란걸 사용함
@Getter @Setter
public abstract class Item { // 추상클래스

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items",fetch = FetchType.LAZY)
    private List<Category> categories = new ArrayList<>();
}
