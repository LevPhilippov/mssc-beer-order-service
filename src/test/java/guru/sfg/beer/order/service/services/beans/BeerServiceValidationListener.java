package guru.sfg.beer.order.service.services.beans;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class BeerServiceValidationListener {

    @Autowired
    JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_REQUEST_QUEUE)
    public void listen(Message message) {
        ValidateOrderRequest request = (ValidateOrderRequest) message.getPayload();
        log.debug("******Received a validation request for beerOrder {}******", request.getBeerOrderDto().getId());
        ValidateOrderResponse response = ValidateOrderResponse.builder().beerOrderId(request.getBeerOrderDto().getId()).isValid(true).build();
        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,response);
    }
}
