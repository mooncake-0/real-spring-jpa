package com.example.actualjpa.Test;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "tree")
public class Tree {

    @GeneratedValue
    @Id
    @Column(name = "tree_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "tree", fetch = FetchType.LAZY)
    private List<Apple> apples = new ArrayList<>();

    @Override
    public String toString() {
        return "Tree{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
