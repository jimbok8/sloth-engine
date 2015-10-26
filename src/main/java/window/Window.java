package window;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import util.Cleanable;

import java.nio.IntBuffer;

/**
 * The window class which encapsulates the
 * window which is used to show the rendering result.
 */
public class Window implements Cleanable {
	private long windowId;
	private String title;
	private GLFWWindowSizeCallback sizeCallback;

	private State state;
	private enum State {
		ENABLED,
		DISABLED,
		CLEANED
	}

	Window(long windowId, String title) {
		this.windowId = windowId;
		this.title = title;
		this.state = State.DISABLED;
	}

	public Window setTitle(String title) {
		this.title = title;
		GLFW.glfwSetWindowTitle(windowId, title);
		return this;
	}

	public long getWindowId() {
		return windowId;
	}

	public int getWidth() {
		IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(windowId, widthBuffer, null);
		return widthBuffer.get();
	}

	public Window setWidth(int width) {
		setSize(width, getHeight());
		return this;
	}

	public int getHeight() {
		IntBuffer heightBuffer =  BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(windowId, null, heightBuffer);
		return heightBuffer.get();
	}

	public Window setHeight(int height) {
		setSize(getWidth(), height);
		return this;
	}

	public Window setSize(int width, int height) {

		width = Math.abs(width);
		height = Math.abs(height);

		width = (width < 1) ? 1 : width;
		height = (height < 1) ? 1 : height;

		GLFW.glfwSetWindowSize(windowId, width, height);
		return this;
	}

	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(windowId) == GL11.GL_TRUE;
	}

	public void enable() {

		if (state != State.DISABLED) {
			throw new IllegalStateException("This window was already enabled");
		}

		state = State.ENABLED;

		GLFW.glfwMakeContextCurrent(windowId);
		GLContext.createFromCurrent();
		GLFW.glfwSetWindowSizeCallback(windowId, sizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				updateViewportSize();
			}
		});
		GLFW.glfwShowWindow(windowId);
		updateViewportSize();
	}

	public void disable() {
		if (state == State.DISABLED) {
			throw new IllegalStateException("This window was already disabled");
		}

		GLFW.glfwSetWindowSizeCallback(windowId, null);
		GLFW.glfwMakeContextCurrent(0L);
	}

	public void update() {
		GLFW.glfwSwapBuffers(windowId);
		GLFW.glfwPollEvents();
	}

	@Override
	public void clean() {
		if (state == State.CLEANED) {
			throw new IllegalArgumentException("The Window was already cleaned");
		}

		if (state == State.DISABLED) {
			disable();
		}

		GLFW.glfwDestroyWindow(windowId);
		state = State.CLEANED;
	}

	private void updateViewportSize() {
		GL11.glViewport(0, 0, getWidth(), getHeight());
	}



}