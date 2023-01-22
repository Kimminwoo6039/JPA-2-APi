package com.jpabook.www.controller;
import com.jpabook.www.domain.Member;
import com.jpabook.www.domain.Order;
import com.jpabook.www.domain.OrderSearch;
import com.jpabook.www.domain.item.Item;
import com.jpabook.www.service.ItemService;
import com.jpabook.www.service.MemberService;
import com.jpabook.www.service.OrderService;
import lombok.Getter;
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

        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members",members);
        model.addAttribute("items",items);

        return "order/orderForm";
    }

    @PostMapping("/order")
    public String create(@RequestParam("memberId") Long memberId,
                         @RequestParam("itemId") Long itemId,
                         @RequestParam("count") int count) {
        orderService.order(memberId,itemId,count);
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch,Model model) {
        List<Order> orders = orderService.findAllByString(orderSearch);
        model.addAttribute("orders",orders);
        return "order/orderList";
    }

    @PostMapping(value = "/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
