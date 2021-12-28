package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.web.mappers.CustomerMapper;
import guru.sfg.brewery.model.CustomerDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@SpringBootTest
public class CustomerMapperTest {

    @Autowired
    CustomerMapper mapper;

    @Test
    void customerMapperTest() {
        String name = "test_customer" ;
        UUID id = UUID.randomUUID();
        Timestamp created = Timestamp.valueOf(LocalDateTime.now());
        Timestamp updated = Timestamp.valueOf(LocalDateTime.now());
        Long version = 1L;
        Customer test_customer = Customer.builder()
                .customerName("test_customer")
                .id(id)
                .createdDate(created)
                .lastModifiedDate(updated)
                .version(version)
                .apiKey(id)
                .build();

        CustomerDto customerDto = mapper.customerToCustomerDto(test_customer);

        Assertions.assertEquals(id,customerDto.getId());
        Assertions.assertEquals(version,Integer.toUnsignedLong(customerDto.getVersion()));
        Assertions.assertEquals(OffsetDateTime.of(created.toLocalDateTime(), ZoneOffset.UTC),customerDto.getCreatedDate());


    }
}
