package com.example.demo;

import com.example.demo.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

//Si por ejemplo realizamos una consulta a la base de datos obtenemos una lista de objetos, podemos convertir ese listado en un flujo de datos con el metodo fromIterable
//Hay bases de datos NoSql que son reactivas y ya devuelven un flujo de datos en vez de una lista, por ejemplo MongoDB
@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
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
