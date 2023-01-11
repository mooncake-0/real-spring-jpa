package com.example.actualjpa.domain;

import com.example.actualjpa.domain.embeddable.Address;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "delivery")
@Data
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

}
