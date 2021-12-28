package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.brewery.model.CustomerDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    @Mapping(source = "customerName", target = "name")
    CustomerDto customerToCustomerDto(Customer customer);

    @InheritInverseConfiguration
    Customer customerDtoToCustomer(CustomerDto dto);
}
