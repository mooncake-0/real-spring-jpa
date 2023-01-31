package com.example.actualjpa.repository.query;

import com.example.actualjpa.domain.OrderStatus;
import com.example.actualjpa.domain.embeddable.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderQueryDto {

    // 가져오고 싶은 데이터들
    // select ~ from 할 때 ~ 에 들어가는거임

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus status, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.status = status;
        this.address = address;
    }
}
