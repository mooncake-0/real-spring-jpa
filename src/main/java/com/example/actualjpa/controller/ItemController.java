package com.example.actualjpa.controller;

import com.example.actualjpa.domain.Item;
import com.example.actualjpa.domain.items.ItemBook;
import com.example.actualjpa.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {

        ItemBook itemBook = new ItemBook();
        // Setter 는 실무에서는 닫혀있는게 나음
        itemBook.setName(form.getName());
        itemBook.setPrice(form.getPrice());
        itemBook.setStockQuantity(form.getStockQuantity());
        itemBook.setAuthor(form.getAuthor());
        itemBook.setIsbn(form.getIsbn());
        itemService.saveItem(itemBook);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    // 수정이 정말 중요함!!!! JPA 에서 수정하는 방식에 대하여ㄴ
    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable(value = "itemId") Long itemId, Model model) {

        // 예제니까 BOOK 이라고 하자 (원래는 이러면 안됨)
        ItemBook targetItem = (ItemBook) itemService.findOne(itemId);

        // UPDATE 하기 위해서도 Form 을 활용할 것입니다.
        BookForm form = new BookForm();
        form.setId(targetItem.getId());
        form.setName(targetItem.getName());
        form.setPrice(targetItem.getPrice());
        form.setStockQuantity(targetItem.getStockQuantity());
        form.setAuthor(targetItem.getAuthor());
        form.setIsbn(targetItem.getIsbn());

        // 현재 있는 상태를 엮은 뒤에 html 에 뿌려줘야 현상태에서 변경을 할 수 있음
        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    // 정말 수정을 하는 부분
    @PostMapping("items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") BookForm form) { // BODY 에서 FORM 형식으로 가져올 데이터를 명시

        ItemBook itemBook = new ItemBook();

        itemBook.setId(form.getId());
        itemBook.setName(form.getName());
        itemBook.setPrice(form.getPrice());
        itemBook.setStockQuantity(form.getStockQuantity());
        itemBook.setAuthor(form.getAuthor());
        itemBook.setIsbn(form.getIsbn());

        itemService.saveItem(itemBook);

        return "redirect:/items";
    }
}
