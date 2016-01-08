package shader.source;

import renderer.Renderer;
import shader.ShaderType;
import shader.source.ShaderSource;

public class StringShaderSource extends ShaderSource {

	private String source;
	private String name;
	private long modifiedTimestamp = 0;

	public StringShaderSource(String name, ShaderType type, String source) {
		super(type);
		this.source = source;
		this.name = name;
		markAsModified();
	}

	@Override
	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		enableUpdateRequired();
		this.source = source;
	}

	@Override
	public long lastModified() {
		return modifiedTimestamp;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void deleteObject(Renderer renderer) {
		renderer.deleteShaderSource(this);
	}

	@Override
	public void resetObject() {
		enableUpdateRequired();
	}

	private void markAsModified() {
		enableUpdateRequired();
		modifiedTimestamp = System.currentTimeMillis();
	}

}
