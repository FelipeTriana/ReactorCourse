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

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Zazza","Juan")
				.doOnNext(e -> {
					if (e.isEmpty()){
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(e);
				});

		//Si vemos la especificacion de subscribe, podemos ver que recibe 3 parametros, 2 Consumer y un Runnable
		//Consumer: Es cualquier tarea que queramos implementar usando expresiones lambda
		//Runnable: Es un evento que se ejecuta al finalizar el observable, osea, se ejecuta en el onComplete
		nombres.subscribe(log::info,
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito");
					}
				});
	}
}
