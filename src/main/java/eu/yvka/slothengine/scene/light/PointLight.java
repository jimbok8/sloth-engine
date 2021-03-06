package eu.yvka.slothengine.scene.light;


import eu.yvka.slothengine.shader.Shader;

/**
 * Entity Point Light
 */
public class PointLight extends Light{
	public PointLight(String id) {
		super(id, LightType.Point);
	}

	@Override
	protected void passToShader(int lightId, Shader shader) {
		super.passToShader(lightId, shader);
		getLightUniform(lightId, shader, LIGHT_UNIFORM_POSITION).setValue(getWorldPosition());
	}
}
