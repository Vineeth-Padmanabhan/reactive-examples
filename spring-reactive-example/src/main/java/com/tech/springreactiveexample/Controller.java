package com.tech.springreactiveexample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.FluentProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class Controller {
	
	// @EndpointInject(uri = "geocoder:address:current")
	 private FluentProducerTemplate producer;
	 
	 @Autowired
	 WebClient client;

	@GetMapping("/test")
	public Flux<String> getResponse() throws InterruptedException {
		List<String> list = List.of("abc");
		//String where = producer.request(String.class);
		Flux<String> returnFlux = Flux.fromIterable(list);
		return returnFlux
		.flatMap(a -> {
			return blockingCall(a);
		}).doOnComplete(()-> {
			System.out.println( " - " + Thread.currentThread().getName());
			Controller.printThreadStack();
		});
		
	}

	private Mono<String> blockingCall(String a) {
		return Mono.just(client.get().uri("/get/{a}",a).retrieve().bodyToMono(String.class).subscribeOn(Schedulers.newBoundedElastic(1, 10000, "urscheduler")).block());
	}

	@GetMapping("/get/{value}")
	public String getResponseValue(@PathVariable("value") String value) throws InterruptedException {

		Map<String, String> map = new HashMap<String, String>();

		map.put("abc", "vineeth");
		map.put("xyz", "testing");
		//Controller.printThreadStack();
		return map.getOrDefault(value, "Random");
	}

	public static void printThreadStack() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		
		List<Thread> sortedThreads = new ArrayList<Thread>(threadSet);
        Collections.sort(sortedThreads,Comparator.comparing(Thread::getName));
		
		
		// iterating over the threads to get the names of
		// all the active threads
		for (Thread x : sortedThreads) {
			System.out.println(x.getName());
		}
	}
}
