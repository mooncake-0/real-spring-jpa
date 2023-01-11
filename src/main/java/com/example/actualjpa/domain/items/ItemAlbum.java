package com.example.actualjpa.domain.items;

import com.example.actualjpa.domain.Item;
import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "item_album")
@Data
@DiscriminatorValue(value = "A")
public class ItemAlbum extends Item {

    private String artist;
    private String etc;

}
