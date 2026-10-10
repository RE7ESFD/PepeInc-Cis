package Character;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.CharacterBody2D;
import godot.api.Input;
import godot.core.Color;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.global.GD;

@Script
public class Potato extends CharacterBody2D {
    private AnimatedSprite2D sprite;

    private final StringName animIdle = new StringName("idle");
    private final StringName animWalk = new StringName("walk");

    private final StringName left = new StringName("ui_left");
    private final StringName right = new StringName("ui_right");
    private final StringName up = new StringName("ui_up");
    private final StringName down = new StringName("ui_down");

    private final StringName grupoPlayer = new StringName("player");

    @Visible public double speed = 110.0;

    // ---- Vida (los enemigos llaman a recibirDanio) ----
    @Visible public double vidaMax = 100.0;
    @Visible public double invulnerableTras = 0.5; // segundos de i-frames después de un golpe
    private double vida;
    private double tiempoInvulnerable = 0.0;

    private final Color colorNormal = new Color(1.0, 1.0, 1.0, 1.0);
    private final Color colorGolpe = new Color(1.0, 0.4, 0.4, 1.0);

    @Register
    @Override
    public void _ready() {
        sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
        vida = vidaMax;
        addToGroup(grupoPlayer); // así los enemigos encuentran a la papa
    }

    @Register
    @Override
    public void _physicsProcess(double delta) {
        // Movimiento top-down en 8 direcciones (estilo Brotato)
        Vector2 dir = Input.getVector(left, right, up, down);
        setVelocity(dir.times(speed));
        moveAndSlide();

        if (dir.getX() != 0) sprite.setFlipH(dir.getX() < 0);
        sprite.play(dir.length() > 0 ? animWalk : animIdle);

        if (tiempoInvulnerable > 0) {
            tiempoInvulnerable -= delta;
            if (tiempoInvulnerable <= 0) sprite.setModulate(colorNormal);
        }
    }

    @Register
    public void recibirDanio(double cantidad) {
        if (tiempoInvulnerable > 0) return;
        vida -= cantidad;
        tiempoInvulnerable = invulnerableTras;
        sprite.setModulate(colorGolpe);
        GD.print("Papa recibió " + cantidad + " de daño. Vida: " + vida);

        if (vida <= 0) {
            GD.print("La papa se murió :(");
            getTree().reloadCurrentScene(); // reinicia la run
        }
    }

    public double getVida() { return vida; }
}
