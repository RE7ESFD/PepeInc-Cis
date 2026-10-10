package Character;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.CharacterBody2D;
import godot.api.Input;
import godot.api.KinematicCollision2D;
import godot.api.Node2D;
import godot.api.RigidBody2D;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;

@Script
public class Potato extends CharacterBody2D {
    private AnimatedSprite2D sprite;

    private final StringName animIdle = new StringName("idle");
    private final StringName animWalk = new StringName("walk");
    private final StringName animJump = new StringName("jump");
    private final StringName animDoubleJump = new StringName("double jump");

    private final StringName left = new StringName("ui_left");
    private final StringName right = new StringName("ui_right");
    private final StringName jump = new StringName("ui_jump");

    @Visible public double speed = 110.0;
    public int jumpReaming = 2;

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

        double dir = Input.INSTANCE.getAxis(left, right);

        vx = dir * speed * 1.0;
        vy = dir * speed * 1.0;
        setVelocity(new Vector2(vx, vy));
        moveAndSlide();

        if (dir != 0) sprite.setFlipH(dir < 0);

        if (jumpReaming == 1) {
            sprite.play(animJump);
        } else if (jumpReaming == 0) {
            sprite.play(animDoubleJump);
        } else if (dir != 0) {
            sprite.play(animWalk);
        } else {
            sprite.play(animIdle);
        }
        if(isOnFloor()) {
            jumpReaming = 2;
        }
    }
}
