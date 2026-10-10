package Character;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.CharacterBody2D;
import godot.api.Input;

import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.global.GD;

@Script
public class Potato extends CharacterBody2D {
	private AnimatedSprite2D sprite;

	private final StringName animUp = new StringName("Up");
	private final StringName animLeft = new StringName("Left");
	private final StringName animIdle = new StringName("Idle");

	private final StringName left = new StringName("ui_left");
	private final StringName right = new StringName("ui_right");
	private final StringName up = new StringName("ui_up");
	private final StringName down = new StringName("ui_down");

	@Visible public double speed = 110.0;

	@Register
	@Override
	public void _ready() {
		sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
	}

	@Register
	@Override
	public void _physicsProcess(double delta) {
		GD.print("physics tick");
		Vector2 vel = getVelocity();
		double vx = vel.getX();
		double vy = vel.getY();

		double dirx = Input.getAxis(left, right);
		double diry = Input.getAxis(up, down);
		Vector2 dir = new Vector2(dirx, diry);
		if (dir.length() > 1.0) {
			dir = dir.normalized();
		}

		setVelocity(new Vector2(dir.getX() * speed, dir.getY() * speed));
		moveAndSlide();
		

		if (dirx != 0) {
			sprite.setFlipH(dirx < 0);   // si tu sprite "Left" mira a la izquierda, usa dirx > 0
			sprite.play(animLeft);
		} else if (diry != 0) {
			sprite.play(animUp);
		} else {
			sprite.play(animIdle);
		}
	}
}
