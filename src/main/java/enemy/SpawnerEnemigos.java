package enemy;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.Node;
import godot.api.Node2D;
import godot.api.PackedScene;
import godot.api.ResourceLoader;
import godot.core.StringName;
import godot.core.Vector2;

/**
 * Spawnea enemigos en un anillo alrededor de la papa.
 * Cada vez spawnea más rápido y los enemigos salen más fuertes (roguelike puro).
 * Ponerlo como un Node2D dentro de la escena principal.
 */
@Script
public class SpawnerEnemigos extends Node2D {

    @Visible public double intervaloInicial = 1.5;   // segundos entre spawns al inicio
    @Visible public double intervaloMinimo = 0.25;   // lo más rápido que puede llegar
    @Visible public double reduccionPorSegundo = 0.01;
    @Visible public double radioSpawn = 350.0;       // distancia a la papa donde aparecen
    @Visible public int maxEnemigos = 150;

    private PackedScene escenaEnemigo;
    private double intervaloActual;
    private double contador = 0.0;
    private double tiempoVivo = 0.0;

    private final StringName grupoPlayer = new StringName("player");
    private final StringName grupoEnemigos = new StringName("enemigos");

    @Register
    @Override
    public void _ready() {
        escenaEnemigo = (PackedScene) ResourceLoader.load("res://Scenes/enemigo.tscn");
        intervaloActual = intervaloInicial;
    }

    @Register
    @Override
    public void _process(double delta) {
        tiempoVivo += delta;
        intervaloActual = Math.max(intervaloMinimo, intervaloActual - reduccionPorSegundo * delta);

        contador += delta;
        if (contador >= intervaloActual) {
            contador = 0.0;
            spawnear();
        }
    }

    private void spawnear() {
        Node papa = getTree().getFirstNodeInGroup(grupoPlayer);
        if (!(papa instanceof Node2D)) return;
        if (getTree().getNodesInGroup(grupoEnemigos).size() >= maxEnemigos) return;

        double angulo = Math.random() * Math.PI * 2.0;
        Vector2 offset = new Vector2(Math.cos(angulo) * radioSpawn, Math.sin(angulo) * radioSpawn);
        Vector2 pos = ((Node2D) papa).getGlobalPosition().plus(offset);

        Horno enemigo = (Horno) escenaEnemigo.instantiate();
        // +10% de vida y daño por cada 30 segundos que lleves vivo
        enemigo.escalar(1.0 + (tiempoVivo / 30.0) * 0.10);
        getParent().addChild(enemigo);
        enemigo.setGlobalPosition(pos);
    }
}
