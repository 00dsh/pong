package com.zerologic.pong.engine;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The {@code Time} utility class.
 * @author Dilan Shabani
 */

public class Time {

	private static double currentTime;
	private static float deltaTime;
	public static float actualTime;

	public static float deltaTimef() {
		return deltaTime;
	}

	public static void calcTime() {
		double oldTime = currentTime;
		currentTime = glfwGetTime();

		deltaTime = (float) (currentTime - oldTime);

		//deltaTime = deltaTimef();
		actualTime = (float) glfwGetTime();
	}
}
