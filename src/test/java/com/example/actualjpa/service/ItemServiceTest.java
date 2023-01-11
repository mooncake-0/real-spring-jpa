package com.example.actualjpa.service;

import com.example.actualjpa.domain.Item;
import com.example.actualjpa.domain.items.ItemAlbum;
import com.example.actualjpa.domain.items.ItemBook;
import com.example.actualjpa.domain.items.ItemMovie;
import com.example.actualjpa.repository.ItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class ItemServiceTest {

    @Autowired ItemRepository itemRepository;
    @Autowired ItemService itemService;

    @Test
    void 상품저장() {

        //given
        ItemBook itemBook = new ItemBook();
        itemBook.setAuthor("김정필");
        ItemAlbum itemAlbum = new ItemAlbum();
        itemAlbum.setArtist("지논논");
        ItemMovie itemMovie = new ItemMovie();
        itemMovie.setDirector("낄낄낄");

        //when
        Long bookId = itemService.saveItem(itemBook);
        Long albumId = itemService.saveItem(itemAlbum);
        Long movieId = itemService.saveItem(itemMovie);

        //then
        Assertions.assertEquals(itemBook, itemRepository.findOne(bookId));
        Assertions.assertEquals(itemAlbum, itemRepository.findOne(albumId));
        Assertions.assertEquals(itemMovie, itemRepository.findOne(movieId));
    }

//    @Test
//    void
}
