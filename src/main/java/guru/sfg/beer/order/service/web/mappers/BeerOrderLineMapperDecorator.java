package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.beer.order.service.services.beer.BeerService;
import guru.sfg.brewery.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

public class BeerOrderLineMapperDecorator implements BeerOrderLineMapper{

    private BeerService beerService;

    private BeerOrderLineMapper mapper;

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }
    @Autowired
    public void setMapper(BeerOrderLineMapper mapper) {
        this.mapper = mapper;
    }


    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerDto beerDto = beerService.getBeerByUpc(line.getUpc());
        BeerOrderLineDto beerOrderLineDto = mapper.beerOrderLineToDto(line);
        beerOrderLineDto.setBeerName(beerDto.getBeerName());
        beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return mapper.dtoToBeerOrderLine(dto);
    }
}
