package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;

public interface BeerOrderManeger {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
}
