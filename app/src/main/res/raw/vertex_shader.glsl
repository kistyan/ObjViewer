attribute vec4 a_Position;
attribute vec3 a_VertexNormal;
attribute vec2 a_TextureCoordinate;
uniform mat4 u_ModelMatrix, u_ModelViewProjectionMatrix;
uniform mat3 u_NormalMatrix;

varying vec2 v_TextureCoordinate;
varying vec3 v_FragPosition, v_FragNormal;

void main() {
    gl_Position = u_ModelViewProjectionMatrix * a_Position;
    v_TextureCoordinate = a_TextureCoordinate;
    v_FragPosition = vec3(u_ModelMatrix * a_Position);
    v_FragNormal = normalize(u_NormalMatrix * a_VertexNormal);
}
