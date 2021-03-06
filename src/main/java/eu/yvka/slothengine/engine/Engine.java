package eu.yvka.slothengine.engine;

import eu.yvka.slothengine.geometry.Mesh;
import eu.yvka.slothengine.geometry.MeshRepository;
import eu.yvka.slothengine.input.InputManager;
import eu.yvka.slothengine.input.InputManagerFactory;
import eu.yvka.slothengine.material.MaterialManager;
import eu.yvka.slothengine.renderer.Renderer;
import eu.yvka.slothengine.renderer.RendererManager;
import eu.yvka.slothengine.shader.Shader;
import eu.yvka.slothengine.shader.ShaderErrorListener;
import eu.yvka.slothengine.shader.ShaderManager;
import eu.yvka.slothengine.texture.TextureManager;
import eu.yvka.slothengine.utils.IOUtils;
import eu.yvka.slothengine.window.Window;
import eu.yvka.slothengine.window.WindowManager;
import org.lwjgl.glfw.GLFW;

import javax.xml.transform.ErrorListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.LogManager;

import static org.lwjgl.glfw.GLFW.glfwInit;

public class Engine {

	public static final String DEFAULT_LOG_FILE = "logging.properties";


	static {
		enableLogging();
	}


	private static List<ShaderErrorListener> errorListeners = new ArrayList<>();
	private static ShaderManager shaderRepository;
	private static MeshRepository meshRepository;
	private static RendererManager renderManager;
	private static WindowManager windowManager;
	private static TextureManager textureManager;
	private static InputManager inputManager;
	private static MaterialManager materialManager;
	private static boolean initialized;
	private static List<EngineComponent> components = new ArrayList<>();
	private static Queue<Runnable> runWhenReadyQueue = new ConcurrentLinkedQueue<>();

	private static AppSettings appSettings = new AppSettings();

	private Engine() {}

	/**
	 * Starts the engine and all its components.
	 *
	 * @param settings the settings to start the application
	 */
	static
	public void start(AppSettings settings) {
		appSettings = settings;
		if (initialized) {
			throw new IllegalArgumentException("The Engine is already initialized");
		}

		if ( !glfwInit()) {
			throw new IllegalStateException("GLFW could not initialized");
		}
		registerDefaultComponents();

		initialized = true;
	}

	/**
	 * Register the default components that the engine provides.
	 */
	private static void registerDefaultComponents() {
		windowManager = register(new WindowManager(), WindowManager.class);
		textureManager = register(new TextureManager(), TextureManager.class);
		renderManager = register(new RendererManager(), RendererManager.class);
		shaderRepository = register(new ShaderManager(), ShaderManager.class);
		meshRepository = register(new MeshRepository(), MeshRepository.class);
		materialManager = register(new MaterialManager(), MaterialManager.class);
		onInit();

		JavaFXOffscreenSupport offscreenSupport = appSettings.get(JavaFXOffscreenSupport.JAVAFX_OFFSCREEN_SUPPORT, JavaFXOffscreenSupport.class);
		if (offscreenSupport != null) {
			inputManager = register(InputManagerFactory.createInputManager(offscreenSupport.getNode()), InputManager.class);
		} else {
			inputManager = register(InputManagerFactory.createInputManager(Engine.getPrimaryWindow()), InputManager.class);
		}
		inputManager.initialize();

		if (offscreenSupport != null) {
			register(offscreenSupport);
		}

	}

	/**
	 * Register and initialize a engine component.
	 *
	 * @param component the component add
     */
	public static void register(EngineComponent component) {
		register(component, component.getClass());
		if (!component.isInitialized()) {
			component.initialize();
		}
	}

	private static <T extends EngineComponent> T register(EngineComponent component, Class<T> type) {
		if (! components.contains(component)) {
			components.add(component);
		}
		return type.cast(component);
	}

	/**
	 * Unregister and shutdown a engine component.
	 *
	 * @param component the component to remove
	 */
	public static void unregister(EngineComponent component) {
		if (components.contains(component) && component.isInitialized() && isCoreComponent(component)) {
			components.remove(component);
			if (component.isInitialized()) {
				component.shutdown();
			}
		}
	}

	private static boolean isCoreComponent(EngineComponent component) {
		return
			component == windowManager    ||
			component == renderManager    ||
			component == textureManager   ||
			component == shaderRepository ||
			component == meshRepository   ||
			component == materialManager  ||
			component == inputManager;

	}

	/**
	 * Shutdown the engine and all it's components
	 */
	static
	public void shutdown() {
		if (initialized) {
			onShutdown();
			components.clear();
			GLFW.glfwTerminate();
			initialized = false;
		}
	}

	static
	public ShaderManager shaderManager() {
		return shaderRepository;
	}

