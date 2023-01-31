package com.example.actualjpa.repository;

import com.example.actualjpa.domain.Order;
import com.example.actualjpa.domain.OrderStatus;
import com.example.actualjpa.domain.QMember;
import com.example.actualjpa.domain.QOrder;
import com.example.actualjpa.model.query.QuerySimpleOrderDto;
import com.example.actualjpa.repository.model.OrderSearch;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long orderId) {
        return em.find(Order.class, orderId);
    }

    // 주문에 대한 검색 요청을 보낼 수 있다
    // 너의 생각 ^_^ (틀린점, member Join 안해서 SQL 두개 날라감 ^!^, like 문 안해서 포함결과 못가져옴 ^!^)
    /*public List<Order> findAll(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o join o.member m where m.name like :memberName and o.status = :status", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("memberName", orderSearch.getMemberName())
                .getResultList();
    }*/ // 틀리진 않았음.

    // orderSearch 가 다 들어있을 때만 생각함.
    // 둘다 값이 없는 상태로 들어왔는데 파싱을 걸 수도 있기 때문에, 이럴 때는 동적 쿼리를 필수적으로 작성해야 한다.
    // BUG :: 안좋은 방법
    public List<Order> findAll(OrderSearch orderSearch) {

        // 1번 경우, 하드 코딩 함 (강사님은 이렇게 안함)
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        if (orderSearch.getStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        // 이 다음에 StringUtils.hasText(orderSearch.getMemberName()) 하면서 조건 세팅을 해야함
        // if 문으로 동적으로 가야함.

        em.createQuery(jpql, Order.class);
        // .......
        // 이 이후에 Params Binding 까지 동적으로 if 문 박아가면서 해줘야함 ...
        // 이렇게 할 리는 절대 없지? 이렇게 할바엔 Mybatis 공부하지
        // bug도 ㅈㄴ 많음.

        return null;
    }

    // 2번 방법 역시 권장하지 않는 방법 .실무에서 쓰라는 방법은 아닌듯. 근데 ㅈㄴ 신기한건 JPA 권장 방법. (표준 SPEC)
    // 몰라 ㅅ발

    /**
     * JPA Criteria 로 해결한다
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {

        return null;
    }

    // 동적쿼리 권장 방법 > Query DSL 쓰셈요 ~!
    public List<Order> findAllByQDSL(OrderSearch orderSearch) {


        /*
         Query : select * from order o
                     join member m
                     on o.member_id = m.member_id
                     where o.orderStatus = {} and m.username = {};
         */

        return queryFactory.selectFrom(QOrder.order)
                .join(QMember.member)
                .on(QOrder.order.member.id.eq(QMember.member.id))
                .where(eqStatus(orderSearch.getStatus())
                        , eqName(orderSearch.getMemberName()))
                .fetch()
                ;
    }

    public List<Order> findAllWithMemberDelivery() {

//        // 차이점 확인 // 원래 쿼리는 뭘 가져올건지 select 할 수 있지만, 여기서도 할 수 있긴 하지만 일반적인 방식의 차이라고 알아두면 됨
//        // 그냥 join 은 가져오려는 Order 로만 join  한다 (따라서 다 쿼리 나가야함 레이지라)
////        return em.createQuery(
////                "select o from Order o" +
////                        " join o.member m" +
////                        " join o.delivery d", Order.class
////        ).getResultList();
//
//         Fetch Join 이 진짜 조인하는 테이블들 다 가져옴.
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();

    }

    public List<Order> findAllWithMemberDeliveryWithPaging(int offset, int limit) {
        return em.createQuery("select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<QuerySimpleOrderDto> findSimpleOrderDtos() {

        // QueryDsl Projections 와 완전 동일
        return em.createQuery("select new com.example.actualjpa.model.query.QuerySimpleOrderDto(" +
                        "o.id, o.member.name, o.orderDate, o.status, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", QuerySimpleOrderDto.class)
                .getResultList();
    }


    public List<Order> findAllWithItem() {
//        return em.createQuery("select  o from Order o"
        return em.createQuery("select distinct o from Order o" // 뻥튀기 문제 해결을 위한 distinct 문
                        + " join fetch o.member m "
                        + " join fetch o.delivery d "
                        + " join fetch o.orderItems oi "
                        + " join fetch oi.item i", Order.class)
                .setFirstResult(1)// 실행할 수 없는 것을 보여주기 위한 페이징 문법 ( 컬렉션 조회 페치 조인 + 디스팅트 시 불가)
                .setMaxResults(100)
                .getResultList()
                ;
    }


    private BooleanExpression eqStatus(OrderStatus orderStatus) {
        if (Objects.isNull(orderStatus)) {
            return null;
        }
        return QOrder.order.status.eq(orderStatus);
    }

    private BooleanExpression eqName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        return QMember.member.name.eq(name);
    }
}
