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

	// ---------- AJUSTES: cambia los numeros aqui y recompila ----------
	@Visible public double speed = 800.0;		  // velocidad base (vida llena)
	@Visible public double health = 100;		   // vida actual
	@Visible public double bonoVelocidad = 0.5;	// +50% de velocidad con 0 de vida
	@Visible public double bonoDanio = 1.0;		// +100% de dano con 0 de vida
	@Visible public double duracionAtaque = 0.3;   // segundos de la animacion de ataque
	@Visible public double duracionMuerte = 1.5;   // segundos antes de reiniciar la escena
	@Visible public PackedScene escenaProyectil;
	@Visible public String rutaProyectil = "res://Scenes/proyectil.tscn";

	private static final double VIDA_MAX = 100.0;

	// ---------- ANIMACIONES (indice 0 = vida alta, 1 = media, 2 = baja) ----------
	private final StringName[] animIdle = {
			new StringName("Idle100"), new StringName("Idle70"), new StringName("Idle35")
	};
	private final StringName[] animLeft = {
			new StringName("Left100"), new StringName("Left70"), new StringName("Left35")
	};
	private final StringName[] animUp = {
			new StringName("Up100"), new StringName("Up70"), new StringName("Up35")
	};
	private final StringName[] animAtackLeft = {new StringName("AtackLeft100"), new StringName("AtackLeft70"),
			new StringName("AtackLeft35")};
	private final StringName[] animAtackDown = {new StringName("AtackDown100"), new StringName("AtackDown70"),
			new StringName("AtackDown35")};
	private final StringName animDeath = new StringName("Death");

	private final StringName left = new StringName("ui_left");
	private final StringName right = new StringName("ui_right");
	private final StringName up = new StringName("ui_up");
	private final StringName down = new StringName("ui_down");
	private final StringName grupoPlayer = new StringName("player");

	// ---------- ESTADO ----------
	private AnimatedSprite2D sprite;
	private boolean atacando = false;
	private double tiempoAtaque = 0.0;
	private double tiempoInvulnerable = 0.0;
	private boolean muerto = false;
	private double tiempoMuerte = 0.0;

	@Register
	@Override
	public void _ready() {
		addToGroup(grupoPlayer);
		sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
	}

	// ---------- DANO Y MUERTE ----------
	@Register
	public void recibirDanio(double cantidad) {
		if (muerto || tiempoInvulnerable > 0) return;
		health = Math.max(0, health - cantidad);
		tiempoInvulnerable = 0.5;
		GD.print("Papa recibio " + cantidad + " de danio. Vida: " + health);
		if (health <= 0) morir();
	}

	private void morir() {
		muerto = true;
		tiempoMuerte = duracionMuerte;
		setVelocity(new Vector2(0, 0));
		sprite.setVisible(true);
		SpriteFrames frames = sprite.getSpriteFrames();
		if (frames != null && frames.hasAnimation(animDeath)) {
			sprite.play(animDeath);
		} else {
			GD.printErr("Potato: falta la animacion 'Death' en el SpriteFrames");
		}
	}

	// ---------- DISPARO ----------
	@Register
	@Override
	public void _input(InputEvent event) {
		if (muerto) return;
		if (event instanceof InputEventMouseButton click
				&& click.getButtonIndex() == MouseButton.LEFT
				&& click.isPressed()) {
			disparar();
		}
	}

	private void disparar() {
		if (escenaProyectil == null) {
			Resource r = ResourceLoader.load(rutaProyectil);
			if (r instanceof PackedScene) {
				escenaProyectil = (PackedScene) r;
			} else {
				GD.printErr("Papa: no pude cargar la bala en '" + rutaProyectil + "'");
				return;
			}
		}

		Node instancia = escenaProyectil.instantiate();
		if (!(instancia instanceof Node2D)) {
			GD.printErr("Papa: la escena de la bala debe tener un Node2D/Area2D como raiz.");
			return;
		}
		Node2D bala = (Node2D) instancia;

		Vector2 origen = getGlobalPosition();
		Vector2 dirBala = getGlobalMousePosition().minus(origen).normalized();

		// La direccion va ANTES de addChild porque Projectile._ready() la usa
		if (bala instanceof Projectile) {
			((Projectile) bala).direction = dirBala;
			// La bala se lleva el bono de dano que tiene la papa EN ESTE disparo
			((Projectile) bala).setMultiplicadorDanio(multiplicadorDanio());
		} else {
			GD.printErr("Papa: la raiz de la bala no tiene el script Projectile.");
		}

		getParent().addChild(bala);
		bala.setGlobalPosition(origen);
		atacarSprite(dirBala);
	}

	// ---------- MOVIMIENTO ----------
	@Register
	@Override
	public void _physicsProcess(double delta) {
		// Muerto: solo espera a que termine la animacion y reinicia
		if (muerto) {
			tiempoMuerte -= delta;
			if (tiempoMuerte <= 0) {
				tiempoMuerte = 9999; // evita llamar varias veces al reinicio
				getTree().reloadCurrentScene();
			}
			return;
		}

		// Parpadeo mientras es invulnerable
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

		// Mecanica de calor: cuanta menos vida, mas rapido y mas dano
		double calor = calor();
		double velocidadActual = speed * (1.0 + calor * bonoVelocidad);

		double dirx = Input.getAxis(left, right);
		double diry = Input.getAxis(up, down);
		double largo = Math.sqrt(dirx * dirx + diry * diry);
		if (largo > 1.0) { // normaliza para no ir mas rapido en diagonal
			dirx /= largo;
			diry /= largo;
		}

		setVelocity(new Vector2(dirx * velocidadActual, diry * velocidadActual));
		moveAndSlide();

		if (!atacando) actualizarAnimacion(dirx, diry);
	}

	// ---------- CALOR ----------
	// 0.0 = vida llena, 1.0 = sin vida
	private double calor() {
		return Math.max(0.0, Math.min(1.0, (VIDA_MAX - health) / VIDA_MAX));
	}

	// 1.0 con la vida llena; sube hasta (1 + bonoDanio) sin vida
	private double multiplicadorDanio() {
		return 1.0 + calor() * bonoDanio;
	}

	// ---------- ANIMACIONES ----------
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
		if (Math.abs(dir.getX()) >= Math.abs(dir.getY())) {
			sprite.setFlipH(dir.getX() > 0);
			sprite.play(animAtackLeft[nivelVida()]);
		} else if (dir.getY() < 0) {
			sprite.play(animUp[nivelVida()]);
		} else {
			sprite.play(animAtackDown[nivelVida()]);
		}
		atacando = true;
		tiempoAtaque = duracionAtaque;
	}
}
