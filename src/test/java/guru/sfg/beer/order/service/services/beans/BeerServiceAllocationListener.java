package guru.sfg.beer.order.service.services.beans;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BeerServiceAllocationListener {

    @Autowired
    JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_REQUEST_QUEUE)
    public void listen(Message message){
        boolean isAllocated = true;
        boolean allocationError = false;

        AllocateOrderRequest request = (AllocateOrderRequest) message.getPayload();
        log.debug("******Received an allocation request from beer order service with order id {}******", request.getBeerOrderDto().getId());
        request.getBeerOrderDto().getBeerOrderLines().forEach(line-> line.setQuantityAllocated(line.getOrderQuantity()));

        if(request.getBeerOrderDto().getCustomerRef() != null){
            if(request.getBeerOrderDto().getCustomerRef().equals("allocation-fail"))
                allocationError=true;
            if(request.getBeerOrderDto().getCustomerRef().equals("allocation-await"))
                isAllocated = false;
        }

        AllocateOrderResponse response = AllocateOrderResponse.builder()
                .beerOrderDto(request.getBeerOrderDto())
                .isAllocated(isAllocated)
                .allocationError(allocationError)
                .build();
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,response);
    }


}
