package com.example.demo;

import com.example.demo.models.Comentarios;
import com.example.demo.models.Usuario;
import com.example.demo.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploContraPresionManual();

	}

	//Otra forma mas sencilla de aplicar contrapresion es con el metodo limitRate
	public void ejemploContraPresion() {
		Flux.range(1, 10)
				.log()
				.limitRate(3) //Limitamos la cantidad de elementos que se pueden emitir por segundo
				.subscribe();
	}

	//A continuacion la forma manual de aplicar contrapresion en un flujo de datos por medio de la implementacion del Subscriber
	public void ejemploContraPresionManual() {
		Flux.range(1, 10)
				.log()
				.subscribe(new Subscriber<Integer>() {
					private Subscription s;
					private Integer limite = 2;
					private Integer consumido = 0;

					@Override
					public void onSubscribe(Subscription s) {
						this.s = s;       //Recordar que el subscriber es el observador que requiere una subscripcion para ejecutar alguna tarea dentro del onNext cada vez que recibe un elemento del flujo
						s.request(limite); //Solicitamos la cantidad de elementos definida en "limite"
					}

					@Override
					public void onNext(Integer integer) {
						log.info(integer.toString());
						consumido++;
						if (consumido == limite) {
							consumido = 0;
							s.request(limite);   //Se procesaran bloques de dos elementos, request es el metodo que solicita mas elementos al flujo
						}
					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {

					}
				});
	}

	/*Esta es otra forma de crear y emitir nuestro Flux Observable a nuestra medida por medio del metodo create y el emitter
	donde cada objeto a emitir se registra con .next*/
	public void ejemploIntervalDesdeCreate() throws InterruptedException {

		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(contador++);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}

					if (contador == 5){
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}
				}
			}, 1000, 1000);
		})
				.subscribe(next -> log.info(next.toString()),   //Aqui se puede insertar el comportamiento de un doOnNext, ademas de control del error y el onComplete
						error -> log.error(error.getMessage()),
						() -> log.info("Hemos terminado"));
	}

	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.map(i -> "Hola" + i)
				.doOnNext(s -> log.info(s))
				.subscribe();

		latch.await();
	}

	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> range = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		range.subscribe(); //No muestra nada en consola porque el flujo de datos se esta ejecutando en un hilo diferente

		Thread.sleep(13000); //Como esto bloquea el hilo se mostrara en consola
	}

	public void ejemploInterval(){
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(retraso, (ra, re) -> ra)  //Solamente emitiremos el rango ya que el delay se combinara con el rango
				.doOnNext(i -> log.info(i.toString())) //Esto se podria hacer en el subscribe
				.blockLast();   //Subscribe al flujo con bloqueo, bloquea hasta que se haya emitido el ultimo elemento.
	}

	public void ejemploZipWithRangos(){

		Flux.just(1,2,3,4)
				.map( i -> (i * 2))
				.zipWith(Flux.range(0,4), (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));

	}

	public void ejemploUsuarioComentariosZipWithForma2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal?");
			comentarios.addComentario("Mañana nos vemos");
			return comentarios;
		});

		Mono <UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosMono) //Con esta sintaxis zipWith retorna una tupla con los dos elementos, pero al aplicar map tendremos el mismo resultado
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u, c);
				});

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe")); //Dentro de fromCallable se puede hacer llamado a un metodo y el envolvera el retorno en un Mono
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal?");
			comentarios.addComentario("Mañana nos vemos");
			return comentarios;
		});

		//Queremos crear un flujo de datos que emita un usuario y sus comentarios (Combinacion de los dos flujos anteriores con zipWith)
		Mono <UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosMono, (usuario, comentarios) -> new UsuarioComentarios(usuario, comentarios));

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe")); //Dentro de fromCallable se puede hacer llamado a un metodo y el envolvera el retorno en un Mono
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal?");
			comentarios.addComentario("Mañana nos vemos");
			return comentarios;
		});

		//Queremos crear un flujo de datos que emita un usuario y sus comentarios (Combinacion de los dos flujos anteriores con flatMap)
		usuarioMono.flatMap( u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Anuar"));
		usuariosList.add(new Usuario("Diego", "Ciruela"));
		usuariosList.add(new Usuario("Zazza", "Kepaza"));
		usuariosList.add(new Usuario("Juan", "Planeta"));

		Flux.fromIterable(usuariosList)
				.collectList() //Convierte el Flux en un Mono y el nuevo Mono almacena una lista asi: Mono<List<Usuario>>
				.subscribe(lista -> log.info(lista.toString()));

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
						return Mono.empty(); //Ojo: flatMap aplana el flujo de datos y retorna todo en un solo flujo Flux
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
