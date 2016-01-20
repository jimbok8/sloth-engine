package scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class BaseCamera {

	protected Vector3f position;
	protected Vector3f up;
	protected Vector3f direction;
	protected Vector3f directionUp;
	protected Vector3f right;

	protected Matrix4f viewMatrix;
	protected Matrix4f projectionMatrix;

	protected float fov;
	protected float aspect;
	protected float near;
	protected float far;

	protected float yaw;
	protected float pitch;
	protected float roll;

	public BaseCamera() {
		this.position = new Vector3f();
		this.direction = new Vector3f();
		this.directionUp = new Vector3f();
		this.right = new Vector3f();
		this.up = new Vector3f(0.0f, 1.0f, 0.0f);
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
		fov = 45;
		aspect = 4f / 3f;
		near = 0.1f;
		far = 1000.f;
		yaw = pitch = roll = 0f;
	}

	public abstract void update(float time);


	public void setupProjection(float fov, float aspect, float near, float far) {
		if (this.fov != fov || this.aspect != aspect || this.near != near || this.far != far) {
			projectionMatrix.setPerspective(fov, aspect, near, far);
			this.fov = fov;
			this.aspect = aspect;
			this.near = near;
			this.far = far;
		}
	}

	public void setRotation(float yaw, float pitch, float roll) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public Matrix4f getProjektionMatrix() {
		return projectionMatrix;
	}

	public  Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position.set(position);
	}

	public void setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
	}

	public float getAspectRatio() {
		return aspect;
	}

	public float getFieldOfView() {
		return fov;
	}

}
