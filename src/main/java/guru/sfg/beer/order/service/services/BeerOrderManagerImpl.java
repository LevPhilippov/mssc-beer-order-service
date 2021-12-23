package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.interceptors.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String BEER_ORDER_ID_HEADER = "beerOrderId";
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder newSavedBeerOrder = beerOrderRepository.save(beerOrder);
        sendEvent(newSavedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return newSavedBeerOrder;
    }

    @Transactional
    @Override
    public BeerOrder validateOrder(UUID beerOrderId, boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.getById(beerOrderId);
        if(isValid){
            sendEvent(beerOrder,BeerOrderEventEnum.VALIDATON_PASSED);

            beerOrder = beerOrderRepository.getById(beerOrderId);

            sendEvent(beerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        }
        else
            sendEvent(beerOrder,BeerOrderEventEnum.VALIDATION_ERROR);
        return beerOrder;
    }

    private void sendEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine <BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);
        Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(eventEnum).setHeader(BEER_ORDER_ID_HEADER, beerOrder.getId().toString()).build();
        sm.sendEvent(message);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = factory.getStateMachine(beerOrder.getId());
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<BeerOrderStatusEnum, BeerOrderEventEnum>>() {
            @Override
            public void apply(StateMachineAccess<BeerOrderStatusEnum, BeerOrderEventEnum> sma) {
                sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                sma.resetStateMachine(new DefaultStateMachineContext(beerOrder.getOrderStatus(),null,null,null));
            }
        });
        sm.start();
        return sm;
    }


}
