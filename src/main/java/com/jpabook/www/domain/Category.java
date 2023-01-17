package com.jpabook.www.domain;

import com.jpabook.www.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")  // 자기자신 테이블에서 매핑해줌
    private Category parent;

    @OneToMany(mappedBy = "parent") // 자기자신 테이블에서 매핑해줌
    private List<Category> child = new ArrayList<>();


    //== 연관관계 메서드 ==//

    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}
