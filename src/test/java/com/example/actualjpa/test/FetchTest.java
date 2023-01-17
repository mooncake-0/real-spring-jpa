package com.example.actualjpa.test;

import com.example.actualjpa.Test.Apple;
import com.example.actualjpa.Test.Tree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class FetchTest {

    @Autowired
    EntityManager em;

    Tree tree = new Tree();
    Apple apple = new Apple();
    Apple apple2 = new Apple();
    Apple apple3 = new Apple();

    @BeforeEach
    void be() {
        tree.setName("tree");

        apple.setName("apple");
        apple.setTree(tree);

        apple2.setName("apple");
        apple2.setTree(tree);

        apple3.setName("apple");
        apple3.setTree(tree);

        em.persist(tree);
        em.persist(apple);
        em.persist(apple2);
        em.persist(apple3);

        em.flush();
        em.clear();

        System.out.println("==================================================");

    }

    @Test
    @DisplayName("1:N에서 N이 1을 조회시 :: LAZY LOADING 의 타입은 Proxy")
    void test1() {

        //Apple 을 조회한다.
        Apple apple1 = em.find(Apple.class, apple.getId());

        System.out.println("Tree Class = " + apple1.getTree().getClass());
        System.out.println("================== SELECT QUERY 나감 =====================");

        System.out.println("apple1 = " + apple1);

        // apple1 = Apple{id=1, tree= Tree{id=,name='tree'}, name='apple'}

    }

    @Test
    @DisplayName("1:N에서 1이 N List를 조회시 :: LAZY LOADING 의 타입은 Proxy")
    void test2() {

        Tree findTree = em.find(Tree.class, tree.getId());
        System.out.println("findTree.getApples().get(0).getClass() = " + findTree.getApples().get(0).getClass());

    }
}
