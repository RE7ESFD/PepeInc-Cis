package Game;

import enemy.Horno;
import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.Area2D;
import godot.api.Node2D;
import godot.api.Sprite2D;
import godot.api.Texture2D;
import godot.core.NodePath;
import godot.core.VariantArray;
import godot.core.Vector2;
import godot.global.GD;

@Script
public class Projectile extends Area2D {

	@Visible public double speed = 600.0;
	@Visible public double danio = 10.0;
	@Visible public double vidaUtil = 2.0;
	@Visible public Texture2D customTexture = null;

	public Vector2 direction = new Vector2(1.0, 0.0);

	private double tiempoVivo = 0.0;
	private boolean usada = false;

	@Register
	@Override
	public void _ready() {
		// Usamos la misma forma de obtener nodos que tu script
		Sprite2D sprite = (Sprite2D) getNode(new NodePath("Sprite2D"));

		if (customTexture != null && sprite != null) {
			sprite.setTexture(customTexture);
		}

		setRotation((float) direction.angle());
		GD.print("Projectile: _ready ok | dir=" + direction + " speed=" + speed
				+ " pos=" + getGlobalPosition());
	}

	@Register
	@Override
	public void _physicsProcess(double delta) {
		if (usada) return;

		// Movimiento
		Vector2 movimiento = direction.times(speed * delta);
		setGlobalPosition(getGlobalPosition().plus(movimiento));

		// Tiempo de vida
		tiempoVivo += delta;
		if (tiempoVivo >= vidaUtil) {
			queueFree();
			return;
		}

		// Colisión con enemigos (mismo método que usa Horno con su Hitbox)
		VariantArray<Node2D> cuerpos = getOverlappingBodies();
		for (int i = 0; i < cuerpos.size(); i++) {
			Node2D cuerpo = cuerpos.get(i);
			if (cuerpo instanceof Horno) {
				usada = true;
				((Horno) cuerpo).recibirDanio(danio);
				queueFree();
				return;
			}
		}
	}
}
