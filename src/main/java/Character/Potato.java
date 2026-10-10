package Character;

import Game.Projectile;
import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.*;

import godot.core.MouseButton;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.global.GD;

@Script
public class Potato extends CharacterBody2D {
	private AnimatedSprite2D sprite;

	private final StringName animUp100 = new StringName("Up100");
	private final StringName animLeft100 = new StringName("Left100");
	private final StringName animIdle100 = new StringName("Idle100");
	private final StringName animUp70 = new StringName("Up70");
	private final StringName animLeft70 = new StringName("Left70");
	private final StringName animIdle70 = new StringName("Idle70");
	private final StringName animUp35 = new StringName("Up35");
	private final StringName animLeft35 = new StringName("Left35");
	private final StringName animIdle35 = new StringName("Idle35");

	private final StringName left = new StringName("ui_left");
	private final StringName right = new StringName("ui_right");
	private final StringName up = new StringName("ui_up");
	private final StringName down = new StringName("ui_down");
	private final StringName grupoPlayer = new StringName("player");

	@Visible public double speed = 800.0;
	@Visible public double health = 100;
	@Visible public PackedScene escenaProyectil;
	@Visible public String rutaProyectil = "res://Scenes/proyectil.tscn";
	private double tiempoInvulnerable = 0.0;
	private double invulnerableTras = 0.5;
	Vector2 dirBala;

	@Register
	@Override
	public void _ready() {
		addToGroup(grupoPlayer);
		GD.print("Potato: _ready ejecutado, registrada en grupo 'player'");
		GD.print("Potato: escenaProyectil " + (escenaProyectil == null
				? "es NULL (el inspector no la entregó al juego)" : "OK, asignada"));
		sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
	}
	@Register
	public void recibirDanio(double cantidad) {
		if (tiempoInvulnerable > 0) return;
		health -= cantidad;
		tiempoInvulnerable = invulnerableTras;
		GD.print("Papa recibió " + cantidad + " de daño. Vida: " + health);

		if (health <= 0) {
			GD.print("La papa se murió :(");
			getTree().reloadCurrentScene();
		}
	}


	@Register
	@Override
	public void _input(InputEvent event) {
		if (event instanceof InputEventMouseButton click
				&& click.getButtonIndex() == MouseButton.LEFT
				&& click.isPressed()) {
			disparar();
		}
	}
	private void disparar() {
		if (escenaProyectil == null) {
			// Plan B: cargar por ruta
			Resource r = ResourceLoader.load(rutaProyectil);
			if (r instanceof PackedScene) {
				escenaProyectil = (PackedScene) r;
				GD.print("Papa: bala cargada por ruta " + rutaProyectil);
			} else {
				GD.printErr("Papa: 'escenaProyectil' está vacía y no pude cargar '" + rutaProyectil
						+ "'. Corrige 'rutaProyectil' en el inspector o arriba en Potato.java.");
				return;
			}
		}

		Node instancia = escenaProyectil.instantiate();
		if (!(instancia instanceof Node2D)) {
			GD.printErr("Papa: la escena de la bala no tiene un Node2D/Area2D como raíz.");
			return;
		}
		Node2D bala = (Node2D) instancia;

		Vector2 origen = getGlobalPosition();
		dirBala = getGlobalMousePosition().minus(origen).normalized();

		// La dirección se asigna ANTES de addChild, porque Projectile._ready()
		// usa "direction" para rotar el sprite.
		boolean tieneScript = bala instanceof Projectile;
		if (tieneScript) {
			((Projectile) bala).direction = dirBala;
		} else {
			GD.printErr("Papa: la raíz de la escena de la bala NO tiene el script Projectile "
					+ "(Game/Projectile). La bala saldrá pero no se moverá ni hará daño.");
		}

		getParent().addChild(bala);
		bala.setGlobalPosition(origen);
		if (!tieneScript) bala.setGlobalRotation((float) dirBala.angle());

		GD.print("Papa disparó desde " + origen + " hacia " + dirBala);
	}


	@Register
	@Override
	public void _physicsProcess(double delta) {
		if (tiempoInvulnerable > 0) {
			tiempoInvulnerable -= delta;
			sprite.setVisible(((int) (tiempoInvulnerable * 10)) % 2 == 0);
			if (tiempoInvulnerable <= 0) {
				tiempoInvulnerable = 0;
				sprite.setVisible(true);
			}
		}

		Vector2 vel = getVelocity();

		double dirx = Input.getAxis(left, right);
		double diry = Input.getAxis(up, down);
		Vector2 dir = new Vector2(dirx, diry);
		if (dir.length() > 1.0) {
			dir = dir.normalized();
		}

		setVelocity(new Vector2(dir.getX() * speed, dir.getY() * speed));
		moveAndSlide();


		if (dirx != 0 && (health>70 && health<=100)) {
			sprite.setFlipH(dirx > 0);
			sprite.play(animLeft100);
		} else if (diry < 0 && (health>70 && health<=100)) {
			sprite.play(animUp100);
		} else if (health>70 && health<=100) {
			sprite.play(animIdle100);
		} else if (dirx != 0 && (health>35 && health<=70)) {
			sprite.setFlipH(dirx > 0);
			sprite.play(animLeft70);
		} else if (diry < 0 && (health>40 && health<=70)) {
			sprite.play(animUp70);
		} else if (health>40 && health<=70) {
			sprite.play(animIdle70);
		}else if (dirx != 0 && (health>1 && health<=40)) {
			sprite.setFlipH(dirx > 0);
			sprite.play(animLeft35);
		} else if (diry < 0 && (health>1 && health<=40)) {
			sprite.play(animUp35);
		} else if (health>1 && health<=40) {
			sprite.play(animIdle35);
		}
	}
}