	static
	public AppSettings getSettings() {
		return appSettings;
	}

	static
	public Shader getShader(String name) {
		return shaderRepository.getShader(name);
	}

	static
	public  WindowManager windowManager() {
		return windowManager;
	}

	static
	public RendererManager renderManager() {
		return renderManager;
	}

	static
	public MaterialManager materialManager() {
		return materialManager;
	}

	static
	public Window getPrimaryWindow() {
		return windowManager.getPrimaryWindow();
	}

	static
	public Renderer getCurrentRenderer() {
		return renderManager.getRenderer();
	}

	static
	public Mesh getMeshFromAssets(String fileName) {
		return meshRepository.getMeshFromAssets(fileName);
	}

	static
	public Mesh getMesh(String fileName) {
		return meshRepository.getMesh(fileName);
	}

	static
	public TextureManager textureManager() {
		return textureManager;
	}

	static
	public void registerShaderErrorListener(ShaderErrorListener shaderErrorListener) {
		if (! errorListeners.contains(shaderErrorListener)) {
			errorListeners.add(shaderErrorListener);
		}
	}

	static
	public void unregisterShaderErrorListener(ShaderErrorListener shaderErrorListener) {
		if (errorListeners.contains(shaderErrorListener)) {
			errorListeners.remove(shaderErrorListener);
		}
	}

	static
	public void notifyShaderErrorListeners(Consumer<ShaderErrorListener> notifier) {
		errorListeners.forEach(notifier);
	}

	static void onInit() {
		components.stream().filter((c) -> !c.isInitialized()).forEach(EngineComponent::initialize);
	}

	static void onFrameStart() {
		while (! runWhenReadyQueue.isEmpty()) {
			runWhenReadyQueue.poll().run();
		}

		for (EngineComponent component : components) {
			if (component.isInitialized()) component.onFrameStart();
		}

		// components.stream().filter(EngineComponent::isInitialized).forEach(EngineComponent::onFrameStart);
	}

	static void onUpdate(float elapsedTime) {
		for (EngineComponent component : components) {
			if (component.isInitialized()) component.onUpdate(elapsedTime);
		}
		// components.stream().filter(EngineComponent::isInitialized).forEach((c) -> c.onUpdate(elapsedTime));
	}

	static void onBeforeRender(float elapsedTime) {
		for (EngineComponent component : components) {
			if (component.isInitialized()) component.onBeforeRender(elapsedTime);
		}
		// components.stream().filter(EngineComponent::isInitialized).forEach((c) -> c.onBeforeRender(elapsedTime));
	}

	static void onRender(float elapsedTime) {
		for (EngineComponent component : components) {
			if (component.isInitialized()) component.onRender(elapsedTime);
		}
		// components.stream().filter(EngineComponent::isInitialized).forEach((c) -> c.onRender(elapsedTime));
	}

	static void onAfterRender(float elapsedTime) {
		for (EngineComponent component : components) {
			if (component.isInitialized())component.onAfterRender(elapsedTime);
		}
		// components.stream().filter(EngineComponent::isInitialized).forEach((c) -> c.onAfterRender(elapsedTime));
	}

	static void onFrameEnd() {
		for (EngineComponent component : components) {
			if (component.isInitialized()) component.onFrameEnd();
		}
		//components.stream().filter(EngineComponent::isInitialized).forEach(EngineComponent::onFrameEnd);
	}

	static void onShutdown() {
		for (EngineComponent component : components) {
			if (component.isInitialized()) component.shutdown();
		}
		// components.stream().filter(EngineComponent::isInitialized).forEach(EngineComponent::shutdown);
	}

	public static boolean shouldExit() {
		return windowManager.getPrimaryWindow().shouldClose();
	}

	public static void requestExit() {
		windowManager.getPrimaryWindow().requestClose();
	}

	public static InputManager getInputManager() {
		return inputManager;
	}

	public static void runWhenReady(Runnable engineInitializedHandler) {
		runWhenReadyQueue.offer(engineInitializedHandler);
	}

	private static void enableLogging() {

		if (System.getProperty("java.util.logging.config.file") == null) {

			InputStream logConfigStream = null;

			try {
				File logFile = new File(DEFAULT_LOG_FILE);
				if (logFile.isFile()) {
					logConfigStream = new FileInputStream(logFile);
				} else {
					URL localResourceFile = ClassLoader.getSystemResource(DEFAULT_LOG_FILE);
					if (localResourceFile != null) {
						logConfigStream = localResourceFile.openStream();
					}
				}

				if (logConfigStream != null) {
					LogManager.getLogManager().readConfiguration(logConfigStream);
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				IOUtils.closeQuietly(logConfigStream);
			}
		} else {
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
