package com.example.actualjpa.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "order_item")

public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;
    private int count;

    // 비즈니스 생성 메서드
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count); // 주문한 갯수만큼 잔여 수량에서 제외한다
        return orderItem;
    }

    // 비즈니스 로직
    public void cancel() {
        getItem().addStock(count); // 주문한 갯수만큼 원ㄴ복
    }

    public int getTotalPrice() {
        return this.orderPrice * this.count;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", item=" + item.getId() +
//                ", order=" + order +
                ", orderPrice=" + orderPrice +
                ", count=" + count +
                '}';
    }
}
