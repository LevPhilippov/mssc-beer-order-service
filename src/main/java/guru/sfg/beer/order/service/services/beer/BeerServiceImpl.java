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

    private String beerServiceApiUrl ="/api/v1/beer/{beerId}";

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
        ResponseEntity<BeerDto> exchange = restTemplate.exchange(beerServiceHostUrl + beerServiceApiUrl, HttpMethod.GET, RequestEntity.EMPTY, new ParameterizedTypeReference<BeerDto>() {
        }, beerId);
        return Objects.requireNonNull(exchange).getBody();
    }
}

