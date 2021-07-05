package com.tech.springreactiveexample;

import javax.annotation.PostConstruct;

import org.apache.camel.CamelContext;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component()
public class FileComponent {
	
	@Autowired
	CamelContext context;
	
	@PostConstruct
    private void postConstruct() {
		CamelReactiveStreamsService rsCamel = CamelReactiveStreams.get(context);
		
		// use stream engine to subscribe from the publisher
        // where we filter out the big numbers which is logged
        	Flux.merge(rsCamel.from("file:C:/Demo/inbox"))
            // call the direct:inbox Camel route from within this flow
        	
            .doOnNext(e -> rsCamel.to("direct:inbox2", e))
           
           
            // let Camel also be subscriber by the endpoint direct:camel
            .subscribe(rsCamel.subscriber("direct:inbox3"));
       
    }

}
