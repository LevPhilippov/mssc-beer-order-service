package guru.sfg.brewery.model.events;


import guru.sfg.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateOrderRequest implements Serializable {

    static final long serialVersionUID = 7087048810485715162L;
    private BeerOrderDto beerOrderDto;
}
