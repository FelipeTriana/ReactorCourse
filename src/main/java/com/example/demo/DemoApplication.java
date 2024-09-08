package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Juan")
				.doOnNext(System.out::println); //la referencia al metodo abrevia la funcion lambda

		//Es lo mismo ejecutar la tarea en el subscribe que en el doOnNext
		//Ojo: La tarea se esta suscribiendo en el evento doOnNext
		nombres.subscribe(log::info);
	}
}
