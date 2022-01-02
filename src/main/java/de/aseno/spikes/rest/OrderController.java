package de.aseno.spikes.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import de.aseno.spikes.order.Order;
import de.aseno.spikes.order.OrderRepository;
import de.aseno.spikes.order.ProductNameAndPrice;

@RestController
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView root() {
        return new ModelAndView("redirect:/swagger-ui/");
    }

    // CREATE

    @PostMapping("orders")
    public Order createOrder(@RequestBody Order order) {
        return orderRepository.save(order);
    }

    // UPDATE

    @PutMapping("orders/{oid}/{pid}")
    public Order updateOrder(@PathVariable("oid") UUID oid, @PathVariable("pid") UUID pid, @RequestBody Order order) {
        order.getKey().setOrderId(oid);
        order.getKey().setProductId(pid);
        return orderRepository.save(order);
    }

    // DELETE

    @DeleteMapping("orders/{oid}/{pid}")
    public void deleteOrder(@PathVariable("oid") UUID oid, @PathVariable("pid") UUID pid) {
        orderRepository.deleteByKeyOrderIdAndKeyProductId(oid, pid);
    }

    @DeleteMapping("orders/{oid}")
    public void deleteOrders(@PathVariable("oid") UUID oid) {
        orderRepository.deleteByKeyOrderId(oid);
    }

    // FIND

    @GetMapping("orders/{oid}/{pid}")
    public ProductNameAndPrice findOrder(@PathVariable("oid") UUID oid, @PathVariable("pid") UUID pid) {
        return orderRepository.findByKeyOrderIdAndKeyProductId(oid, pid);
    }

    @GetMapping("orders/{oid}")
    public List<ProductNameAndPrice> findOrders(@PathVariable("oid") UUID oid) {
        return orderRepository.findByKeyOrderId(oid);
    }

    @GetMapping("orders")
    public List<ProductNameAndPrice> findAll() {
        return orderRepository.findAllProjectedBy();
    }

}

