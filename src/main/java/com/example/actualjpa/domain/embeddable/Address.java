package com.example.actualjpa.domain.embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {

    private String city;
    private String street;

    /*
     Immutable 복습 필요 : 값의 변경이 설계되면 안됨
     같이 참조하고 있는 녀석들까지 꼬일 수 있기 때문
     새로 넣어주기 위해선 아예 새로 생성후 넣어줘야함 (영컨에 의해 관리되지 않도록)
     */

    public Address(String city, String street) {
        this.city = city;
        this.street = street;
    }

    /*
     JPA 는 자신의 기술을 활용할 때 (프록시 등) 기본 생성자를 잘 사용함.
     따라서 기본 생성자가 필요한데, 사람들이 많이 사용하지 않도록 하기 위해서 다음과 같이 한다
     */
    protected Address() {

    }
}
