package com.example.actualjpa.api.controller;


import com.example.actualjpa.domain.Order;
import com.example.actualjpa.domain.OrderStatus;
import com.example.actualjpa.domain.embeddable.Address;
import com.example.actualjpa.model.query.QuerySimpleOrderDto;
import com.example.actualjpa.repository.model.OrderSearch;
import com.example.actualjpa.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    // XToOne (One, Many)
    // Order / Member, Delivery 의  연관관계를 걸리게 하는게 메인
    // Member (1:N) Order
    // Order (1:1) Delivery

    private final OrderService orderService;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {

        /*
         Lazy 로딩이 걸려있는게 문제가 아니라, findOrders 명령어 자체가 member 를 조인해서 다 가져오라고  함.
         >> order > member join 함
         */
        List<Order> orders = orderService.findOrders(new OrderSearch());
//        System.out.println("-----------------------");
//
//        for (Order order :
//                orders) {
//            System.out.println("order.getMember() = " + order.getMember());
//        }
//        return null;
        return orders;
    }

    /*
     이거에도 문제점이 있음:
     >  findOrder 를 함
     >  order 를 가지고 옴
     >  order.getMember 를 함. LAZY 로딩으로 Member 를 가져옴
     >  order.getDelivery 를 함. LAZY 로딩으로 Delivery 를 가져옴 >> Query 가 한 요청에 너무 많이 나간다.

     >> 심지어 두명이니까 하나씩 더 나감.... 레이지 로딩이라 ....Order 와 member 는 하나씩 연관되어 있기 때문에. ..
     >> 하나의 Order 를 가져오라는 명령어의 결과를 위해서, 각 연관된 N명의 Member 들의 모든 것을 조회한다 (N+1 문제) // 참고로 여기선 Delivery 까지 해서 2N+1
     >> 지연 로딩의 단점이긴 함.
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        List<Order> orders = orderService.findOrders(new OrderSearch());

        List<SimpleOrderDto> responseList
                = orders.stream()
                .map((order) -> new SimpleOrderDto(order.getId(), order.getMember().getName(), order.getOrderDate(), order.getStatus(), order.getDelivery().getAddress()))
                .collect(Collectors.toList());

        return responseList;
    }

    // Fetch Join 을 통해서 최적화된 쿼리를 통해서 동일한 요청을 진행한다.
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {

        // 우선 쿼리를 어떻게 내보내야 할까 생각? 더블 조인 밖에 없는 것으로 보이는디?
        // select o.order_id, m.member_name, o.order_date, o.order_status, d.delivery_address
        //   from  order o join member m on o.member_id = m.member_id
        //   join delivery d on d.delivery_id = o.delivery_id =?

        // JPA 에서는 fetch join 을 사용하고, 이 fetch Join 에 대해서 100% 이해해야 한다
        // 한방 쿼리 나가는 모습을 확인할 수 있음

        // 오더 칼럼에 이미 다 들어가있음>> 탐색 그래프 형태로 들어가 있음
        List<Order> allOrders = orderService.findAllWithMemberDelivery();

        List<SimpleOrderDto> responseList
                = allOrders.stream()
                .map((order) -> new SimpleOrderDto(order.getId(), order.getMember().getName(), order.getOrderDate(), order.getStatus(), order.getDelivery().getAddress()))
                .collect(Collectors.toList());

        return responseList;
    }

    // V4 는 조회용이다
    @GetMapping("/api/v4/simple-orders")
    public List<QuerySimpleOrderDto> ordersV4() {
        return orderService.findSimpleOrderDtos();
    }


    /**
     * 과연 V4가 꼭 V3보다 좋을까?
     * >> V4 는 fetch join 과 동일하지만 원하는 것만 들고 온 것, 튜닝한 것
     * >> V4 는 SQL 짜듯이 JPQL 을 짠 것.
     * >> V4 의 단점은, 해당 API 만을 위해서만 존재하는 로직이다. >> 재사용성이 없을 가능성이 높음
     * >> V3 는 사실상 All Get 이기 때문에 여러 곳에 사용할 수 있음. Entity 를 조회하였기 때문. 그래서 변경하면 영컨에 적용시킬 수 있다
     * >> 하지만 V4 는 DTO 로 조회한 것이기 때문에, 영속화시킬 수도 없음
     * >> 작지만 효율적인 DB 사용을 할 수 있음. (성능상은 4번이 조금 더 낫다)
     * <p>
     * 제일 중요한 것
     * >> Controller - Service - Repository 계층이 특정 API 에 의존하고 있다는 점
     * >> API Spec 이 Repository 에 들어와버렸기 때문에, API 스펙 바뀌면 또 다 뜯어 고쳐야 함.
     * >> 사실 냉정하게 말하면 OrderRepository 인데 query 모델을 불러오고 있으니, 이건 깨진 로직으로 봐도 됨.
     * <p>
     * >> Trade Off 가 확실히 있다. >> 그렇다면 생각해볼 문제.. 정말.. 성능 차이가 그렇게 클까?
     * >> App 마다 다르기 때문에, 이건 성능테스트를 해보는게 맞음.
     * >> but 참고로, 사실 필드 몇개 더 늘어난다고 해서 엄청난 영향을 주진 않음. 사실 join 하거나, where 찾거나 이런게 성능에 큰 영향을 주는 편
     */


    // API 스펙에 맞춰서 나감
    @Data
    @AllArgsConstructor
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
    }

}
