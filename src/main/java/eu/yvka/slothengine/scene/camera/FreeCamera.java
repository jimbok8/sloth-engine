package eu.yvka.slothengine.scene.camera;

import eu.yvka.slothengine.engine.Engine;
import eu.yvka.slothengine.input.InputManager;
import eu.yvka.slothengine.input.provider.KeyInputProvider;
import eu.yvka.slothengine.input.provider.MouseInputProvider;
import eu.yvka.slothengine.math.MathUtils;
import eu.yvka.slothengine.window.Window;

import org.joml.Vector2d;
import org.joml.Vector3f;

public class FreeCamera extends Camera {

	private float horizontalAngle;
	private float verticalAngle;
	private float mouseSpeed = 5.0f;

	private double initialX = - 1;
	private double initialY = -1;

	private double lastXPos = - 1;
	private double lastYPos = -1;

	public FreeCamera() {
		horizontalAngle = (float) Math.PI;
		position.set(0, 0, 0);
		viewMatrix.identity().lookAt(position, new Vector3f(position).add(direction), up);
	}

	@Override
	public void update(float time) {
		InputManager inputManager = Engine.getInputManager();
		Window window = Engine.getPrimaryWindow();

		int width = window.getWidth();
		int height = window.getHeight();

		if (inputManager.isMouseButtonPressed(MouseInputProvider.MouseButton.Second)) {
			Vector2d mousePos = inputManager.getMousePosition();

			// When the mouse button pressed the first time
			if (lastXPos == -1 || lastYPos == -1) {
				initialX = lastXPos = mousePos.x;
				initialY = lastYPos = mousePos.y;
			}

			float offsetX = (float) (lastXPos - mousePos.x);
			float offsetY = (float) (lastYPos - mousePos.y);
			lastXPos = mousePos.x;
			lastYPos = mousePos.y;

			offsetX /= mouseSpeed;
			offsetY /= mouseSpeed;

			horizontalAngle += mouseSpeed * time * offsetX;
			verticalAngle += mouseSpeed * time * offsetY;

			// Prevents the Camera from vertically flipping
			verticalAngle = MathUtils.clamp(verticalAngle, -1.5f, 1.5f);
		} else {
			if (lastXPos != -1 && lastYPos != -1) {
				inputManager.setMousePosition((int)initialX, (int)initialY);
			}
			lastXPos = -1;
			lastYPos = -1;
		}

		direction.set(
			(float) (Math.cos(verticalAngle) * Math.sin(horizontalAngle)),
			(float) (Math.sin(verticalAngle)),
			(float) (Math.cos(verticalAngle) * Math.cos(horizontalAngle))
		).normalize();


		direction.cross(up, right);
		right.cross(direction, directionUp);

		viewMatrix.identity().lookAt(position, new Vector3f(position).add(direction), up);
		// Capture the cursor in the primary window

		if (inputManager.isMouseButtonPressed(MouseInputProvider.MouseButton.Second)) {
			inputHandling(time);
		}
	}

	private void inputHandling(float time) {
		InputManager inputManager = Engine.getInputManager();
		if (inputManager.isKeyPressed(KeyInputProvider.KeyButton.W)) {
			walk(mouseSpeed, time);
		}

		if (inputManager.isKeyPressed(KeyInputProvider.KeyButton.S)) {
			walk(-mouseSpeed, time);
		}

		if (inputManager.isKeyPressed(KeyInputProvider.KeyButton.A)) {
			strafe(-mouseSpeed, time);
		}

		if (inputManager.isKeyPressed(KeyInputProvider.KeyButton.D)) {
			strafe(mouseSpeed, time);
		}

		if (inputManager.isKeyPressed(KeyInputProvider.KeyButton.R)) {
			lift(mouseSpeed, time);
		}

		if (inputManager.isKeyPressed(KeyInputProvider.KeyButton.F)) {
			lift(-mouseSpeed, time);
		}
	}

	public void walk(float speed, float time) {
		Vector3f dir = new Vector3f(direction).mul(time * speed);
		position.add(dir);
	}

	public void strafe(float speed, float time) {
		Vector3f dir = new Vector3f(right).mul(time * speed);
		position.add(dir);
	}

	public void lift(float speed, float time) {
		Vector3f dir = new Vector3f(directionUp).mul(time * speed);
		position.add(dir);
	}

	public void setTarget(Vector3f direction) {
		Vector3f dir = new Vector3f(position).sub(direction).normalize();
		// Converts vector into unit sphere coordinate
		horizontalAngle = (float) (Math.atan2(dir.x, dir.z) + Math.PI);
		verticalAngle = (float) (-Math.atan(dir.y / Math.sqrt(dir.z * dir.z + dir.x * dir.x)));

	}

}
