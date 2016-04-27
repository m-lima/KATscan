#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler1D transferFunction;

uniform int numSamples;
uniform int lodMultiplier;

uniform mat4 model;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

int actualSamples = numSamples * lodMultiplier;
float stepSize = 1f / float(actualSamples);

out vec4 fragColor;

float rand(vec2 co){
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = (inverse(model) * vec4(effectiveEyePos, 1.0)).xyz;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 stepValue = rayDirection * stepSize;
    vec3 pos = vertexOut + rand(gl_FragCoord.xy) * stepValue;

    float density;
    vec3 coord;
    fragColor = vec4(0.0);
    vec4 transferColor;
    for (int i = 0; i < actualSamples * 3; ++i, pos += stepValue) {
        coord = pos / ratio + 0.5;
        if (coord.x < 0.0 || coord.x > 1.0 ||
            coord.y < 0.0 || coord.y > 1.0 ||
            coord.z < 0.0 || coord.z > 1.0) {
            break;
        }
        
        density = texture(volumeTexture, coord).x;
        if (density <= 0.0) continue;
        transferColor = texture(transferFunction, density);

        transferColor.a /= lodMultiplier;
        if (transferColor.a <= 0.0) continue;

        fragColor.rgb = mix(fragColor.rgb, transferColor.rgb, transferColor.a);
        fragColor.a += transferColor.a;

        if (fragColor.a >= 1.0) {
            break;
        }
    }

//#define COLOR_CUBE
#ifdef COLOR_CUBE
    vec3 maxVec = vec3(0.5) / ratio;
    vec3 minVec = vec3(-0.5) / ratio;
    vec4 saturated = vec4((vertexOut.x == maxVec.x || vertexOut.x == minVec.x) ? 1.0 : 0.0,
                          (vertexOut.y == maxVec.y || vertexOut.y == minVec.y) ? 1.0 : 0.0,
                          (vertexOut.z == maxVec.z || vertexOut.z == minVec.z) ? 1.0 : 0.0,
                          0.25);
    fragColor += saturated;
#endif
}