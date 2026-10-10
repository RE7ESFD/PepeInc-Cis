package enemy;

import Character.Potato;
import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.Area2D;
import godot.api.Node2D;
import godot.api.StaticBody2D;
import godot.core.VariantArray;
import godot.core.Vector2;

/**
 * Bola de fuego que dispara el Horno.
 * Escena: res://Scenes/proyectil_enemigo.tscn
 * Detecta la capa 1 (papa y muros): a la papa le hace daño, en los muros se apaga.
 */
@Script
public class ProyectilEnemigo extends Area2D {

	@Visible public double velocidad = 900.0;
	@Visible public double danio = 10.0;
	@Visible public double tiempoVida = 4.0; // se borra sola si no choca con nada

	/** La pone el Horno antes de agregarla a la escena. */
	public Vector2 direccion = new Vector2(1.0, 0.0);

	private boolean usada = false;

	@Register
	@Override
	public void _ready() {
		setRotation((float) direccion.angle());
	}

	@Register
	@Override
	public void _physicsProcess(double delta) {
		if (usada) return;

		setGlobalPosition(getGlobalPosition().plus(direccion.times(velocidad * delta)));

		tiempoVida -= delta;
		if (tiempoVida <= 0) {
			queueFree();
			return;
		}

		// Mismo método que usa el proyectil de la papa (Game/Projectile)
		VariantArray<Node2D> cuerpos = getOverlappingBodies();
		for (int i = 0; i < cuerpos.size(); i++) {
			Node2D cuerpo = cuerpos.get(i);
			if (cuerpo instanceof Potato) {
				usada = true;
				((Potato) cuerpo).recibirDanio(danio);
				queueFree();
				return;
			}
			if (cuerpo instanceof StaticBody2D) { // muros del mapa
				usada = true;
				queueFree();
				return;
			}
		}
	}
}
