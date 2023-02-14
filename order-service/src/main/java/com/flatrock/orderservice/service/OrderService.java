package com.flatrock.orderservice.service;

import com.flatrock.orderservice.dto.InventoryResponse;
import com.flatrock.orderservice.dto.OrderLineItemsDto;
import com.flatrock.orderservice.dto.OrderRequest;
import com.flatrock.orderservice.event.OrderPlacedEvent;
import com.flatrock.orderservice.model.Order;
import com.flatrock.orderservice.model.OrderLineItems;
import com.flatrock.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();


        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class).block();

        boolean areAllItemsAvailable = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);

        if(areAllItemsAvailable){
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
        }else{
            throw new IllegalArgumentException("Product is not available now, please try again");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
