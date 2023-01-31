package com.example.actualjpa.repository.query;

import com.example.actualjpa.domain.OrderStatus;
import com.example.actualjpa.domain.embeddable.Address;
import lombok.Data;

import java.time.LocalDateTime;

/*
 DB 에서 한 방에 다가져온다
 Order, OrderItem 을 다 Join 해버림
 한줄로(?)
 */
@Data
public class OrderFlatDto {

    // Order
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private Address address;

    // OrderItem
    private String itemName;
    private int orderPrice;
    private int count;

    public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus status, Address address, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.status = status;
        this.address = address;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
