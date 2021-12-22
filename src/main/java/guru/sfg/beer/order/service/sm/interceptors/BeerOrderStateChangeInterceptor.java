package guru.sfg.beer.order.service.sm.interceptors;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state, Message<BeerOrderEventEnum> message, Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition, StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine, StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> rootStateMachine) {
        Optional.ofNullable(message).ifPresent(new Consumer<Message<BeerOrderEventEnum>>() {
            @Override
            public void accept(Message<BeerOrderEventEnum> msg) {
                Optional.ofNullable((UUID)msg.getHeaders().getOrDefault(BeerOrderManagerImpl.BEER_ORDER_ID,null)).ifPresent(new Consumer<UUID>() {
                    @Override
                    public void accept(UUID beerOrderId) {
                        BeerOrder beerOrder = beerOrderRepository.getById(beerOrderId);
                        beerOrder.setOrderStatus(state.getId());
                        beerOrderRepository.save(beerOrder);
                    }
                });
            }
        });
    }
}
