package com.example.demo;

import com.example.demo.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploToString();

	}

	public void ejemploToString() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Anuar"));
		usuariosList.add(new Usuario("Diego", "Ciruela"));
		usuariosList.add(new Usuario("Zazza", "Kepaza"));
		usuariosList.add(new Usuario("Juan", "Planeta"));

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.split(" ")[0].equalsIgnoreCase("zazza")) {
						return Mono.just(usuario); //Cada elemento que se emite se convierte a un Mono o Flux y por dentro el se aplana y se une al mismo stream del flujo de salida
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					return usuario.split(" ")[0].toLowerCase();
				})
				.subscribe(u -> log.info(u.toString()));


	}

	public void ejemploFlatMap() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Anuar");
		usuariosList.add("Diego Ciruela");
		usuariosList.add("Zazza Kepaza");
		usuariosList.add("Juan Planeta");

	Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0], nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("zazza")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
			.subscribe(u -> log.info(u.toString()));


	}

	public void ejemploIterable() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Anuar");
		usuariosList.add("Diego Ciruela");
		usuariosList.add("Zazza Kepaza");
		usuariosList.add("Juan Planeta");
		Flux<String> nombres = Flux.fromIterable(usuariosList);

		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0], nombre.split(" ")[1]))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("zazza"))
				.doOnNext(e -> {
					if (e == null){
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(e.getNombre() + " " + e.getApellido());
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		nombres.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito");
					}
				});

		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito");
					}
				});
	}
}
