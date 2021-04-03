#version 330 core

layout (location = 0) in vec4 vPos;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat4 transform;

out vec2 ourTexCoord;

void main() {
	gl_Position = projection * model * transform * vec4(vPos.xy, 0.0f, 1.0f);
	ourTexCoord = vec2(vPos.zw);
}