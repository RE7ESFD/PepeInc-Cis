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
import godot.global.GD;

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
	private boolean avisoSinPapa = false;
	private boolean avisoSinScript = false;

	private final StringName grupoPlayer = new StringName("player");
	private final StringName grupoEnemigos = new StringName("enemigos");

	@Register
	@Override
	public void _ready() {
		escenaEnemigo = (PackedScene) ResourceLoader.load("res://Scenes/enemigo.tscn");
		if (escenaEnemigo == null) {
			GD.printErr("SpawnerEnemigos: no se pudo cargar res://Scenes/enemigo.tscn "
					+ "(revisa que existan todos los sprites que usa la escena). Spawner apagado.");
			setProcess(false);
		}
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
		if (!(papa instanceof Node2D)) {
			if (!avisoSinPapa) {
				avisoSinPapa = true;
				GD.printErr("SpawnerEnemigos: no hay ningún nodo en el grupo 'player'. "
						+ "Revisa que el nodo de la papa tenga el script Potato "
						+ "(o agrégalo al grupo 'player' en Nodo > Grupos).");
			}
			return;
		}
		if (getTree().getNodesInGroup(grupoEnemigos).size() >= maxEnemigos) return;

		double angulo = Math.random() * Math.PI * 2.0;
		Vector2 offset = new Vector2(Math.cos(angulo) * radioSpawn, Math.sin(angulo) * radioSpawn);
		Vector2 pos = ((Node2D) papa).getGlobalPosition().plus(offset);

		Node instancia = escenaEnemigo.instantiate();
		if (!(instancia instanceof Node2D)) {
			GD.printErr("SpawnerEnemigos: la raíz de enemigo.tscn no es un Node2D/CharacterBody2D.");
			setProcess(false);
			return;
		}

		if (instancia instanceof Horno) {
			// +10% de vida y daño por cada 30 segundos que lleves vivo
			((Horno) instancia).escalar(1.0 + (tiempoVivo / 30.0) * 0.10);
		} else if (!avisoSinScript) {
			avisoSinScript = true;
			GD.printErr("SpawnerEnemigos: la raíz de enemigo.tscn NO tiene el script Horno "
					+ "(enemy/Horno). El enemigo aparecerá pero no se moverá.");
		}

		getParent().addChild(instancia);
		((Node2D) instancia).setGlobalPosition(pos);
	}

}
