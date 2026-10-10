package enemy;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.CharacterBody2D;
import godot.api.Node;
import godot.api.Node2D;
import godot.api.PackedScene;
import godot.api.ResourceLoader;
import godot.core.Color;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.global.GD;

/**
 * Enemigo Horno a distancia: camina hacia la papa hasta quedar a "rangoDisparo",
 * se frena, abre la puerta (animación "ataque") y escupe una bola de fuego.
 * Escena: res://Scenes/enemigo.tscn  (necesita un hijo "AnimatedSprite2D")
 */
@Script
public class Horno extends CharacterBody2D {

	@Visible public double vidaMax = 20.0;
	@Visible public double velocidad = 220.0;
	@Visible public double danioProyectil = 12.0;
	@Visible public double rangoDisparo = 1000.0;   // a esta distancia se frena y dispara
	@Visible public double cadenciaDisparo = 4.0;   // segundos entre disparos
	@Visible public int frameDelDisparo = 2;        // frame de "ataque" en el que sale la bala

	private static final String RUTA_PROYECTIL = "res://Scenes/proyectil_enemigo.tscn";

	private double vida;
	private double cooldownDisparo = 0.0;
	private double tiempoFlash = 0.0;
	private boolean muerto = false;
	private boolean atacando = false;
	private boolean disparoHecho = false;

	private AnimatedSprite2D sprite;
	private PackedScene escenaProyectil;

	private final StringName grupoPlayer = new StringName("player");
	private final StringName grupoEnemigos = new StringName("enemigos");

	private final StringName animWalkDown = new StringName("walk_down");
	private final StringName animWalkUp = new StringName("walk_up");
	private final StringName animWalkLeft = new StringName("walk_left");
	private final StringName animWalkRight = new StringName("walk_right");
	private final StringName animAtaque = new StringName("ataque");

	private StringName animCaminar = animWalkDown;

	private final Color colorNormal = new Color(1.0, 1.0, 1.0, 1.0);
	private final Color colorGolpe = new Color(1.0, 0.35, 0.35, 1.0);

	@Register
	@Override
	public void _ready() {
		sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
		vida = vidaMax;
		addToGroup(grupoEnemigos);

		escenaProyectil = (PackedScene) ResourceLoader.load(RUTA_PROYECTIL);
		if (escenaProyectil == null) {
			GD.printErr("Horno: no se pudo cargar " + RUTA_PROYECTIL + " (el horno no va a disparar).");
		}
		// Arranca en un punto distinto de la recarga para que no disparen todos a la vez
		cooldownDisparo = Math.random() * cadenciaDisparo;

		sprite.play(animCaminar);
	}

	@Register
	@Override
	public void _physicsProcess(double delta) {
		if (muerto) return;

		if (cooldownDisparo > 0) cooldownDisparo -= delta;
		actualizarFlash(delta);

		if (atacando) {
			procesarDisparo();
			return;
		}

		Node2D papa = buscarPapa();
		if (papa == null) {
			setVelocity(new Vector2(0.0, 0.0));
			sprite.stop();
			return;
		}

		Vector2 haciaPapa = papa.getGlobalPosition().minus(getGlobalPosition());
		double distancia = haciaPapa.length();
		Vector2 dir = haciaPapa.normalized();
		orientar(dir);

		if (distancia > rangoDisparo) {
			// Lejos: camina hacia la papa
			setVelocity(dir.times(velocidad));
			moveAndSlide();
			sprite.play(animCaminar);
		} else if (cooldownDisparo <= 0) {
			// En rango y recargado: dispara
			empezarAtaque();
		} else {
			// En rango pero recargando: se queda quieto mirando a la papa
			setVelocity(new Vector2(0.0, 0.0));
			sprite.play(animCaminar);
			sprite.stop();
		}
	}

	private boolean ejeHorizontal = false;

	private void orientar(Vector2 dir) {
		double ax = Math.abs(dir.getX());
		double ay = Math.abs(dir.getY());
		if (ejeHorizontal && ay > ax * 1.3) ejeHorizontal = false;
		else if (!ejeHorizontal && ax > ay * 1.3) ejeHorizontal = true;

		StringName nueva;
		if (ejeHorizontal) nueva = dir.getX() < 0 ? animWalkLeft : animWalkRight;
		else nueva = dir.getY() < 0 ? animWalkUp : animWalkDown;

		if (nueva != animCaminar) {
			animCaminar = nueva;
			GD.print("Horno anim -> " + animCaminar);
		}
	}

	// ------------------------------------------------------------------
	// Disparo
	// ------------------------------------------------------------------

	private void empezarAtaque() {
		atacando = true;
		disparoHecho = false;
		setVelocity(new Vector2(0.0, 0.0));
		sprite.play(animAtaque); // "ataque" no hace loop
	}

	private void procesarDisparo() {
		// La bala sale en el frame del fuego, apuntando a donde esté la papa en ese momento
		if (!disparoHecho && sprite.getFrame() >= frameDelDisparo) {
			disparoHecho = true;
			Node2D papa = buscarPapa();
			if (papa != null) lanzarProyectil(papa);
		}

		if (!sprite.isPlaying()) {
			atacando = false;
			cooldownDisparo = cadenciaDisparo;
			sprite.play(animCaminar);
		}
	}

	private void lanzarProyectil(Node2D papa) {
		if (escenaProyectil == null) return;

		Node inst = escenaProyectil.instantiate();
		if (!(inst instanceof ProyectilEnemigo)) {
			GD.printErr("Horno: la raíz de proyectil_enemigo.tscn no tiene el script ProyectilEnemigo.");
			inst.queueFree();
			return;
		}
		ProyectilEnemigo bala = (ProyectilEnemigo) inst;
		bala.direccion = papa.getGlobalPosition().minus(getGlobalPosition()).normalized();
		bala.danio = danioProyectil;

		getParent().addChild(bala);
		bala.setGlobalPosition(getGlobalPosition());
	}

	private Node2D buscarPapa() {
		Node n = getTree().getFirstNodeInGroup(grupoPlayer);
		if (n instanceof Node2D) return (Node2D) n;
		return null;
	}

	// ------------------------------------------------------------------
	// Vida
	// ------------------------------------------------------------------

	/** Lo llaman los proyectiles de la papa. */
	@Register
	public void recibirDanio(double cantidad) {
		if (muerto) return;
		vida -= cantidad;
		tiempoFlash = 0.1;
		sprite.setModulate(colorGolpe);
		if (vida <= 0) morir();
	}

	private void actualizarFlash(double delta) {
		if (tiempoFlash > 0) {
			tiempoFlash -= delta;
			if (tiempoFlash <= 0) sprite.setModulate(colorNormal);
		}
	}

	private void morir() {
		muerto = true;
		// Aquí después se puede soltar XP / material, sumar calor a la papa, etc.
		queueFree();
	}

	/** Lo usa el SpawnerEnemigos para que salgan más duros con el tiempo. */
	public void escalar(double multiplicador) {
		vidaMax *= multiplicador;
		vida = vidaMax;
		danioProyectil *= multiplicador;
	}
}
