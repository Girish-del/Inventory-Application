package com.girish.order_service.order.service.service;

import com.girish.order_service.order.service.client.InventoryClient;
import com.girish.order_service.order.service.dto.OrderRequest;
import com.girish.order_service.order.service.event.OrderPlacedEvent;
import com.girish.order_service.order.service.model.Order;
import com.girish.order_service.order.service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void placeOrder(OrderRequest orderRequest){

        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        if(isProductInStock){
            // map OrderRequest to Order Object
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());

            // save order to OrderRepository
            orderRepository.save(order);

            // Send message to kafka topic
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(), orderRequest.userDetails().email());
            log.info("Start - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
            kafkaTemplate.send("order-placed",orderPlacedEvent);
            log.info("End - Sent OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
        }
        else{
            throw new RuntimeException("Product with SkuCode " + orderRequest.skuCode() + " is not in stock");
        }



    }
}
