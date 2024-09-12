package com.example.demo;

import com.example.demo.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

//Imutabilidad: Cada vez que aplicamos un operador a un flujo se van creando nuevas instancias pero nunca se modifica el flujo original
//Lo anterior se puede evidenciar en el ejemplo a continuacion, donde se aplica el operador map y filter a un flujo de nombres
@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//El operador filter es para filtrar los elementos que cumplan con la condicion dada, evalúa una condicion BOOLEANA
		Flux<String> nombres = Flux.just("Andres Guzman","Pedro Anuar","Diego Ciruela","Zazza Kepaza","Juan Planeta");

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
