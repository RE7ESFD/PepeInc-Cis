package Game;

import godot.annotation.Register;
import godot.annotation.Script;
import godot.annotation.Visible;
import godot.api.Input;
import godot.api.InputEvent;
import godot.api.Node2D;
import godot.api.PackedScene;
import godot.api.Texture2D;
import godot.core.StringName;
import godot.core.Vector2;

@Script
public class Shooter extends Node2D {

    @Visible public PackedScene projectileScene = null;
    @Visible public Texture2D currentProjectileTexture = null;

    private final StringName shootAction = new StringName("shoot");

    @Register
    @Override
    public void _input(InputEvent event) {
        if (Input.INSTANCE.isActionJustPressed(shootAction, false)) {
            fireProjectile();
        }
    }

    private void fireProjectile() {
        if (projectileScene == null) return;

        // Instanciar y castear al tipo correcto
        Projectile projectile = (Projectile) projectileScene.instantiate();
        if (projectile == null) return;

        // Calcular dirección
        Vector2 mousePos = getGlobalMousePosition();
        Vector2 dir = mousePos.minus(getGlobalPosition()).normalized();

        // Configurar proyectil
        projectile.direction = dir;
        projectile.setGlobalPosition(getGlobalPosition());
        projectile.customTexture = currentProjectileTexture;

        // Añadir a la escena
        if (getTree() != null && getTree().getCurrentScene() != null) {
            getTree().getCurrentScene().addChild(projectile, false);
        }
    }
}