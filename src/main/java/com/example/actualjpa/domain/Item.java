package com.example.actualjpa.domain;

import com.example.actualjpa.exception.NotEnoughStockException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "item")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 한 곳에 다 때려박을 것이고, DTYPE 이 지정될 것임
@DiscriminatorColumn(name = "d_type")
public abstract class Item { // 함유 데이터가 있을 것이기 때문

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();


    // MEMO:: 왜 이게 객체지향적으로 맞을까 생각해보면 좋을듯?
    // MEMO:: 왜 이게 Setter 와 어떤 차이가 있는걸까?

    // 두가지 비즈니스를 넣을 것임. 재고 늘고 줄고
    // 객체 지향적으로 생각을 해보면, 밖에서 일을 수행하고 안에 set 을 할게 아니라
    // data 를 가지고 있는 쪽에 Business 로직이 있는 것이 나음.
    // 여기 있는게 제일 맞음. 나의 것을 관리한다. 이거 중요한 부분인듯
    /*
     Stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /*
     Stock 감소
     */
    public void removeStock(int quantity) {

        int restStock = this.stockQuantity - quantity;

        if (restStock < 0) {
            throw new NotEnoughStockException("남은 Stock 수를 초과했습니다. 재시도해주세요");
        }

        this.stockQuantity = restStock;
    }
}
