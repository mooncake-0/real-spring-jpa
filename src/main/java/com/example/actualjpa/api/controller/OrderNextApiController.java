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
import com.example.actualjpa.repository.query.OrderFlatDto;
import com.example.actualjpa.repository.query.OrderQueryDto;
import com.example.actualjpa.repository.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderNextApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

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
     DTO 로 감싸라는 것은 그냥 OrderDto 로 하나 감싸라는게 아니라,
     그 내부 연관관계 엔티티들 또한 다 Dto 로 묶어야 한다.
     */
    /*
     MEMO : 어쨌든 지연로딩을 최적화해주지 않으면, Dto 에 맞춰서 내보내더라도, ㄹㅇ ㅈㄴ 성능 쉣이 된다.
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
    // MEMO:: 의의와 한계 : 패치 조인을 통한 쿼리 최적화는 가능하나, 컬렉션 조회시 페이징이 불가하다.
    @GetMapping("/api/v3/orders")
    public List<OrdersDto> ordersV3() {

        // 심지어 그냥 얘를 가지고 iter 을 해봐도 4개임
        List<Order> allWithItem = orderRepository.findAllWithItem();

        // 중요 --> 심지어 REFERENCE 까찌 똒깥음~!~! (여기서 REF : 가르키는 메모리 주소) 이거는 동일한 객체로 인지한단 것이다
        for (Order order : allWithItem) {
            System.out.println("order = " + order);
        }

        return allWithItem.stream().map(o -> new OrdersDto(o)).collect(Collectors.toList());
    }

    // MEMO: 페치조인의 한계를 돌파하여, 컬렉션 조회시 페이징을 할 수 있는 방법 >> Batch 적용
    @GetMapping("/api/v3.1/orders")
    public List<OrdersDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {

        // 컬렉션관계가 되는 애들 말고, 조회하려는 곳에서 ToOne 관계인 모든 것을 조회한다.
//        List<Order> orderList = orderRepository.findAllWithMemberDelivery();// Order, Member, Delivery Fetch join >> 결과 row size에 영향 안주는 애들
        // 위와 동일하게 페이징이 되도록 처리해본다.
        List<Order> orderList = orderRepository.findAllWithMemberDeliveryWithPaging(offset, limit);

        List<OrdersDto> fetchJoinList = orderList.stream().map(o -> new OrdersDto(o)).collect(Collectors.toList());

        return fetchJoinList;

    }

    /*
     MEMO: DTO Constructor Projection 하는 방법 (Query Dto 형성)
           요약 :: 컬렉션된 애들은 DB에서 select ~ 문에 넣을 수가 없기 때문에, 1차로 가능한 애들을 fetch join 해준 뒤에 컬렉션애들은 컬렉션 DTO 로 따로 불러와줘야 한다
           결국 N+1 이 같이 발생하는 문제가 있음
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtoWithItems();
    }

    /*
    MEMO: V4 에서 DTO Projection 방법의 한계 : 결국 N+1 >> 이것을 최적화시키는 방법
          페치조인을 안하는대신 사용하는 방향이 DTO Projection 인 것을 좀 기억
          DTO 를 사용하는 이유 >> 원하는 값들을 지정해주기 위해서임, 단순히 select o from Order 를 하면 어쨌든 Collection 에 매핑될 수 있는 건 o 뿐임
          하지만 DTO 를 세팅해주면 원하는 데이터를 지정을 해놨기 때문에, 그냥 단순 join 을 해서 가져오면 됨
          참고로 페치조인을 쓰면 다 때려 박아지기 때문에 그것을 사용해도 되는 것임 (연관관계가 되어 있을시)
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findOrderQueryDtosOptimized();
    }

    /*
     MEMO : V5 에서 DTO 컬렉션 사용에서 N+1 이 발생하지 않도록 최적화를 해보았다. (in 절을 사용해서 한번에 메모리에 로딩하는 것이다)
            여기서 더 최적화할 수 있도록 해보자
            플랫을 사용하는 것 > 나중에 더 최적화 해야 쓰것다 싶을 때 한번 해보면 될ㄷ ㅡㅅ..? .. 결과 : Query 1번
            이것도 사실 V5 처럼, 메모리에서 작업하는 것을 도입하는 것 (분해를 햇다가 전송용 Dto조립을 해야함)
     */
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> ordersV6() {
        return orderQueryRepository.findOrderQueryDtosOptimizedByFlat();
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
