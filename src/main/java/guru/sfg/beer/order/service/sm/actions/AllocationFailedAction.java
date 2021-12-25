package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.brewery.model.events.AllocationOrderCompensatingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllocationFailedAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum>  {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        UUID orderId = UUID.fromString((String)context.getMessageHeader(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER));
        log.debug("*******Sending an allocation compensation action request for order {}*******", orderId);
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATION_FAILED_COMPENSATING_QUEUE, AllocationOrderCompensatingRequest
                .builder()
                .beerOrderId(orderId)
                .build());
    }
}
