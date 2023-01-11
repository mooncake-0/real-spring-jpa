package com.example.actualjpa.domain.items;

import com.example.actualjpa.domain.Item;
import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "item_book")
@Data
@DiscriminatorValue(value = "B")
public class ItemBook extends Item {

    private String author;
    private String isbn;
}
