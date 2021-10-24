package lib;

import lib.buffer.Tessellator;
import lib.math.Timer;
import lib.render.Canvas;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static final long NS_PER_SECOND = 1_000_000_000;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            GLFW.glfwTerminate();
            GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(null);
            if (callback != null) {
                callback.free();
            }
        }, "GLFW shutdown hook"));
    }

    protected final long handle;
    protected final int width, height;
    protected boolean cursorOver;
    protected double cursorX, cursorY;
    protected final Canvas canvas;
    protected final Timer timer = new Timer();

    public Window(int width, int height, String title, boolean vsync, String font, int fontSize) {
        this.width = width;
        this.height = height;

        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("GLFW initialization failed");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        if ((handle = GLFW.glfwCreateWindow(width, height, title, NULL, NULL)) == NULL) {
            throw new RuntimeException("GLFW window creation failed");
        }

        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> onKeyButton(key, scancode, action, mods));
        GLFW.glfwSetCharCallback(handle, (window, codepoint) -> onSymbol((char) codepoint));
        GLFW.glfwSetMouseButtonCallback(handle, (window, button, action, mods) -> onMouseButton(button, action, mods));
        GLFW.glfwSetScrollCallback(handle, (window, dx, dy) -> onScroll(dx, dy));
        GLFW.glfwSetCursorEnterCallback(handle, (window, entered) -> onCursorEntered(entered));
        GLFW.glfwSetCursorPosCallback(handle, (window, x, y) -> onCursorMoved(x, y));

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(handle, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

        GLFW.glfwMakeContextCurrent(handle);
        if (vsync) {
            GLFW.glfwSwapInterval(1);
        }

        GL.createCapabilities();
        this.canvas = new Canvas(Tessellator.DEFAULT, width, height, font, fontSize);
    }

    public void setIcon(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer data = STBImage.stbi_load(path, w, h, comp, 4);
            if (data == null) {
                throw new RuntimeException("Failed to load icon from file: " + path, new Throwable(STBImage.stbi_failure_reason()));
            }
            GLFWImage image = GLFWImage.malloc();

            image.set(w.get(), h.get(), data);

            GLFWImage.Buffer images = GLFWImage.malloc(1);
            images.put(0, image);
            GLFW.glfwSetWindowIcon(handle, images);
        }
    }

    public void show() {
        GLFW.glfwShowWindow(handle);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        clearColor();

        long lastFrameTime = System.nanoTime();
        GL11.glViewport(0, 0, width, height);

        while (!GLFW.glfwWindowShouldClose(handle)) {
            GLFW.glfwPollEvents();
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            long frameStartTime = System.nanoTime();
            long duration = frameStartTime - lastFrameTime;
            lastFrameTime = frameStartTime;

            double elapsed = duration / (double) NS_PER_SECOND;

            timer.tick(elapsed);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glOrtho(0, width, height, 0, 100, 300);
            GL11.glTranslatef(0, 0, -200);

            onFrame(elapsed);

            GL11.glPopMatrix();

            GLFW.glfwSwapBuffers(handle);
        }

        Callbacks.glfwFreeCallbacks(handle);
        GLFW.glfwDestroyWindow(handle);
    }

    protected void clearColor() {
        GL11.glClearColor(1F, 1F, 1F, 0F);
    }

    protected void onKeyButton(int key, int scancode, int action, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
            GLFW.glfwSetWindowShouldClose(handle, true);
        }
    }

    protected void onSymbol(char symbol) {

    }

    protected void onMouseButton(int button, int action, int mods) {

    }

    protected void onScroll(double dx, double dy) {

    }

    protected void onCursorEntered(boolean entered) {
        cursorOver = entered;
    }

    protected void onCursorMoved(double x, double y) {
        cursorX = x;
        cursorY = y;
    }

    protected void onFrame(double elapsed) {

    }
}
