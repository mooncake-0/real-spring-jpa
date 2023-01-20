package com.example.actualjpa.domain;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // CACADE 를 지정해주는 곳 :: 내가 들어가게 되면, 영컨에 없어도 나랑 연관된 얘네들까진 다 넣겠다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id") // 로직상 여기서 Delivery 를 조회하는게 맞기 때문에 연관주임
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;


    // 연관관계 메서드 // 연관관계 주인에 있어서 수행해야 함
    public void changeMember(Member member) {
        this.member = member;
        // 멤버가 한 오더에 이 오더를 추가한다
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void changeDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    // Setting Method (세터는 언제 넣어주면 되는 건가요???)
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    // 생성 메서드 **** 여러 연관관계에 의해 생성이 복잡함.
    // 이럴 경우에는 생성 메서드가 있으면 좋음

    // 이걸 잘 모르면, 누군가는 그냥 생성자를 사용해서 쓸 수도 있음. 그러면 분산됨.
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.changeMember(member);
        order.changeDelivery(delivery);

        for (OrderItem settingItem : orderItems) {
            order.addOrderItem(settingItem);
        }

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //  따라서 이렇게 할거면 막아놔야 함 >> protected 로 아무데서나 사용하는 것을 막아놓음
    //  같은 것 : @NoArgsConstructor(access = AccessLevel.PROTECTED)
    //  이렇게 제약하는 스타일로 짜는게 안전함
    protected Order() {
    }

    // 비즈니스 로직
    /*
     주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다");
        }

        this.setStatus(OrderStatus.CANCEL);
        // 한 Order 에는 여러 Item 들이 있을 수 있음
        // 이 Order 에 대한 모든 아이템들에서 제외한다?
        // A, B, C  상품을 주문했음 그럼 이 오더에는 3개의 OrderItem 이 있는 것
        // 이 Order 을 Cancel 하면, A,B,C 상품에 대한 모든 OrderItem 을 취소해줘야함
        for (OrderItem items : this.orderItems) {
            // 재고 수량 원복
            items.cancel();
        }
    }

    // 조회 로직
    /*
     전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : this.orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", member=" + member +
                ", orderItems=" + orderItems +
//                ", delivery=" + delivery +
                ", status=" + status +
                ", orderDate=" + orderDate +
                '}';
    }
}
