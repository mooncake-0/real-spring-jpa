package com.example.actualjpa.Test;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "apple")
public class Apple {

    @Id
    @GeneratedValue
    @Column(name = "apple_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_id")
    private Tree tree;

    private String name;

    @Override
    public String toString() {
        return "Apple{" +
                "id=" + id +
                ", tree=" + tree +
                ", name='" + name + '\'' +
                '}';
    }
}
