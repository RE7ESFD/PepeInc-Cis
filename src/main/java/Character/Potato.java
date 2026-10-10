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

	@Visible public double speed = 800.0;
	@Visible public double health = 100;

	@Register
	@Override
	public void _ready() {
		sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
	}

	@Register
	@Override
	public void _physicsProcess(double delta) {
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
			sprite.play(animIdle35);
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
