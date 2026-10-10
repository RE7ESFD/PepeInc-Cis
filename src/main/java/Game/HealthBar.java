package Game;

import Character.Potato;
import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.AnimatedSprite2D;
import godot.api.Camera2D;
import godot.api.Node;
import godot.api.Node2D;
import godot.api.ProgressBar;
import godot.core.NodePath;
import godot.core.StringName;
import godot.core.Vector2;
import godot.global.GD;

@Script
public class HealthBar extends Node2D {
	private ProgressBar healthBar;
	private AnimatedSprite2D actualPotato;
	private Potato player;
	private Camera2D camara;
	private int nivelActual = -1;

	// Posicion en PIXELES DE PANTALLA respecto al centro de la vista.
	// x positivo = derecha, y negativo = arriba. Ajusta hasta que quede donde quieras.
	@Visible public double offsetX = 500.0;
	@Visible public double offsetY = -110.0;

	private final StringName grupoPlayer = new StringName("player");

	// AJUSTA estos nombres a las animaciones reales de tu AnimatedSprite2D
	// 0 = vida alta (>70), 1 = media (>35), 2 = baja (<=35)
	private final StringName[] animNivel = {
			new StringName("100"), new StringName("70"), new StringName("35")
	};

	@Register
	@Override
	public void _ready() {
		// AJUSTA las rutas si tus nodos hijos se llaman distinto
		healthBar = (ProgressBar) getNodeOrNull(new NodePath("ProgressBar"));
		actualPotato = (AnimatedSprite2D) getNodeOrNull(new NodePath("AnimatedSprite2D"));

		if (healthBar == null) GD.printErr("HealthBar: no encontre el nodo hijo 'ProgressBar'");
		if (actualPotato == null) GD.printErr("HealthBar: no encontre el nodo hijo 'AnimatedSprite2D'");
	}

	@Register
	@Override
	public void _process(double delta) {
		if (player == null) {
			buscarJugador();
			if (player == null) return; // Potato aun no esta en el grupo 'player'
		}

		if (camara == null) {
			// AJUSTA el nombre si tu camara se llama distinto
			camara = (Camera2D) player.getNodeOrNull(new NodePath("Camera2D"));
			if (camara == null) {
				GD.printErr("HealthBar: no encontre 'Camera2D' como hija de Potato");
			}
		}
		if (camara != null) fijarEnPantalla();

		double vida = 100-player.health;

		if (healthBar != null) healthBar.setValue(vida);

		int nivel = nivelVida(vida);
		if (nivel != nivelActual) {		  // solo cambia la animacion al cruzar un umbral
			nivelActual = nivel;
			if (actualPotato != null) actualPotato.play(animNivel[nivel]);
		}
	}

	// Coloca la barra a un punto fijo de la vista REAL de la camara
	// (respeta smoothing y limites, a diferencia de ser solo hija de la camara).
	private void fijarEnPantalla() {
		Vector2 centro = camara.getScreenCenterPosition();
		Vector2 zoom = camara.getZoom();
		setGlobalPosition(new Vector2(
				centro.getX() + offsetX / zoom.getX(),
				centro.getY() + offsetY / zoom.getY()));
	}

	private void buscarJugador() {
		Node nodo = getTree().getFirstNodeInGroup(grupoPlayer);
		if (nodo instanceof Potato) {
			player = (Potato) nodo;
		}
	}

	private int nivelVida(double vida) {
		if (vida > 70) return 0;
		if (vida > 35) return 1;
		return 2;
	}
}
