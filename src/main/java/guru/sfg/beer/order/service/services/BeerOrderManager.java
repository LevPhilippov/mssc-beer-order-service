package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.brewery.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void validateOrder(UUID beerOrderId, boolean isValid);

    void allocateOrder(BeerOrderDto beerOrderDto);

    void allocationPendingInventory(BeerOrderDto beerOrderDto);

    void allocationFailed(BeerOrderDto beerOrderDto);

    void cancelOrder(UUID orderId);

    void pickUp(UUID beerOrderId);
}
