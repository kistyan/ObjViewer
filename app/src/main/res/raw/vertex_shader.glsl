attribute vec4 a_Position;
attribute vec3 a_VertexNormal;
attribute vec2 a_TextureCoordinate;
attribute vec3 a_FaceTangent;
uniform mat4 u_ModelMatrix, u_ModelViewProjectionMatrix;
uniform mat3 u_NormalMatrix;

varying vec2 v_TextureCoordinate;
varying vec3 v_FragPosition;
varying mat3 v_TBNMatrix;

void main() {
    gl_Position = u_ModelViewProjectionMatrix * a_Position;
    v_TextureCoordinate = a_TextureCoordinate;
    v_FragPosition = vec3(u_ModelMatrix * a_Position);

    vec3 T = normalize(u_NormalMatrix * a_FaceTangent);
    vec3 N = normalize(u_NormalMatrix * a_VertexNormal);
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(N, T);
    v_TBNMatrix = mat3(T, B, N);
}
