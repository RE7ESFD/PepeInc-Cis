   shader_type canvas_item;
   uniform float threshold : hint_range(0.0, 1.0) = 0.92;

   void fragment() {
       vec4 c = texture(TEXTURE, UV);
       float m = min(c.r, min(c.g, c.b));
       if (m > threshold) discard;
       COLOR = c;
   }
