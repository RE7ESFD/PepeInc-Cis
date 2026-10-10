extends Control

func _on_button_pressed():
	get_tree().change_scene_to_file("res://Scenes/map.tscn")
	
	
func _in_button_pressed():
	get_tree().change_scene_to_file("res://Scenes/settings.tscn")
	
