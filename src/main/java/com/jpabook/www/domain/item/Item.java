package com.jpabook.www.domain.item;

import com.jpabook.www.domain.Category;
import com.jpabook.www.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 한테이블에 때려박아서 돌림 앨범 무비
@DiscriminatorColumn(name = "dtype") // dtpe 이란걸 사용함
@Getter @Setter
// Setter 를 사용하지않고 메소드를 사용해..작성
public abstract class Item { // 추상클래스

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items",fetch = FetchType.LAZY)
    private List<Category> categories = new ArrayList<>();

    //== 비즈니스 로직== //

    /**
     * stock 증가 (재고 증가)
     */
    public void addStock(int quantity) {
        this.stockQuantity +=quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int resetStock = this.stockQuantity - quantity;
        if (resetStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = resetStock;
    }
}
