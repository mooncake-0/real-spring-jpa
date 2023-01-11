package com.example.actualjpa.repository;


import com.example.actualjpa.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    /*
     한 영속성 컨텍스트를 위한 EntityManagerFactory 가 존재
     > 그 EMF 는 트랜젝션을 처리하기 위한 EntityManager 를 생성하며 사용함
     > 처리하기 위한 EM 을 주입해준다
     */
//    @PersistenceContext >> @Autowired 로 바꿀 수 있음
//    참고로, 원래는 @PC 써야만 됨. 하지만 스프링 부트에서 @Autowired 도 되게 지원을 해주는 것
//    그래서 결론적으로 이 방식도 채택할 수 있는 것
    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {

        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();

    }
}
