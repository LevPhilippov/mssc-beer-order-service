package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.interceptors.BeerOrderStateChangeInterceptor;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public void validateOrder(UUID beerOrderId, boolean isValid) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

        beerOrderOptional.ifPresentOrElse(beerOrder ->{
            if(isValid){
                sendEvent(beerOrder,BeerOrderEventEnum.VALIDATON_PASSED);
                beerOrder = beerOrderRepository.findById(beerOrderId).orElseThrow(() -> new RuntimeException("Something went wrong due to validation process"));
                sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
            }
            else{
                sendEvent(beerOrder,BeerOrderEventEnum.VALIDATION_ERROR);
            }
        },()->{log.error("Error with validation process of beerOrder {}", beerOrderId); });
    }
    @Transactional
    @Override
    public void allocateOrder(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        allocatedOrderOptional.ifPresentOrElse(beerOrder->{
            updateAllocatedQty(beerOrderDto);
            sendEvent(beerOrder,BeerOrderEventEnum.ALLOCATION_SUCCESS);
        },()->{log.error("Error occurred during allocation success SM process of beerOrder {}", beerOrderDto.getId()); });
    }
    @Transactional
    @Override
    public void allocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        allocatedOrderOptional.ifPresentOrElse(beerOrder->{
            updateAllocatedQty(beerOrderDto);
            sendEvent(beerOrder,BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        },()->{log.error("Error occurred during allocation inventory pending SM process of beerOrder {}", beerOrderDto.getId()); });
    }

    @Override
    public void allocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        allocatedOrderOptional.ifPresentOrElse(beerOrder->{
            sendEvent(beerOrder,BeerOrderEventEnum.ALLOCATION_FAILED);
        },()->{log.error("Error occurred during allocation error SM process of beerOrder {}", beerOrderDto.getId()); });
    }

    private BeerOrder updateAllocatedQty(BeerOrderDto dto){
        BeerOrder allocatedOrder = beerOrderRepository.getById(dto.getId());
        allocatedOrder.getBeerOrderLines().forEach(bol -> dto.getBeerOrderLines()
                .forEach(bolDto -> {
                    if(bol.getId().equals(bolDto.getId()))
                        bol.setQuantityAllocated(bolDto.getQuantityAllocated());
        }));
        return beerOrderRepository.saveAndFlush(allocatedOrder);
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
