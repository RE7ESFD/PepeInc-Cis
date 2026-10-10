package Game;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.api.Button;
import godot.api.CanvasLayer;
import godot.api.Control;
import godot.api.Input;
import godot.api.Label;
import godot.api.Node;
import godot.core.LambdaCallable0;
import godot.core.NodePath;
import godot.core.StringName;
import godot.global.GD;
@Script
public class MenuPausa extends CanvasLayer {

	private Control fondo;
	private Label titulo;
	private Button botonContinuar;
	private Button botonReintentar;
	private Button botonSalir;

	private boolean gameOver = false;

	// ui_cancel es la accion que Godot ya trae mapeada a Esc
	private final StringName accionPausa = new StringName("ui_cancel");
	private final StringName grupoMenu = new StringName("menu");

	@Register
	@Override
	public void _ready() {
		// El menu y sus hijos siguen funcionando mientras el juego esta en pausa
		setProcessMode(Node.ProcessMode.ALWAYS);
		addToGroup(grupoMenu);

		fondo = (Control) getNodeOrNull(new NodePath("Fondo"));
		titulo = (Label) getNodeOrNull(new NodePath("Fondo/Caja/Titulo"));
		botonContinuar = (Button) getNodeOrNull(new NodePath("Fondo/Caja/BotonContinuar"));
		botonReintentar = (Button) getNodeOrNull(new NodePath("Fondo/Caja/BotonReintentar"));
		botonSalir = (Button) getNodeOrNull(new NodePath("Fondo/Caja/BotonSalir"));

		if (fondo == null) GD.printErr("MenuPausa: no encontre el nodo hijo 'Fondo'");
		if (titulo == null) GD.printErr("MenuPausa: no encontre 'Fondo/Caja/Titulo'");
		if (botonContinuar == null) GD.printErr("MenuPausa: no encontre 'Fondo/Caja/BotonContinuar'");
		if (botonReintentar == null) GD.printErr("MenuPausa: no encontre 'Fondo/Caja/BotonReintentar'");
		if (botonSalir == null) GD.printErr("MenuPausa: no encontre 'Fondo/Caja/BotonSalir'");

		// Conectamos las senales 'pressed' por codigo (no hace falta hacerlo en el editor)
		if (botonContinuar != null) botonContinuar.getPressed().connect(LambdaCallable0.create(() -> onContinuar()));
		if (botonReintentar != null) botonReintentar.getPressed().connect(LambdaCallable0.create(() -> onReintentar()));
		if (botonSalir != null) botonSalir.getPressed().connect(LambdaCallable0.create(() -> onSalir()));

		if (fondo != null) fondo.setVisible(false);
	}

	@Register
	@Override
	public void _process(double delta) {
		if (gameOver) return; // en game over, Esc no hace nada

		if (Input.isActionJustPressed(accionPausa)) {
			if (getTree().isPaused()) {
				onContinuar();
			} else {
				mostrar("Pausa", true);
			}
		}
	}

	// Lo llama Potato cuando su vida llega a 0
	public void mostrarGameOver() {
		gameOver = true;
		mostrar("Te moriste :(", false);
	}

	private void mostrar(String texto, boolean conContinuar) {
		if (titulo != null) titulo.setText(texto);
		if (botonContinuar != null) botonContinuar.setVisible(conContinuar);
		if (fondo != null) fondo.setVisible(true);
		getTree().setPause(true);
	}

	@Register
	public void onContinuar() {
		if (gameOver) return;
		getTree().setPause(false);
		if (fondo != null) fondo.setVisible(false);
	}

	@Register
	public void onReintentar() {
		// La pausa se conserva al recargar la escena: hay que quitarla ANTES,
		// si no la escena nueva empezaria congelada.
		getTree().setPause(false);
		getTree().reloadCurrentScene();
	}

	@Register
	public void onSalir() {
		getTree();
	}
}
