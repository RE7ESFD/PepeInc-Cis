extends Node2D

# Arrastra tu escena Proyectil.tscn aquí en el Inspector
@export var escena_proyectil: PackedScene

func _ready() -> void:
	# Configura el clic izquierdo de forma automática si no existe
	if not InputMap.has_action("disparar"):
		InputMap.add_action("disparar")
		var evento_click = InputEventMouseButton.new()
		evento_click.button_index = MOUSE_BUTTON_LEFT
		InputMap.action_add_event("disparar", evento_click)

func _input(event: InputEvent) -> void:
	# Detecta el clic en cualquier parte de la pantalla
	if event.is_action_pressed("disparar"):
		disparar_desde_mouse()

func disparar_desde_mouse() -> void:
	if not escena_proyectil: return
	
	var bala = escena_proyectil.instantiate()
	
	# El proyectil aparece en el centro de tu pantalla de prueba (ej. x: 576, y: 324)
	bala.global_position = get_viewport_rect().size / 2
	
	# Calcula el ángulo exacto desde el centro de la pantalla hacia el cursor del mouse
	var posicion_mouse = get_global_mouse_position()
	bala.global_rotation = (posicion_mouse - bala.global_position).angle()
	
	add_child(bala)
