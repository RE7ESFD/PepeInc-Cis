package Game;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.Area2D;
import godot.api.Sprite2D;
import godot.api.Texture2D;
import godot.core.NodePath;
import godot.core.Vector2;

@Script
public class Projectile extends Area2D {

	@Visible public double speed = 600.0;
	@Visible public Texture2D customTexture = null;

	public Vector2 direction = new Vector2(1.0, 0.0);

	@Register
	@Override
	public void _ready() {
		// Usamos la misma forma de obtener nodos que tu script
		Sprite2D sprite = (Sprite2D) getNode(new NodePath("Sprite2D"));

		if (customTexture != null && sprite != null) {
			sprite.setTexture(customTexture);
		}

		setRotation((float) direction.angle());
	}

	@Register
	@Override
	public void _physicsProcess(double delta) {
		// Usamos tu misma lógica de vectores
		Vector2 movement = direction.times(speed * delta);
		setPosition(getPosition().plus(movement));
	}
}
