extends Area2D

@export var velocidad: float = 600.0

func _physics_process(delta: float) -> void:
	# Mueve el proyectil hacia adelante basándose en su rotación actual
	position += transform.x * velocidad * delta

func _on_visible_on_screen_notifier_2d_screen_exited() -> void:
	# Optimización: Elimina la bala si sale de la pantalla
	queue_free()

func _on_body_entered(body: Node2D) -> void:
	# Lógica al impactar con un cuerpo (ej. enemigo o mapa)
	if body.is_in_group("enemigos"):
		body.recibir_danio() # Llama a una función del enemigo
	
	# Destruye el proyectil tras el impacto
	queue_free()
