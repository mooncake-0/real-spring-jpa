package com.example.actualjpa.service;

import com.example.actualjpa.domain.*;
import com.example.actualjpa.repository.ItemRepository;
import com.example.actualjpa.repository.MemberRepository;
import com.example.actualjpa.repository.OrderRepository;
import com.example.actualjpa.repository.model.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 비즈니스 로직이 엔티티 쪽에 있는 것을 도메인 모델 패턴 이라고 한다

 */

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    /*
     CASCADE 의 원리도 한번 볼 수 있음
     CASCADE 같은 경우 다른 곳에서 참조하지 않고 거의 여기에서만 사용할 것이다.
     참조란 막 setting 하는 것.
     그렇다면 CASCADE 같은걸 사용해도 대체적으로 안전하다.
     > 잘 모르겠으면 일단 쓰지 마셈.. 나중에 배워가면서 refactoring 하는 것이 더 좋음
     */

    // 중요 로직 3가지
    // 주문 (Order 생성)
    public Long order(Long memberId, Long itemId, int count) {

        // 어떤 멤버가, 어떤 상품을, 몇 개 주문 하는지?
        Member foundMember = memberRepository.findOne(memberId);
        Item foundItem = itemRepository.findOne(itemId);

        // 배송 정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(foundMember.getAddress());

        // 주문 상품 생성
        // 단순한 예제를 위해 하나만 넣을 수 있도록 구성한다
        OrderItem orderItem = OrderItem.createOrderItem(foundItem, foundItem.getPrice(), count);
        // CASCADE 속성 때문에 연관주가 영속화되면 자동으로 OrderItem도 영속화된다.

        // 주문 생성 (따라서 orderItem 저장 필요 없이 이렇게 진행할 수 있다)
        Order order = Order.createOrder(foundMember, delivery, orderItem);
        orderRepository.save(order);

        return order.getId();
    }

    // 취소 할 때는 ID만 넘어올 것
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findOne(orderId);
        order.cancel();

        // JPA 의 강점
        //  --> 일반적으론 order 의 status 를 CANCEL로 바꾼 것
        //  --> OrderItem에서 Item Stock 갯수를 다시 올려주는 것
        //  --> 밖에서 다 SQL 을 뽑아줘야하는데, JPA 는 더티체킹 덕분에 안해줘도 됨. 다 영속화되어 있기 때문
        //  --> OrderItem 도 Lazy 이기 때문에 사용하기 위해 조회하는 순간 select 쿼리 다 날라감

    }

    public Order findOrder(Long orderId) {

        return orderRepository.findOne(orderId);
    }

    // 검색
    public List<Order> findOrders(OrderSearch orderSearch) {

        return orderRepository.findAllByQDSL(orderSearch);

    }
}
