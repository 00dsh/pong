package com.zerologic.pong.engine;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The {@code Time} utility class.
 * @author Dilan Shabani
 */

public class Time {

	private static double oldTime = 0;
	private static double currentTime = 0;
	private static float deltaTime;

	public static float deltaTimef() {
		return deltaTime;
	}

	public static void calcTime() {
		oldTime = currentTime;
		currentTime = glfwGetTime();

		deltaTime = (float) (currentTime - oldTime);
	}
}
