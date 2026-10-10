package enemy;

import Character.Potato;
import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.Area2D;
import godot.api.CharacterBody2D;
import godot.api.Node;
import godot.api.Node2D;
import godot.core.Color;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.core.VariantArray;

/**
 * Enemigo básico tipo Brotato: persigue a la papa y le pega por contacto.
 * Escena: res://Scenes/enemigo.tscn
 * Nodos que necesita como hijos: "AnimatedSprite2D" y "Hitbox" (Area2D).
 */
@Script
public class Horno extends CharacterBody2D {

    // ---- Stats (se pueden cambiar desde el inspector de Godot) ----
    @Visible public double vidaMax = 30.0;
    @Visible public double velocidad = 60.0;
    @Visible public double danio = 10.0;
    @Visible public double tiempoEntreAtaques = 0.8; // segundos

    // ---- Estado interno ----
    private double vida;
    private double cooldownAtaque = 0.0;
    private double tiempoFlash = 0.0;
    private boolean muerto = false;

    private AnimatedSprite2D sprite;
    private Area2D hitbox;

    private final StringName grupoPlayer = new StringName("player");
    private final StringName grupoEnemigos = new StringName("enemigos");
    private final StringName animIdle = new StringName("idle");
    private final StringName animWalk = new StringName("walking");

    private final Color colorNormal = new Color(1.0, 1.0, 1.0, 1.0);
    private final Color colorGolpe = new Color(1.0, 0.35, 0.35, 1.0);

    @Register
    @Override
    public void _ready() {
        sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
        hitbox = (Area2D) getNode(new NodePath("Hitbox"));
        vida = vidaMax;
        addToGroup(grupoEnemigos);
        sprite.play(animWalk);
    }

    @Register
    @Override
    public void _physicsProcess(double delta) {
        if (muerto) return;

        if (cooldownAtaque > 0) cooldownAtaque -= delta;
        actualizarFlash(delta);

        Node2D papa = buscarPapa();
        if (papa == null) {
            // No hay papa (murió o no está en la escena): quieto
            setVelocity(new Vector2(0.0, 0.0));
            sprite.play(animIdle);
            return;
        }

        // Perseguir a la papa
        Vector2 dir = getGlobalPosition().directionTo(papa.getGlobalPosition());
        setVelocity(dir.times(velocidad));
        moveAndSlide();

        if (dir.getX() != 0) sprite.setFlipH(dir.getX() < 0);
        sprite.play(animWalk);

        intentarAtacar();
    }

    /** Busca la papa por grupo, así no importa en qué parte del árbol esté. */
    private Node2D buscarPapa() {
        Node n = getTree().getFirstNodeInGroup(grupoPlayer);
        if (n instanceof Node2D) return (Node2D) n;
        return null;
    }

    /** Daño por contacto: si la papa está dentro del Hitbox, le pega cada X segundos. */
    private void intentarAtacar() {
        if (cooldownAtaque > 0) return;

        VariantArray<Node2D> cuerpos = hitbox.getOverlappingBodies();
        for (int i = 0; i < cuerpos.size(); i++) {
            Node2D cuerpo = cuerpos.get(i);
            if (cuerpo instanceof Potato) {
                ((Potato) cuerpo).recibirDanio(danio);
                cooldownAtaque = tiempoEntreAtaques;
                return;
            }
        }
    }

    /** Lo llaman las balas / ataques de la papa. */
    @Register
    public void recibirDanio(double cantidad) {
        if (muerto) return;
        vida -= cantidad;
        tiempoFlash = 0.1;
        sprite.setModulate(colorGolpe);
        if (vida <= 0) morir();
    }

    private void actualizarFlash(double delta) {
        if (tiempoFlash > 0) {
            tiempoFlash -= delta;
            if (tiempoFlash <= 0) sprite.setModulate(colorNormal);
        }
    }

    private void morir() {
        muerto = true;
        // Aquí después se puede soltar XP / material, sumar calor a la papa, etc.
        queueFree();
    }

    /** Para que el spawner pueda escalar la dificultad. */
    public void escalar(double multiplicador) {
        vidaMax *= multiplicador;
        vida = vidaMax;
        danio *= multiplicador;
    }
}
