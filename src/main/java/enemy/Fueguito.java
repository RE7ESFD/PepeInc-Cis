package enemy;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.CharacterBody2D;
import godot.api.Node;
import godot.api.Node2D;
import godot.core.Color;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.global.GD;

/**
 * Enemigo Fueguito cuerpo a cuerpo: camina siempre hacia la papa y, cuando la toca,
 * le hace daño y se prende en fuego (se muestra el AnimatedSprite2D "Fuego": se quema hasta quedar en cenizas) y desaparece.
 * Escena: res://Scenes/fueguito.tscn  (necesita hijos "AnimatedSprite2D" y "Fuego")
 */
@Script
public class Fueguito extends CharacterBody2D {

    @Visible public double vidaMax = 20.0;
    @Visible public double velocidad = 160.0;
    @Visible public double danioContacto = 12.0;
    @Visible public double rangoContacto = 80.0;    // distancia a la papa a la que "le da"
    @Visible public double duracionFuego = 2.3;     // segundos totales de la animación de muerte antes de desaparecer (12 frames a 8 fps = 1.5 s + 0.8 s de cenizas)

    private double vida;
    private double tiempoFlash = 0.0;
    private double esperaCenizas = 0.0;
    private boolean muerto = false;
    private boolean ejeHorizontal = false;

    private AnimatedSprite2D sprite;
    private AnimatedSprite2D fuego;

    private final StringName grupoPlayer = new StringName("player");
    private final StringName grupoEnemigos = new StringName("enemigos");

    private final StringName animWalkDown = new StringName("walk_down");
    private final StringName animWalkUp = new StringName("walk_up");
    private final StringName animWalkLeft = new StringName("walk_left");
    private final StringName animWalkRight = new StringName("walk_right");
    private final StringName animFuego = new StringName("fuego");

    // Según cómo registre los métodos tu versión de godot-kotlin-jvm, el nombre puede ser uno u otro
    private final StringName metodoDanioPapa = new StringName("recibirDanio");
    private final StringName metodoDanioPapaSnake = new StringName("recibir_danio");

    private StringName animCaminar = animWalkDown;

    private final Color colorNormal = new Color(1.0, 1.0, 1.0, 1.0);
    private final Color colorGolpe = new Color(1.0, 0.35, 0.35, 1.0);

    @Register
    @Override
    public void _ready() {
        sprite = (AnimatedSprite2D) getNode(new NodePath("AnimatedSprite2D"));
        fuego = (AnimatedSprite2D) getNode(new NodePath("Fuego"));
        vida = vidaMax;
        addToGroup(grupoEnemigos);

        if (fuego != null) fuego.setVisible(false); // el fueguito solo aparece al morir
        sprite.play(animCaminar);
    }

    @Register
    @Override
    public void _physicsProcess(double delta) {
        if (muerto) {
            // Ya se quemó: pasado el tiempo de la animación de muerte, desaparece
            esperaCenizas -= delta;
            if (esperaCenizas <= 0) queueFree();
            return;
        }

        actualizarFlash(delta);

        Node2D papa = buscarPapa();
        if (papa == null) {
            setVelocity(new Vector2(0.0, 0.0));
            sprite.stop();
            return;
        }

        Vector2 haciaPapa = papa.getGlobalPosition().minus(getGlobalPosition());
        double distancia = haciaPapa.length();
        Vector2 dir = haciaPapa.normalized();
        orientar(dir);

        if (distancia <= rangoContacto) {
            // Le dio a la papa: si el golpe entra, se quema. Si la papa está invulnerable, espera pegado.
            if (danarPapa(papa)) {
                morir();
            } else {
                setVelocity(new Vector2(0.0, 0.0));
                sprite.play(animCaminar);
            }
            return;
        }

        // Camina hacia la papa como fueguito
        setVelocity(dir.times(velocidad));
        moveAndSlide();
        sprite.play(animCaminar);
    }

    private void orientar(Vector2 dir) {
        double ax = Math.abs(dir.getX());
        double ay = Math.abs(dir.getY());
        // Margen para que no cambie de eje (izq/der <-> arriba/abajo) por poca diferencia
        if (ejeHorizontal && ay > ax * 1.3) ejeHorizontal = false;
        else if (!ejeHorizontal && ax > ay * 1.3) ejeHorizontal = true;

        if (ejeHorizontal) animCaminar = dir.getX() < 0 ? animWalkLeft : animWalkRight;
        else animCaminar = dir.getY() < 0 ? animWalkUp : animWalkDown;
    }

    // ------------------------------------------------------------------
    // Daño a la papa
    // ------------------------------------------------------------------

    /** Devuelve true si el golpe ya se resolvió (entró o no se pudo hacer), false si la papa lo ignoró por invulnerable. */
    private boolean danarPapa(Node2D papa) {
        StringName metodo;
        if (papa.hasMethod(metodoDanioPapa)) metodo = metodoDanioPapa;
        else if (papa.hasMethod(metodoDanioPapaSnake)) metodo = metodoDanioPapaSnake;
        else {
            GD.printErr("Fueguito: la papa no tiene recibirDanio ni recibir_danio registrado (no se le pudo hacer daño). "
                    + "Revisa que Potato.recibirDanio tenga @Register y que hayas compilado.");
            return true; // no se queda esperando para siempre
        }
        Object resultado = papa.call(metodo, danioContacto);
        // Si Potato.recibirDanio devuelve boolean y dice false, la papa estaba invulnerable
        return !Boolean.FALSE.equals(resultado);
    }

    private Node2D buscarPapa() {
        Node n = getTree().getFirstNodeInGroup(grupoPlayer);
        if (n instanceof Node2D) return (Node2D) n;
        return null;
    }

    // ------------------------------------------------------------------
    // Vida
    // ------------------------------------------------------------------

    /** Lo llaman los proyectiles de la papa. */
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
        if (muerto) return;
        muerto = true;
        esperaCenizas = duracionFuego;

        // Ya no choca ni cuenta como enemigo vivo
        setVelocity(new Vector2(0.0, 0.0));
        setCollisionLayer(0L);
        setCollisionMask(0L);
        removeFromGroup(grupoEnemigos);

        // Se esconde el fueguito y se despliega el fueguito
        sprite.stop();
        sprite.setVisible(false);
        if (fuego != null) {
            fuego.setVisible(true);
            fuego.play(animFuego);
        }
        // Aquí después se puede soltar XP / material, etc.
    }

    /** Lo usa el SpawnerEnemigos para que salgan más duros con el tiempo. */
    public void escalar(double multiplicador) {
        vidaMax *= multiplicador;
        vida = vidaMax;
        danioContacto *= multiplicador;
    }
}
