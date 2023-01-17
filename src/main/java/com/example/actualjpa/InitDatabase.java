package com.example.actualjpa;

import com.example.actualjpa.domain.Delivery;
import com.example.actualjpa.domain.Member;
import com.example.actualjpa.domain.Order;
import com.example.actualjpa.domain.OrderItem;
import com.example.actualjpa.domain.embeddable.Address;
import com.example.actualjpa.domain.items.ItemBook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component // Bean 인데, 초기화 데이터 세팅용 빈
@RequiredArgsConstructor
public class InitDatabase {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }


    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void dbInit1() {
            Member member = new Member();
            member.setName("userA");
            member.setAddress(new Address("서울", "1"));

            ItemBook book = new ItemBook();
            book.setName("JPA1 BOOK");
            book.setPrice(10000);
            book.setStockQuantity(100);

            ItemBook book2 = new ItemBook();
            book2.setName("JPA2 BOOK");
            book2.setPrice(20000);
            book2.setStockQuantity(100);

            em.persist(member);
            em.persist(book);
            em.persist(book2);

            OrderItem orderItem = OrderItem.createOrderItem(book, book.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());

            Order order = Order.createOrder(member, delivery, orderItem, orderItem2);// 한 번에 주문
            em.persist(order);

        }

        public void dbInit2() {
            Member member = new Member();
            member.setName("userB");
            member.setAddress(new Address("성남", "2"));

            ItemBook book = new ItemBook();
            book.setName("Spring 1 BOOK");
            book.setPrice(20000);
            book.setStockQuantity(200);

            ItemBook book2 = new ItemBook();
            book2.setName("Spring 2 BOOK");
            book2.setPrice(40000);
            book2.setStockQuantity(300);

            em.persist(member);
            em.persist(book);
            em.persist(book2);

            OrderItem orderItem = OrderItem.createOrderItem(book, book.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 4);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());

            // 딜리버리랑 오더 아이템은 따로 em.persist 를 안해도, Order 를 넣으면 알아서 들어감. (CASCADE)
            Order order = Order.createOrder(member, delivery, orderItem, orderItem2);// 한 번에 주문
            em.persist(order);

        }

    }
}
