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
 * Spawnea enemigos (hornos y fueguitos) en un anillo alrededor de la papa.
 * Cada vez spawnea más rápido y los enemigos salen más fuertes (roguelike puro).
 * Ponerlo como un Node2D dentro de la escena principal.
 */
@Script
public class SpawnerEnemigos extends Node2D {

	@Visible public double intervaloInicial = 3;   // segundos entre spawns al inicio
	@Visible public double intervaloMinimo = 0.5;   // lo más rápido que puede llegar
	@Visible public double reduccionPorSegundo = 0.0001;
	@Visible public double radioSpawn = 1800.0;      // distancia a la papa donde aparecen
	@Visible public double probabilidadFueguito = 0.6; // 0.0 = solo hornos, 1.0 = solo fueguitos

	// Bordes de la arena (por dentro de los muros de map.tscn) para no spawnear afuera
	@Visible public double arenaMinX = -3650.0;
	@Visible public double arenaMaxX = 3820.0;
	@Visible public double arenaMinY = -800.0;
	@Visible public double arenaMaxY = 1840.0;
	@Visible public int maxEnemigos = 150;

	private static final String RUTA_HORNO = "res://Scenes/enemigo.tscn";
	private static final String RUTA_FUEGUITO = "res://Scenes/fueguito.tscn";

	private PackedScene escenaHorno;
	private PackedScene escenaFueguito;
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
		escenaHorno = (PackedScene) ResourceLoader.load(RUTA_HORNO);
		escenaFueguito = (PackedScene) ResourceLoader.load(RUTA_FUEGUITO);
		if (escenaHorno == null) {
			GD.printErr("SpawnerEnemigos: no se pudo cargar " + RUTA_HORNO + " (no saldrán hornos).");
		}
		if (escenaFueguito == null) {
			GD.printErr("SpawnerEnemigos: no se pudo cargar " + RUTA_FUEGUITO + " (no saldrán fueguitos).");
		}
		if (escenaHorno == null && escenaFueguito == null) {
			GD.printErr("SpawnerEnemigos: no hay ninguna escena de enemigo. Spawner apagado.");
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

	/** Elige qué escena spawnear según probabilidadFueguito (si falta una, usa la otra). */
	private PackedScene elegirEscena() {
		if (escenaFueguito == null) return escenaHorno;
		if (escenaHorno == null) return escenaFueguito;
		return Math.random() < probabilidadFueguito ? escenaFueguito : escenaHorno;
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
		pos = new Vector2(
				Math.max(arenaMinX, Math.min(arenaMaxX, pos.getX())),
				Math.max(arenaMinY, Math.min(arenaMaxY, pos.getY())));

		PackedScene escena = elegirEscena();
		Node instancia = escena.instantiate();
		if (!(instancia instanceof Node2D)) {
			GD.printErr("SpawnerEnemigos: la raíz de la escena de enemigo no es un Node2D/CharacterBody2D.");
			instancia.queueFree();
			return;
		}

		// +10% de vida y daño por cada 30 segundos que lleves vivo
		double multiplicador = 1.0 + (tiempoVivo / 30.0) * 0.10;
		if (instancia instanceof Horno) {
			((Horno) instancia).escalar(multiplicador);
		} else if (instancia instanceof Fueguito) {
			((Fueguito) instancia).escalar(multiplicador);
		} else if (!avisoSinScript) {
			avisoSinScript = true;
			GD.printErr("SpawnerEnemigos: la raíz de una escena de enemigo NO tiene el script Horno ni Fueguito. "
					+ "El enemigo aparecerá pero no se moverá.");
		}

		getParent().addChild(instancia);
		((Node2D) instancia).setGlobalPosition(pos);
	}

}
