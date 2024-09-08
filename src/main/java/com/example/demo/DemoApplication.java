package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		//El flujo "nombres" emite los elementos "Andres", "Pedro", "Diego" y "Juan"
		//El subject es "nombres"
		//El observador es la funcion lambda que se pasa al metodo doOnNext. Esta funcion lambda se encarga de procesar cada elemento emitido por el flujo nombres.
		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Juan")
				.doOnNext(elemento -> System.out.println(elemento));

		//La suscripción es necesaria para que el observador reciba los elementos emitidos por el sujeto, pero en este caso, no es parte del observador en sí, sino del flujo que conecta ambos.
		//Cuando nos suscribimos estamos observando, el observador es quien maneja tambien los errores que se puedan presentar.
		nombres.subscribe();
	}
}
