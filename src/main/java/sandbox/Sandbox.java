package sandbox;


import geometry.Mesh;
import geometry.VertexBuffer;
import math.Color;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWKeyCallback;
import renderer.RenderState;
import renderer.Renderer;
import renderer.RendererManager;
import renderer.RendererType;
import shader.Shader;
import shader.ShaderType;
import shader.source.FileShaderSource;
import utils.BufferUtils;
import window.Window;
import window.WindowManager;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

public class Sandbox {

	public void run() throws Exception {

		GLFWKeyCallback keyCallback;

		Window window = WindowManager.get()
			.setTitle("Window the first")
			.setSize(1024, 768)
			.setResizeable(true)
			.setGLContextVersion(3, 0)
			.build()
			.enable();

		glfwSetKeyCallback(window.getWindowId(), keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW_KEY_ESCAPE) {
					glfwSetWindowShouldClose(window, GL_TRUE);
				}
			}
		});

		Renderer renderer = RendererManager.getRenderer(RendererType.Lwjgl3);

		Shader diffuseShader = new Shader();
		diffuseShader.addSource(new FileShaderSource(ShaderType.VERTEX, new File("assets/shaders/TransformVertexShader.vert")));
		diffuseShader.addSource(new FileShaderSource(ShaderType.FRAGMENT, new File("assets/shaders/ColorShader.frag")));

		/***
		 *  A---D    A---D      D
		 *	|  /|    |  /      /|
		 *	| / | -> | /  +   / |
		 *	|/  |    |/      /  |
		 *	B---C    B      B---C
		 *
		 *    A'--D'
		 *   /|  /|
		 *  A---D |
		 *	| / | C'
		 *	|/  |/
		 *	B---C
		 *
		 */
		FloatBuffer triangleBuffer = BufferUtils.createBuffer(new float[] {

			-0.5f,  0.5f, 0.5f, 0.0f, 0.5f, 0.0f, // Front Top Left     = A
			-0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, // Front Bottom Left	= B
			 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.0f, // Front Bottom Right = C
			 0.5f,  0.5f, 0.5f, 0.5f, 0.0f, 0.0f  // Front Top Right 	= D

			-0.5f,  0.5f, -0.5f, 0.0f, 0.5f, 0.0f, // Back Top Left     = A'
			-0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // Back Bottom Left	= B'
			0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.0f, // Back Bottom Right  = C'
			0.5f,  0.5f, -0.5f, 0.5f, 0.0f, 0.0f  // Back Top Right 	= D'
		});

		final int topLeft = 0;
		final int bottomLeft = 1;
		final int bottomRight = 2;
		final int topRight = 3;

		final int topLeftB = 4;
		final int bottomLeftB = 5;
		final int bottomRightB = 6;
		final int topRightB = 7;
		ByteBuffer indiceBuffer = BufferUtils.createBuffer(new byte[] {
			// Front Face
			topLeft, bottomLeft, topRight,
			topRight, bottomLeft, bottomRight,

			// Face Right
			topRight, bottomRight, bottomRightB,
			topRightB, topRight, bottomRight,


			// Face Bottom
			bottomLeftB, bottomLeft, bottomRightB,
			bottomRightB, bottomLeft, bottomLeftB
		});

		Mesh cube = new Mesh();


		cube.setBuffer(VertexBuffer.Type.Vertex, 3, triangleBuffer);
		triangleBuffer.clear();


		Matrix4f v = new Matrix4f()
			.lookAt(0.0f, 0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		Matrix4f m = new Matrix4f();
		Matrix4f p = new Matrix4f()
			.setPerspective(45.0f, (float) (window.getWidth() / window.getHeight()) ,0.01f, 100.0f);
		Matrix4f mvp =new Matrix4f().mul(p).mul(v).mul(m);


		RenderState state = new RenderState();
		state.setWireframe(false);

		renderer.setClearColor(Color.Gray);
		renderer.applyRenderState(state);

		double lastTime = glfwGetTime();
		while ( !window.shouldClose()) {
			double currentTime = glfwGetTime();
			float elapsedTime = (float) (currentTime - lastTime);
			lastTime = currentTime;

			m.identity().rotateY(elapsedTime * 2);
			mvp.identity().mul(p).mul(v).mul(m);

			renderer.onNewFrame();
			renderer.clearBuffers(true, true, false);

			diffuseShader.getUniform("mvp").setValue(mvp);
			diffuseShader.getUniform("time").setValue(elapsedTime);
			renderer.setShader(diffuseShader);
			renderer.drawMesh(cube, 0, 0);

			window.update();
		}

		renderer.cleanUp();
		keyCallback.release();
		WindowManager.get().clean();
	}

	public static void main(String[] args) throws Exception {
		Sandbox box = new Sandbox();
		box.run();
	}
}
