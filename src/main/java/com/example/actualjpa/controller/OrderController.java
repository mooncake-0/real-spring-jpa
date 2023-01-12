package com.example.actualjpa.controller;

import com.example.actualjpa.domain.Item;
import com.example.actualjpa.domain.Member;
import com.example.actualjpa.domain.Order;
import com.example.actualjpa.repository.model.OrderSearch;
import com.example.actualjpa.service.ItemService;
import com.example.actualjpa.service.MemberService;
import com.example.actualjpa.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final MemberService memberService;

    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model) {
        //order 을 만드는 화면 >> 회원 / item 을 선택하게 되어 있음.
        // Order 을 만드는건.. 사실 회원 입장에서 하는 것이니까, 맞는 비즈니스 로직은 아님.

        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "order/orderForm";
    }

    // OrderForm 을 만들진 않고, Post 로 전달해줌.
    // 생성 하면 목록으로 이동
    @PostMapping("/order")
    public String order(@RequestParam(name = "memberId") Long memberId
            , @RequestParam(name = "itemId") Long itemId
            , @RequestParam(name = "count") int count) {


        // Controller 에서 찾기보다는 로직 서비스에서 찾는 로직을 넣는게 아무래도 나음.
        // 역할의 구분이 명확함
        // Controller는 엔티티 이런걸 잘 몰라도 됨. 사실 어떻게 하든 엄청 큰 상관은 없는데, 이게 할 수 있는게 더 많아짐. 왜냐면 ID를 보내니까.
        // 더 많은 쿼리를 서비스에서 할 수 있음.
        // 핵심 비즈니스 서비스에서 모든 일을 수행함. 컨트롤러는 엔간하면 간단한걸 넘기기만 함.
        // BUG :: 그리고 제일 중요한건, Transactional 이 유지될 수 있도록 로직에서 처리하는게 나음 (영속화 시켜놓을 수 있어서 더티 체킹 가능)
        //        서비스단에서 멤버를 받게 되면 그 멤버는 영컨이랑 아무 관계 없는 멤버임.

        Long orderId = orderService.order(memberId, itemId, count); // 나중에 그 오더 화면으로 이동하려면 이 Id 활용해보면 됨
        return "redirect:/orders"; // "/" + orderId;
    }

    // ModelAttribute 는 Get 방식으로 날라가는걸 객체로 묶는 거임
    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch // 특정 Order 을 검색하는 쿼리 객체
                            , Model model
                            ) {

        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);

        return "order/orderList";

    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable(name = "orderId") Long orderId) {

        orderService.cancelOrder(orderId);
        // 객체에 대한 삭제는 안하는지?? // ㅇㅇ 안하고 상태만 CANCEL 이 되어있고 조회는 되도록 바꿈. 이게 맞음 ㅋㅋ
        return "redirect:/orders";
    }
}
