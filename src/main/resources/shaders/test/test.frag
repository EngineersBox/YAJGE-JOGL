#version 330

out vec4 fragColor;
in vec4 ourColor;

void main() {
    fragColor = ourColor;
}