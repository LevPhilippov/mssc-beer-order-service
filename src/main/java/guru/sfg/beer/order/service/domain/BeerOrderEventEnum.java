package guru.sfg.beer.order.service.domain;

public enum BeerOrderEventEnum {
    VALIDATE_ORDER, VALIDATON_PASSED, VALIDATION_ERROR,
    ALLOCATE_ORDER,ALLOCATION_SUCCESS, ALLOCATION_NO_INVENTORY, ALLOCATION_FAILED,
    BEERORDER_PICK_UP,BEERORDER_PICKED_UP_ERROR
}
