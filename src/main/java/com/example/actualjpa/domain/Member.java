package com.example.actualjpa.domain;

import com.example.actualjpa.domain.embeddable.Address;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    /*
     컬렉션은 무조건 조회용이다. 변경하고 건드리는 순간 Hibernate 가 원하는대로 작동하지 않을 수 있음
     */
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();


}
