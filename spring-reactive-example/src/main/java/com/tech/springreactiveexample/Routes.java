package com.tech.springreactiveexample;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.throttling.ThrottlingInflightRoutePolicy;
import org.springframework.stereotype.Component;

@Component
public class Routes extends RouteBuilder {

	@Override
	public void configure() throws Exception {

        
		from("direct:inbox2")
		.streamCaching()
		.convertBodyTo(String.class)
        .delay(1000)
        .log("direct:inbox2 - Camel routing to Reactive Streams: ${body}")
        .bean(Controller.class,"printThreadStack")
        .bean(Routes.class, "logThreadName");
		
		from("direct:inbox3")
		.streamCaching()
		.convertBodyTo(String.class)
        .delay(1000)
        .log("direct:inbox3 - Camel routing to Reactive Streams: ${body}")
        .bean(Routes.class, "logThreadName");
		
		// use in flight route policy to throttle how many messages to take in by Camel
        // to control the back-pressure to only allow at most 20 in-flight messages
        ThrottlingInflightRoutePolicy inflight = new ThrottlingInflightRoutePolicy();
        inflight.setMaxInflightExchanges(20);
        // start Camel consumer again when we are down or below 25% of max
        inflight.setResumePercentOfMax(25);

        from("seda:inbox1").routePolicy(inflight)
            // use a little delay as otherwise Camel is to fast and the inflight throttler cannot react so precisely
            // and it also spread the incoming messages more evenly than a big burst
            .delay(100)
            .log("Camel routing to Reactive Streams: ${body}");
            //.to("reactive-streams:inbox");
        
        
        //Consumer BackPressure
        from("reactive-streams:inbox?maxInflightExchanges=5&concurrentConsumers=5")
        // use a little delay so us humans can follow what happens
        .delay(constant(10))
        .log("Processing message ${body}");
        
        
        
        
        
	}
	
	public void logThreadName(String context) {
		System.out.println(context + " " +Thread.currentThread().getName());
	}

}
