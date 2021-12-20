package guru.sfg.beer.order.service.services.beer;

import java.util.UUID;

public interface BeerService {
    BeerDto getBeerDto(UUID beerId);
}
