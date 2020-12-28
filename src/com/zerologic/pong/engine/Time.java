package com.zerologic.pong.engine;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The {@code Time} utility class.
 * @author Dilan Shabani
 */

public class Time {

	private static double oldTime;
	private static double currentTime;
	public static float deltaTime;
	public static float actualTime;

	private static float deltaTimef() {
		oldTime = currentTime;
		currentTime = glfwGetTime();

		deltaTime = (float) (currentTime - oldTime);

		return (float) deltaTime;
	}

	protected static void calcTime() {
		deltaTime = deltaTimef();
		actualTime = (float) glfwGetTime();
	}
}
