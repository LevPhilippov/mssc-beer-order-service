/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package guru.sfg.beer.order.service.web.controllers;

import guru.sfg.beer.order.service.services.BeerOrderService;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderPagedList;
import guru.sfg.brewery.model.CustomerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.UUID;

@RequestMapping("/api/v1/customers")
@RestController
public class BeerOrderController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final BeerOrderService beerOrderService;

    public BeerOrderController(BeerOrderService beerOrderService) {
        this.beerOrderService = beerOrderService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<CustomerDto>> customers(@RequestParam(name = "pageSize", required = false) Integer pageSize,
                                                       @RequestParam(name = "pazeNumber", required = false) Integer pageNumber){
        Page<CustomerDto> customers = beerOrderService.customers(pageSize, pageNumber);
        return new ResponseEntity(customers,HttpStatus.OK);
    }

    @GetMapping(path = "/{customerId}",produces = "application/json")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable("customerId") UUID customerId){
        CustomerDto customerDto = beerOrderService.findCustomerById(customerId);
        return new ResponseEntity<>(customerDto, HttpStatus.OK);
    }

    @GetMapping("/{customerId}/orders")
    public BeerOrderPagedList listOrders(@PathVariable("customerId") UUID customerId,
                                         @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize){

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return beerOrderService.listOrders(customerId, PageRequest.of(pageNumber, pageSize));
    }

    @PostMapping("/{customerId}/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public BeerOrderDto placeOrder(@PathVariable("customerId") UUID customerId, @RequestBody BeerOrderDto beerOrderDto){
        return beerOrderService.placeOrder(customerId, beerOrderDto);
    }

    @GetMapping("/{customerId}/orders/{orderId}")
    public BeerOrderDto getOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        return beerOrderService.getOrderById(customerId, orderId);
    }

    @PutMapping("/{customerId}/orders/{orderId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        beerOrderService.pickupOrder(customerId, orderId);
    }
}
