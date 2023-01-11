package com.example.actualjpa.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany // 실무 사용 금지
    @JoinTable(name = "category_item_linker",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    /*
     Self 로 연관관계를 거는 모습 이해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent; // 부모가 있을 수 있다

    @OneToMany(mappedBy = "parent")
    private List<Category> childs = new ArrayList<>();


}
