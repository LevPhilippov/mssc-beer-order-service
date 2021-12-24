package guru.sfg.beer.order.service.services.inventory;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderAllocationListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(@Payload AllocateOrderResponse response){
        log.debug("******Received an allocation order response from allocation service for order id {}******", response.getBeerOrderDto().getId());
        if(response.getAllocationError()){
            beerOrderManager.allocationFailed(response.getBeerOrderDto());
            return;
        }
        if(response.getIsAllocated())
            beerOrderManager.allocateOrder(response.getBeerOrderDto());
        else
            beerOrderManager.allocationPendingInventory(response.getBeerOrderDto());
    }
}
