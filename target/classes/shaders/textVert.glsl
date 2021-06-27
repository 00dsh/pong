#version 330 core
layout (location=0) in vec4 aCoords;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 transform;

out vec2 texCoords;

void main() {
    gl_Position = projection * model * transform * vec4(aCoords.xy, 0.0f, 1.0f);
    texCoords = aCoords.zw;
}