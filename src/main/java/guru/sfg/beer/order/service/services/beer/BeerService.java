package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;

import java.util.UUID;

public interface BeerService {
    BeerDto getBeerDto(UUID beerId);
    BeerDto getBeerByUpc(String upc);
}
