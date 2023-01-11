package com.example.actualjpa.controller;

import lombok.Data;

@Data
public class BookForm {

    // 공통 속성
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private String author;
    private String isbn;

}
