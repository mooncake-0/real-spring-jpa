package com.example.actualjpa.repository.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtoWithItems() {
        /*
         Collection 부분은 Dto Constructor Projection 으로 가져올 수 없음을 기억
         이렇게 직접 뤂을 돌리면서 쿼리를 짜줘야 함.
         */
        List<OrderQueryDto> result = findOrderQueryDtos();

        // 가지고 온 OQD 에 각각 자기가 가지고 있어야 할 OI 컬렉션들을 넣어줄 것임
        result.forEach(oqd -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(oqd.getOrderId());
            oqd.setOrderItems(orderItems);
        });

        return result;
    }

    public List<OrderQueryDto> findOrderQueryDtos() {

        // 참고로, 컬렉션을 바로 넣을 수는 없다
        return em.createQuery(
                "select new com.example.actualjpa.repository.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderQueryDto.class
        ).getResultList();
    }


    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new com.example.actualjpa.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                                "from OrderItem oi "
                                + "join oi.item i "
                                + "where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }


    /*
     V5 : 목적, N+1 을 해결하자\
     >> 위에서는 쿼리를 계속 각각 돌리면서 하는 것이였다면, 이것은 한번에 다 가져온 다음에 메모리에서 매칭해주는 방법 (메모리를 사용한다는게 핵심)
     >> 쿼리 나가는 갯수가 훨씬 줄어듬 (한번에 가지고 오니까 N+1 은 일단 해결됨)
     MEMO: 이런식으로 Repository Logic 을 작성하기도 하는게 좋은 경험인듯. 메모리를 사용해도 된다
     */
    public List<OrderQueryDto> findOrderQueryDtosOptimized() {

        // 일단 위와 동일하게 Order 에 대해서는 모두 불러옴
        List<OrderQueryDto> result = findOrderQueryDtos();

        // ID List 를 생성한다 (지금 같은 경우 2개의 OrderId 가 꼽혀 있을 것) // 아래 쿼리에 넣어주기 위함
        List<Long> orderIds
                = result.stream().map(oqd -> oqd.getOrderId()).collect(Collectors.toList());

        // 각 Order 에 대한 Item 을 모두 퍼올린다 (한번에)
        List<OrderItemQueryDto> oiDtoList = em.createQuery(
                        "select new com.example.actualjpa.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class // IN 절로 여러 ID를 SQL에 실장할 수 있도록 변경한다 (위에서 만든 모든 OrderId List를 박는다
                ).setParameter("orderIds", orderIds)
                .getResultList();

        // 이젠 가져온 OrderItemQueryDto 를 result 에 세팅을 해줘야 한다
        // 따라서 for loop 두번 돌리지 말고, 다음과 같이 자료구조를 활용해서 result 가 찾아서 지정할 수 있도록 Map 을 만들어 본다

        Map<Long, List<OrderItemQueryDto>> singleItemDtoMap = oiDtoList.stream().collect(Collectors.groupingBy(singleItemDto -> singleItemDto.getOrderId()));

        result.forEach(orderQueryDto -> orderQueryDto.setOrderItems(singleItemDtoMap.get(orderQueryDto.getOrderId()))); // ID 를 통해 각각 맞는 oiList 와 매칭해준다

        return result;

    }


    public List<OrderFlatDto> findOrderQueryDtosOptimizedByFlat() {

        // MEMO: 여기까지는 다 fetch join 했을 때랑 똑같음
        //      (다만 원하는 데이터 선택적으로 가져와주긴 함) >> 원하는 데이터 선택은 무조건 Prjojection 임
        return em.createQuery(
                "select new com.example.actualjpa.repository.query.OrderFlatDto(" +
                        "o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"
                        + " from Order o"
                        + " join o.member m"
                        + " join o.delivery d"
                        + " join o.orderItems oi" // 데이터 뻥튀기 예상
                        + " join oi.item i", OrderFlatDto.class
        ).getResultList();

    }
}
