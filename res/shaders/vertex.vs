#version 330 core

layout (location = 0) in vec3 vPos;
layout (location = 1) in vec2 texCoord;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat4 transform;

out vec2 ourTexCoord;

void main() {
	gl_Position = projection * model * transform * vec4(vPos, 1.0f);
	ourTexCoord = texCoord;
}