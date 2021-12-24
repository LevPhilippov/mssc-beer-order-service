package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;

@Service
@ConfigurationProperties("sfg.brewery")
@Slf4j
public class BeerServiceImpl implements BeerService {

    private RestTemplate restTemplate;

    public static final String beerServiceGetByIdUrl ="/api/v1/beer/";
    public static final String beerServiceGetByUpcUrl ="/api/v1/beer/upc/";

    private String beerServiceHostUrl;

    public void setBeerServiceHostUrl(String beerServiceHostUrl) {
        this.beerServiceHostUrl = beerServiceHostUrl;
    }

    @PostConstruct
    private void init(){
        log.debug("Injected beerServiceHostUrl property is: {}", beerServiceHostUrl);
    }

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public BeerDto getBeerDto(UUID beerId) {
        ResponseEntity<BeerDto> exchange = restTemplate.exchange(beerServiceHostUrl + beerServiceGetByIdUrl+"{beerId}", HttpMethod.GET, RequestEntity.EMPTY, new ParameterizedTypeReference<BeerDto>() {
        }, beerId);
        return Objects.requireNonNull(exchange).getBody();
    }

    @Override
    public BeerDto getBeerByUpc(String upc) {
        ResponseEntity<BeerDto> responseEntity = restTemplate.getForEntity(beerServiceHostUrl+beerServiceGetByUpcUrl+"{upc}",BeerDto.class,upc);
        return Objects.requireNonNull(responseEntity).getBody();
    }
}

