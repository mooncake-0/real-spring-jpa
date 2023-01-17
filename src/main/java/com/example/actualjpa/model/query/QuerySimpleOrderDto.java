package com.example.actualjpa.model.query;

import com.example.actualjpa.domain.OrderStatus;
import com.example.actualjpa.domain.embeddable.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//Query 용 DTO
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuerySimpleOrderDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
}
