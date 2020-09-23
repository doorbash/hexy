attribute vec2 a_position;    
attribute vec2 a_texCoord0;

uniform vec4 a_tint;
uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {                            
   v_color = a_tint; 
   v_texCoords = a_texCoord0; 
   gl_Position =  u_projTrans * vec4(a_position.xy, 0.0, 1.0);  
}                            
