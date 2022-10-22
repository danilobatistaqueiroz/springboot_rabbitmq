package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HeaderExchangeTest {

    @Test
    void test() {
        //header (combinacao): order-paid + premium
        //Coupon.v1.on-order-paid-premium.generate
        //Coupon Service (Consumer)
        
        //header (qualquer): order-paid, order-canceled
        //History.v1.on-order-event.generate
        //History Service (Consumer)

        //headers exchange: Order.v1.events
        
        //Order Service (producer)
        
        
        
        
    }

}
