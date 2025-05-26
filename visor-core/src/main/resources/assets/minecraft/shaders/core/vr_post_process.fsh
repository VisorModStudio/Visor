#version 150 core

uniform sampler2D Sampler0;

in vec2 texCoordinates;

out vec4 fragColor;


void main(){

    vec4 bkg_color = texture(Sampler0, texCoordinates.st);


    fragColor = bkg_color;

}
