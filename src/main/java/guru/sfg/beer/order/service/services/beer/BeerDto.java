package guru.sfg.beer.order.service.services.beer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeerDto {

    private UUID id;

    private String beerName;

    private String beerStyle;

    private String upc;

    private Integer quantityOnHand;
    private Integer quantityToBrew;


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal price;

    private Integer version;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
    private OffsetDateTime createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
    private OffsetDateTime lastModifiedDate;
}
