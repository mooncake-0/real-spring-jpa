package com.example.actualjpa.api.controller;

/*
 사실상 같은 컬렉션이지만, 강의상 두번 째 파트
 -> 일반 API Controller > 지연 로딩 & 조회 성능 최적화
 -> 이번 Next API Cont > 컬렉션 조회 최적화
 ----->목적 >> Order 에서 OrderItem 을 조회하러 간다 Lazy 걸려 있는데 어떻게 최적화 할까? 그리고 어떻게 응답을 해줘야 할까?
 */

import com.example.actualjpa.domain.Order;
import com.example.actualjpa.domain.OrderItem;
import com.example.actualjpa.domain.OrderStatus;
import com.example.actualjpa.domain.embeddable.Address;
import com.example.actualjpa.repository.OrderRepository;
import com.example.actualjpa.repository.model.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderNextApiController {

    private final OrderRepository orderRepository;

    // 이거 Error 뜹니다 > JSONIGNORE 다 걸어야 하기 때문
    @GetMapping("/api/v1/orders")
    public List<Order> getOrdersV1() { // 원래는 Entity 반환하면 안됨

        List<Order> allByQDSL = orderRepository.findAllByQDSL(new OrderSearch());

        // 현재 Order 는 모든 연관관계 Entity 들이 Lazy 로디이므로 다 불러오려면 초기화 필요
        for (Order order : allByQDSL) {
            order.getMember().getName(); // 조회하는 Member 각제 초기화
            order.getDelivery().getAddress(); // 조회하는 Address 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems(); // OrderItem 초기화
            orderItems.forEach(o -> o.getItem().getName()); // OrderItem 이 가지고 있는 각 Item 강제 초기화
        }
        // 각각 하나 할 때마다 쿼리 ㅈㄴ 나가는 모습 볼 수 있음

        return allByQDSL;
    }

    /*
     MEMO : 어쨌든 지연로딩을 최적화해주지 않으면, Dto 에 맞춰서 내보내더라도, ㄹㅇ ㅈㄴ 성능 쉣이 된다.
     */

    /*
     DTO 로 감싸라는 것은 그냥 OrderDto 로 하나 감싸라는게 아니라,
     그 내부 연관관계 엔티티들 또한 다 Dto 로 묶어야 한다.
     */
    @GetMapping("/api/v2/orders")
    public List<OrdersDto> ordersV2() {
        List<Order> allByQDSL = orderRepository.findAllByQDSL(new OrderSearch()); // 실무에선 이런거 페이징 하거나 할거임

        List<OrdersDto> collect = allByQDSL.stream().map(o -> new OrdersDto(o)).collect(Collectors.toList()); // 참고로 이거 N+1 문제 발생 // order 쿼리도 1번 돌고 또 한번 더 나감.

        return collect;
    }

    // 이런 연관관계를 불러오는 조인을 할 때는 "데이터 뻥튀기" 에 대해서 꼭 명심하고 있어야 한다.
    // 이 것을 어떻게 대응할 것인지?
    // 그래도 패치 조인을 하면, Query 가 10개 나가던 것이 1개로 튜닝을 할 수 있음. (참고로 위와 Service 단 로직은 완전히 동일한 것을 알 수 있음!)
    @GetMapping("/api/v3/orders")
    public List<OrdersDto> ordersV3() {

        // 심지어 그냥 얘를 가지고 iter 을 해봐도 4개임
        List<Order> allWithItem = orderRepository.findAllWithItem();

        // 중요 --> 심지어 REFERENCE 까찌 똒깥음~!~! 이거는 동일한 객체로 인지한단 것이다
        for (Order order : allWithItem) {
            System.out.println("order = " + order);
        }

        return allWithItem.stream().map(o -> new OrdersDto(o)).collect(Collectors.toList());
    }

    @Data
    static class OrdersDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; // 이런건 엔티티가 아니라 Value Object 라서 괜찮음
        private List<OrderItemDto> orderItems; // OrderItem 도 2개임. 그럼 각각에 대해서 Item 한번 씩 두번 나감.

        public OrdersDto(Order o) {
            this.orderId = o.getId();
            this.name = o.getMember().getName();
            this.orderDate = o.getOrderDate();
            this.orderStatus = o.getStatus();
            this.address = o.getDelivery().getAddress();
            this.orderItems = o.getOrderItems().stream().map(OrderItemDto::new).collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem oi) {
            // 내가 원하는 API SPEC 만 넣으면 됨
            this.itemName = oi.getItem().getName();
            this.orderPrice = oi.getItem().getPrice();
            this.count = oi.getCount();
        }
    }
}
