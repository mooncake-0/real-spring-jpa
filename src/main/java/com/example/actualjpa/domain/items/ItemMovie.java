package com.example.actualjpa.domain.items;

import com.example.actualjpa.domain.Item;
import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "item_movie")
@Data
@DiscriminatorValue(value = "M")
public class ItemMovie extends Item {

    private String director;
    private String etc;
}
