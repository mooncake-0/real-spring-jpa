package com.example.actualjpa.api.controller;


import com.example.actualjpa.domain.Order;
import com.example.actualjpa.repository.model.OrderSearch;
import com.example.actualjpa.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
