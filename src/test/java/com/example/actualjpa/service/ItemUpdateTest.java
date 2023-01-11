package com.example.actualjpa.service;

import com.example.actualjpa.domain.items.ItemBook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootTest
@Transactional
public class ItemUpdateTest {

    @PersistenceContext
    EntityManager em;

    @Test
    void 수정테스트() throws Exception {

        // 조회를 한다 > 영속화가 된다
        ItemBook itemBook = em.find(ItemBook.class, 1L);

        itemBook.setName("newName");

        // 이후 FLUSH 를 하고 Transaction Commit 발생 >> JPA 가 더티체킹을 통해 처음에 저장될 때의 스냅샷과 달라진 점을 파악한다
        // 이걸 변경 감지라고 함
    }

}
