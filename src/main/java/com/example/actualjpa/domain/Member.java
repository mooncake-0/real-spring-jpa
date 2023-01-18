package com.example.actualjpa.domain;

import com.example.actualjpa.domain.embeddable.Address;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore // 그래도 스면 안됨. 엔티티를 직접 노출하면 안됨.  // 그렇다고 해도 EAGER 로 바꾸는건 더 안됨.
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();


}
