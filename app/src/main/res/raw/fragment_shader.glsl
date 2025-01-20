precision mediump float;

#define POINT_LIGHT_COUNT %d
#define SPOT_LIGHT_COUNT %d
#define DIRECTIONAL_LIGHT_COUNT %d

struct PointLight {
    vec3 color;
    vec3 position;
    float intensity;
};

struct SpotLight {
    vec3 color;
    vec3 position;
    vec3 direction;
    float intensity, angle;
};

struct DirectionalLight {
    vec3 color;
    vec3 direction;
};

struct Material {
    vec3 ambientColor, diffuseColor, specularColor;
    float dissolve, specularHighlights;
    sampler2D ambientTexture, diffuseTexture, specularTexture, dissolveTexture;
};

#if POINT_LIGHT_COUNT > 0
uniform PointLight u_PointLighting[POINT_LIGHT_COUNT];
#endif

#if SPOT_LIGHT_COUNT > 0
uniform SpotLight u_SpotLighting[SPOT_LIGHT_COUNT];
#endif

#if DIRECTIONAL_LIGHT_COUNT > 0
uniform DirectionalLight u_DirectionalLighting[DIRECTIONAL_LIGHT_COUNT];
#endif

uniform vec3 u_CameraPosition;
uniform Material u_Material;

varying vec2 v_TextureCoordinate;
varying vec3 v_FragPosition, v_FragNormal;

void main() {
    vec3 diffuse = vec3(0, 0, 0), specular = vec3(0, 0, 0);

    #if POINT_LIGHT_COUNT > 0
    for (int pointLightIndex = 0; pointLightIndex < POINT_LIGHT_COUNT; pointLightIndex++) {
        vec3 lightColor = u_PointLighting[pointLightIndex].color;
        vec3 lightOffset = u_PointLighting[pointLightIndex].position - v_FragPosition;
        vec3 lightDirection = normalize(lightOffset);
        float lightDistance = length(lightOffset);
        float lightIntensity = u_PointLighting[pointLightIndex].intensity
                / (lightDistance * lightDistance);
        diffuse += max(dot(v_FragNormal, lightDirection), 0.0)
                * lightColor * lightIntensity;
        vec3 cameraDirection = normalize(u_CameraPosition - v_FragPosition);
        vec3 reflectDirection = reflect(-lightDirection, v_FragNormal);
        specular += pow(max(dot(cameraDirection, reflectDirection), 0.0),
                u_Material.specularHighlights) * u_Material.specularColor * lightColor
                * lightIntensity;
    }
    #endif

    #if SPOT_LIGHT_COUNT > 0
    for (int spotLightIndex = 0; spotLightIndex < SPOT_LIGHT_COUNT; spotLightIndex++) {
        vec3 lightColor = u_SpotLighting[spotLightIndex].color;
        vec3 lightOffset = u_SpotLighting[spotLightIndex].position - v_FragPosition;
        vec3 lightDirection = normalize(lightOffset);
        float lightDistance = length(lightOffset);
        float lightIntensity = u_SpotLighting[spotLightIndex].intensity
                / (lightDistance * lightDistance);
        if (acos(dot(lightDirection, normalize(-u_SpotLighting[spotLightIndex].direction)))
                >= u_SpotLighting[spotLightIndex].angle)
            break;
        diffuse += max(dot(v_FragNormal, lightDirection), 0.0) * lightColor * lightIntensity;
        vec3 cameraDirection = normalize(u_CameraPosition - v_FragPosition);
        vec3 reflectDirection = reflect(-lightDirection, v_FragNormal);
        specular += pow(max(dot(cameraDirection, reflectDirection), 0.0),
                u_Material.specularHighlights) * u_Material.specularColor * lightColor
                * lightIntensity;
    }
    #endif

    #if DIRECTIONAL_LIGHT_COUNT > 0
    for (
            int directionalLightIndex = 0;
            directionalLightIndex < DIRECTIONAL_LIGHT_COUNT;
            directionalLightIndex++
    ) {
        vec3 lightColor = u_DirectionalLighting[directionalLightIndex].color;
        vec3 lightDirection = normalize(-u_DirectionalLighting[0].direction);
        diffuse += max(dot(v_FragNormal, lightDirection), 0.0)* lightColor;
        vec3 cameraDirection = normalize(u_CameraPosition - v_FragPosition);
        vec3 reflectDirection = reflect(-lightDirection, v_FragNormal);
        specular += pow(max(dot(cameraDirection, reflectDirection), 0.0)
                u_Material.specularHighlights) * u_Material.specularColor * lightColor;
    }
    #endif

    gl_FragColor = vec4(u_Material.ambientColor * texture2D(
            u_Material.ambientTexture,
            vec2(v_TextureCoordinate.x, 1.0 - v_TextureCoordinate.y)
    ).rgb + diffuse * u_Material.diffuseColor * texture2D(
            u_Material.diffuseTexture,
            vec2(v_TextureCoordinate.x, 1.0 - v_TextureCoordinate.y)
    ).rgb + specular * u_Material.specularColor * texture2D(
            u_Material.specularTexture,
            vec2(v_TextureCoordinate.x, 1.0 - v_TextureCoordinate.y)
    ).rgb, u_Material.dissolve * texture2D(
            u_Material.dissolveTexture,
            vec2(v_TextureCoordinate.x, 1.0 - v_TextureCoordinate.y)
    ).r);
}
