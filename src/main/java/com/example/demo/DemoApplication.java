package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

//Consumer: Es cualquier tarea que queramos implementar usando expresiones lambda

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","","Juan")
				.doOnNext(e -> {
					if (e.isEmpty()){
						throw new RuntimeException("Nombres no pueden ser vacios"); //El error interrumpe el flujo
					}
					System.out.println(e);
				});

		//Ahora en el suscribe se puede manejar el error que ocurra en el evento doOnNext
		nombres.subscribe(log::info,
				error -> log.error(error.getMessage()));
	}
}
