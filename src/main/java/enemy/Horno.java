package enemy;

import Character.Potato;
import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.Area2D;
import godot.api.CharacterBody2D;
import godot.api.Node;
import godot.api.Node2D;
import godot.core.Color;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.core.VariantArray;

@Script
public class Horno extends CharacterBody2D {

	@Visible public double vidaMax = 30.0;
	@Visible public double velocidad = 60.0;
	@Visible public double danio = 10.0;
	@Visible public double tiempoEntreAtaques = 0.8;
	@Visible public int frameDelGolpe = 2;

	private double vida;
	private double cooldownAtaque = 0.0;
	private double tiempoFlash = 0.0;
	private boolean muerto = false;
	private boolean atacando = false;
	private boolean golpeHecho = false;

	private AnimatedSprite2D sprite;
	private Area2D hitbox;

	private final StringName grupoPlayer = new StringName("player");
	private final StringName grupoEnemigos = new StringName("enemigos");

	// --- NUEVAS ANIMACIONES DIRECCIONALES ---
	private final StringName animWalkDown = new StringName("walk_down");
	private final StringName animWalkUp = new StringName("walk_up");
	private final StringName animWalkLeft = new StringName("walk_left");
	private final StringName animWalkRight = new StringName("walk_right");
	private final StringName animAtaque = new StringName("ataque");

	// Guardamos la última dirección para cuando ataque
	private StringName ultimaAnimacion = animWalkDown;

	private final Color colorNormal = new Color(1.0, 1.0, 1.0, 1.0);
	private final Color colorGolpe = new Color(1.0, 0.35, 0.35, 1.0);

	@Register
	@Override
	public void _ready() {
		sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
		hitbox = (Area2D) getNode(new NodePath("Hitbox"));
		vida = vidaMax;
		addToGroup(grupoEnemigos);
		if (sprite != null) sprite.play(ultimaAnimacion);
	}

	@Register
	@Override
	public void _physicsProcess(double delta) {
		if (muerto) return;

		if (cooldownAtaque > 0) cooldownAtaque -= delta;
		actualizarFlash(delta);

		if (atacando) {
			procesarAtaque();
			return;
		}

		Node2D papa = buscarPapa();
		if (papa == null) {
			setVelocity(new Vector2(0.0, 0.0));
			if (sprite != null) sprite.stop(); // Frena la animación si no hay papa
			return;
		}

		if (cooldownAtaque <= 0 && papaEnHitbox() != null) {
			empezarAtaque();
			return;
		}

		double diffX = papa.getGlobalPosition().getX() - getGlobalPosition().getX();
		double diffY = papa.getGlobalPosition().getY() - getGlobalPosition().getY();
		Vector2 direccion = new Vector2(diffX, diffY).normalized();

		setVelocity(new Vector2(direccion.getX() * velocidad, direccion.getY() * velocidad));
		moveAndSlide();

		if (sprite != null) {
			// Lógica para decidir a dónde mira basado en su vector de movimiento
			if (Math.abs(direccion.getX()) > Math.abs(direccion.getY())) {
				if (direccion.getX() < 0) {
					ultimaAnimacion = animWalkLeft;
				} else {
					ultimaAnimacion = animWalkRight;
				}
			} else {
				if (direccion.getY() < 0) {
					ultimaAnimacion = animWalkUp;
				} else {
					ultimaAnimacion = animWalkDown;
				}
			}

			sprite.setFlipH(false); // Apagamos el flip porque ya tenemos sprites para cada lado
			sprite.play(ultimaAnimacion);
		}
	}

	private void empezarAtaque() {
		atacando = true;
		golpeHecho = false;
		setVelocity(new Vector2(0.0, 0.0));
		if (sprite != null) sprite.play(animAtaque);
	}

	private void procesarAtaque() {
		if (sprite == null) return;

		if (!golpeHecho && sprite.getFrame() >= frameDelGolpe) {
			golpeHecho = true;
			Potato papa = papaEnHitbox();
			if (papa != null) papa.recibirDanio(danio);
		}

		if (!sprite.isPlaying()) {
			atacando = false;
			cooldownAtaque = tiempoEntreAtaques;
			sprite.play(ultimaAnimacion);
		}
	}

	private Potato papaEnHitbox() {
		if (hitbox == null) return null;
		VariantArray<Node2D> cuerpos = hitbox.getOverlappingBodies();
		for (int i = 0; i < cuerpos.size(); i++) {
			Node2D cuerpo = cuerpos.get(i);
			if (cuerpo instanceof Potato) return (Potato) cuerpo;
		}
		return null;
	}

	private Node2D buscarPapa() {
		Node n = getTree().getFirstNodeInGroup(grupoPlayer);
		if (n instanceof Node2D) return (Node2D) n;
		return null;
	}

	@Register
	public void recibirDanio(double cantidad) {
		if (muerto) return;
		vida -= cantidad;
		tiempoFlash = 0.1;
		if (sprite != null) sprite.setModulate(colorGolpe);
		if (vida <= 0) morir();
	}

	private void actualizarFlash(double delta) {
		if (tiempoFlash > 0) {
			tiempoFlash -= delta;
			if (tiempoFlash <= 0 && sprite != null) sprite.setModulate(colorNormal);
		}
	}

	private void morir() {
		muerto = true;
		queueFree();
	}

	public void escalar(double multiplicador) {
		vidaMax *= multiplicador;
		vida = vidaMax;
		danio *= multiplicador;
	}
}
