package com.example.actualjpa.service;

import com.example.actualjpa.domain.Item;
import com.example.actualjpa.domain.Member;
import com.example.actualjpa.domain.Order;
import com.example.actualjpa.domain.OrderStatus;
import com.example.actualjpa.domain.embeddable.Address;
import com.example.actualjpa.domain.items.ItemBook;
import com.example.actualjpa.exception.NotEnoughStockException;
import com.example.actualjpa.repository.ItemRepository;
import com.example.actualjpa.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;


// 사실 이런 로직 테스트 전에 수행되어야 하는게 각 Method 별로 필요한 단위 테스트!
// 여기서 하진 않지만 꼭 해야함
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderService orderService;

    @Test
    void 상품주문() throws Exception {

        //given
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("SEOUL", "강가"));
        memberRepository.save(member);

        ItemBook book = new ItemBook();
        book.setName("JPA 알아보기");
        book.setPrice(10000);
        book.setStockQuantity(10);
        itemRepository.save(book);

        int ordercount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), ordercount);

        //then
        Order finalOrder = orderService.findOrder(orderId);

        Assertions.assertEquals(OrderStatus.ORDER, finalOrder.getStatus(), "상품 주문시 상태는 ORDER");
        Assertions.assertEquals(1, finalOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다");
        Assertions.assertEquals(ordercount * 10000, finalOrder.getTotalPrice(), "총 가격은 주문 * 수량 이다");
        Assertions.assertEquals(8, book.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다"); // 영컨에 의해 Item 은 영속화되기 때문에, order 에서 상태가 변경된다.

    }

    @Test
    void 주문취소() throws Exception {
        // given
        Member member = new Member();
        member.setName("길냥이");
        member.setAddress(new Address("지랄", "맞음"));
        memberRepository.save(member);

        int originalStock = 5;

        ItemBook book = new ItemBook();
        book.setAuthor("이잉");
        book.setStockQuantity(originalStock);
        book.setPrice(10000);
        book.setName("책입니다");
        itemRepository.save(book);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount); // 여기까지 주어진 상황인게 맞음

        // when
        orderService.cancelOrder(orderId);

        // then
        Order foundOrder = orderService.findOrder(orderId);

        Assertions.assertEquals(OrderStatus.CANCEL, foundOrder.getStatus(), "주문 상태가 CANCEL 이여야 한다");
        Assertions.assertEquals(originalStock, book.getStockQuantity(), "재고수가 원복되어야 한다");

    }

    @Test
    @DisplayName("예외처리 되어야 하는 Case Test : 이런 Test 가 매우 중요")
    void 상품주문_재고수량초과() throws Exception {

        // given
        Member member = new Member();
        member.setName("길냥이");
        member.setAddress(new Address("지랄", "맞음"));
        memberRepository.save(member);

        ItemBook book = new ItemBook();
        book.setAuthor("이잉");
        book.setStockQuantity(5);
        book.setPrice(10000);
        book.setName("책입니다");
        itemRepository.save(book);

        // when
//        orderService.order(member.getId(), book.getId(), 6);

        // when + then
        Assertions.assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), book.getId(), 6);
        });

    }

}
