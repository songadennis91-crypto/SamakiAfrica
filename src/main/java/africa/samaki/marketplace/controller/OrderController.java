package africa.samaki.marketplace.controller;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public OrderController(OrderRepository orderRepository,
                           ListingRepository listingRepository,
                           UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/listings/{id}/order")
    public String orderForm(@PathVariable Long id, Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        if (user.getRole() != Role.BUYER) return "redirect:/listings";
        model.addAttribute("listing", listingRepository.findById(id).orElseThrow());
        return "order-form";
    }

    @PostMapping("/listings/{id}/order")
    public String placeOrder(@PathVariable Long id,
                             @RequestParam Double quantityKg,
                             @RequestParam(defaultValue = "false") boolean deliveryRequired,
                             @RequestParam(required = false) String deliveryAddress,
                             Principal principal, Model model) {
        User buyer = userRepository.findByEmail(principal.getName()).orElseThrow();
        if (buyer.getRole() != Role.BUYER) return "redirect:/listings";
        Listing listing = listingRepository.findById(id).orElseThrow();

        if (listing.getStatus() == ListingStatus.SOLD || quantityKg <= 0
                || quantityKg > listing.getQuantityKg()) {
            model.addAttribute("listing", listing);
            model.addAttribute("error", "Please enter a quantity between 0 and "
                    + listing.getQuantityKg() + " kg.");
            return "order-form";
        }
        if (deliveryRequired && (deliveryAddress == null || deliveryAddress.isBlank())) {
            model.addAttribute("listing", listing);
            model.addAttribute("error", "Please enter a delivery address.");
            return "order-form";
        }

        Order order = new Order();
        order.setListing(listing);
        order.setBuyer(buyer);
        order.setQuantityKg(quantityKg);
        order.setTotalPrice(listing.getPricePerKg().multiply(BigDecimal.valueOf(quantityKg)));
        order.setDeliveryRequired(deliveryRequired);
        order.setDeliveryAddress(deliveryAddress);
        orderRepository.save(order);
        return "redirect:/my-orders";
    }

    @GetMapping("/my-orders")
    public String myOrders(Principal principal, Model model) {
        User buyer = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("orders", orderRepository.findByBuyerOrderByCreatedAtDesc(buyer));
        return "my-orders";
    }

    @GetMapping("/fisher/orders")
    public String fisherOrders(Principal principal, Model model) {
        User fisher = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("orders", orderRepository.findByListing_FisherOrderByCreatedAtDesc(fisher));
        return "fisher-orders";
    }

    @PostMapping("/orders/{id}/accept")
    public String accept(@PathVariable Long id, Principal principal) {
        Order order = orderRepository.findById(id).orElseThrow();
        Listing listing = order.getListing();
        if (listing.getFisher().getEmail().equals(principal.getName())
                && order.getStatus() == OrderStatus.PENDING
                && listing.getQuantityKg() >= order.getQuantityKg()) {
            order.setStatus(OrderStatus.ACCEPTED);
            double left = listing.getQuantityKg() - order.getQuantityKg();
            listing.setQuantityKg(left);
            if (left <= 0) listing.setStatus(ListingStatus.SOLD);
            listingRepository.save(listing);
            orderRepository.save(order);
        }
        return "redirect:/fisher/orders";
    }

    @PostMapping("/orders/{id}/decline")
    public String decline(@PathVariable Long id, Principal principal) {
        Order order = orderRepository.findById(id).orElseThrow();
        if (order.getListing().getFisher().getEmail().equals(principal.getName())
                && order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.DECLINED);
            orderRepository.save(order);
        }
        return "redirect:/fisher/orders";
    }
}