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

	// Indice 0 = vida alta (100), 1 = media (70), 2 = baja (35)
	private final StringName[] animIdle = {
			new StringName("Idle100"), new StringName("Idle70"), new StringName("Idle35")
	};
	private final StringName[] animLeft = {
			new StringName("Left100"), new StringName("Left70"), new StringName("Left35")
	};
	private final StringName[] animUp = {
			new StringName("Up100"), new StringName("Up70"), new StringName("Up35")
	};
	private final StringName[] animAtackLeft = {new StringName("AtackLeft100"), new StringName("AtackLeft70"),new StringName("AtackLeft35")};
	private final StringName[] animAtackDown = {new StringName("AtackDown100"),new StringName("AtackDown70"),new StringName("AtackDown35")};

	private final StringName left = new StringName("ui_left");
	private final StringName right = new StringName("ui_right");
	private final StringName up = new StringName("ui_up");
	private final StringName down = new StringName("ui_down");
	private final StringName grupoPlayer = new StringName("player");

	@Visible public double speed = 800.0.7*(100-health);
	@Visible public double health = 100;
	@Visible public PackedScene escenaProyectil;
	@Visible public String rutaProyectil = "res://Scenes/proyectil.tscn";
	@Visible public double duracionAtaque = 0.3; // segundos que dura la animacion de ataque

	private double tiempoInvulnerable = 0.0;
	private double invulnerableTras = 0.5;
	private boolean atacando = false;
	private double tiempoAtaque = 0.0;
	

	@Register
	@Override
	public void _ready() {
		addToGroup(grupoPlayer);
		GD.print("Potato: _ready ejecutado, registrada en grupo 'player'");
		GD.print("Potato: escenaProyectil " + (escenaProyectil == null
				? "es NULL (el inspector no la entrego al juego)" : "OK, asignada"));
		sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
	}

	@Register
	public void recibirDanio(double cantidad) {
		if (tiempoInvulnerable > 0) return;
		health -= cantidad;
		tiempoInvulnerable = invulnerableTras;
		GD.print("Papa recibio " + cantidad + " de danio. Vida: " + health);

		if (health <= 0) {
			GD.print("La papa se murio :(");
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
				GD.printErr("Papa: 'escenaProyectil' esta vacia y no pude cargar '" + rutaProyectil
						+ "'. Corrige 'rutaProyectil' en el inspector o arriba en Potato.java.");
				return;
			}
		}

		Node instancia = escenaProyectil.instantiate();
		if (!(instancia instanceof Node2D)) {
			GD.printErr("Papa: la escena de la bala no tiene un Node2D/Area2D como raiz.");
			return;
		}
		Node2D bala = (Node2D) instancia;

		Vector2 origen = getGlobalPosition();
		Vector2 dirBala = getGlobalMousePosition().minus(origen).normalized();

		// La direccion se asigna ANTES de addChild, porque Projectile._ready()
		// usa "direction" para rotar el sprite.
		boolean tieneScript = bala instanceof Projectile;
		if (tieneScript) {
			((Projectile) bala).direction = dirBala;
		} else {
			GD.printErr("Papa: la raiz de la escena de la bala NO tiene el script Projectile "
					+ "(Game/Projectile). La bala saldra pero no se movera ni hara danio.");
		}

		getParent().addChild(bala);
		bala.setGlobalPosition(origen);
		if (!tieneScript) bala.setGlobalRotation((float) dirBala.angle());

		GD.print("Papa disparo desde " + origen + " hacia " + dirBala);
		atacarSprite(dirBala);
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

		if (atacando) {
			tiempoAtaque -= delta;
			if (tiempoAtaque <= 0) atacando = false;
		}

		double dirx = Input.getAxis(left, right);
		double diry = Input.getAxis(up, down);
		Vector2 dir = new Vector2(dirx, diry);
		if (dir.length() > 1.0) {
			dir = dir.normalized();
		}

		setVelocity(new Vector2(dir.getX() * speed, dir.getY() * speed));
		moveAndSlide();

		// Mientras ataca, no se pisa la animacion de ataque con la de caminar
		if (!atacando) actualizarAnimacion(dirx, diry);
	}

	// 0 = vida > 70, 1 = vida > 35, 2 = vida <= 35 (cubre todos los valores, sin huecos)
	private int nivelVida() {
		if (health > 70) return 0;
		if (health > 35) return 1;
		return 2;
	}

	private void actualizarAnimacion(double dirx, double diry) {
		int n = nivelVida();
		if (dirx != 0) {
			sprite.setFlipH(dirx > 0);
			sprite.play(animLeft[n]);
		} else if (diry < 0) {
			sprite.play(animUp[n]);
		} else {
			sprite.play(animIdle[n]);
		}
	}

	private void atacarSprite(Vector2 dir) {
		boolean horizontal = Math.abs(dir.getX()) >= Math.abs(dir.getY());
		if (horizontal) {
			// AtackLeft mira a la izquierda; flip si apunta a la derecha
			sprite.setFlipH(dir.getX() > 0);
			sprite.play(animAtackLeft[nivelVida()]);
		} else if (dir.getY() < 0) {
			// Hacia arriba: se usa la animacion Up normal segun la vida actual
			sprite.play(animUp[nivelVida()]);
		} else {
			sprite.play(animAtackDown[nivelVida()]);
		}
		atacando = true;
		tiempoAtaque = duracionAtaque;
	}
}
