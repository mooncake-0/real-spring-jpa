package com.example.actualjpa.service;

import com.example.actualjpa.domain.Item;
import com.example.actualjpa.domain.items.ItemBook;
import com.example.actualjpa.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Long saveItem(Item item) {
        itemRepository.save(item);
        return item.getId();
    }

    public void updateItem(Long itemId, ItemBook bookParam) {
        Item findItem = itemRepository.findOne(itemId); // 조회를 통해 영속화를 시킨다
        findItem.setPrice(bookParam.getPrice());
        findItem.setName(bookParam.getName());
        findItem.setStockQuantity(bookParam.getStockQuantity());
        // 할거 없음
    }



    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
