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
		ejemploFlatMap();

	}

	//El fatlMap es muy parecido al map, pero en lugar de devolver un objeto, devuelve un flujo de objetos
	/* El .faltMap aplana el flujo de objetos, osea que si por ejemplo en su interior creamos Flux de objetos, este los aplana a todos en un solo flujo Flux,
	   con .map en cambio, se generaria un flujo de flujos de objetos tipo Flux<Flux<Objeto>>
	*/
	public void ejemploFlatMap() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Anuar");
		usuariosList.add("Diego Ciruela");
		usuariosList.add("Zazza Kepaza");
		usuariosList.add("Juan Planeta");

	Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0], nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> { //Se reemplazo el filter por el flatMap
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
		//fromIterable genera un flujo a apartir de un objeto iterable
		Flux<String> nombres = Flux.fromIterable(usuariosList);//Flux.just("Andres Guzman","Pedro Anuar","Diego Ciruela","Zazza Kepaza","Juan Planeta");

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
