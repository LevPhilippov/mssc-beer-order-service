package guru.sfg.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllocationOrderCompensatingRequest {
    static final long serialVersionUID = -9034128520735916866L;
    private UUID beerOrderId;
}
