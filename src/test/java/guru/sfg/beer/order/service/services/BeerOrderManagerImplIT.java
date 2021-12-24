package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.beer.BeerServiceImpl;
import guru.sfg.brewery.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BeerOrderManagerImplIT {

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8083))
            .build();

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    Customer testCustomer;

    UUID beerID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        this.testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
    }


    @Test
    void newToAllocatedTest() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().upc("1234567890").id(beerID).build();
        wm1.stubFor(get(BeerServiceImpl.beerServiceGetByUpcUrl+"1234567890").willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder();
        BeerOrder savedBeerOrder =  beerOrderManager.newBeerOrder(beerOrder);

        assertNotNull(savedBeerOrder);

        await().untilAsserted(()->{
            BeerOrder assertedOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, assertedOrder.getOrderStatus());
        });

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder2.getOrderStatus());
        savedBeerOrder2.getBeerOrderLines().forEach(line->{
            assertEquals(line.getOrderQuantity(),line.getQuantityAllocated());
        });
    }

    @Test
    void newToValidationErrorTest() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().upc("1234567890").id(beerID).build();
        wm1.stubFor(get(BeerServiceImpl.beerServiceGetByUpcUrl+"1234567890").willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("validation-fail");
        BeerOrder savedBeerOrder =  beerOrderManager.newBeerOrder(beerOrder);

        assertNotNull(savedBeerOrder);

        await().untilAsserted(()->{
            BeerOrder assertedOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, assertedOrder.getOrderStatus());
        });
    }

    @Test
    void newToPickedUpTest() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().upc("1234567890").id(beerID).build();
        wm1.stubFor(get(BeerServiceImpl.beerServiceGetByUpcUrl+"1234567890").willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder savedBeerOrder =  beerOrderManager.newBeerOrder(createBeerOrder());

        assertNotNull(savedBeerOrder);

        await().untilAsserted(()->{
            BeerOrder assertedOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, assertedOrder.getOrderStatus());
        });

        beerOrderManager.pickUp(savedBeerOrder.getId());

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertEquals(BeerOrderStatusEnum.PICKED_UP, savedBeerOrder2.getOrderStatus());

    }

    private BeerOrder createBeerOrder() {
        BeerOrder testOrder = BeerOrder.builder().customer(testCustomer).build();
        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .beerId(beerID)
                .beerOrder(testOrder)
                .orderQuantity(1)
                .upc("1234567890")
                .build();
        Set<BeerOrderLine> beerOrderLines = new HashSet<>();
        beerOrderLines.add(beerOrderLine);
        testOrder.setBeerOrderLines(beerOrderLines);
        return testOrder;
    }
}