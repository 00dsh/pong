package com.zerologic.pong.engine;

import static org.lwjgl.opengl.GL46.*;

import java.io.*;

import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * The {@code ShaderProgram} class that organizes and has all functions
 * pertaining to the shaders. Handles loading as well as compilation and linking
 * of the shaders.
 * 
 * @author Dilan Shabani
 */

public class ShaderProgram {

	// Creating handles/IDs for the shaders + program
	static int vertexShader;
	static int fragmentShader;
	public static int ID;
	
	// Load the shader source files into the strings to be read.
	static String vertexShaderSource   = ShaderProgram.load("./res/shaders/vertex.vs");
	static String fragmentShaderSource = ShaderProgram.load("./res/shaders/fragment.fs");

	// Matrices for view transforms and vertex transformations.
	public static Matrix4f projection = new Matrix4f();
	public static Matrix4f model      = new Matrix4f();
	public static Matrix4f transform  = new Matrix4f();

	static int status;

	/**
	 * The {@code init()} function initializes, creates and compiles the given
	 * shaders, which then also creates the shader program to link and run these
	 * shaders.
	 */

	public static void init() {
		// Creation and compilation of the shaders.
		vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexShaderSource);
		glCompileShader(vertexShader);

		status = glGetShaderi(vertexShader, GL_COMPILE_STATUS);

		if (status != 1)
			System.err.println("Vertex shader compiling error:\n" + glGetShaderInfoLog(vertexShader));

		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentShaderSource);
		glCompileShader(fragmentShader);

		status = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);

		if (status != 1)
			System.err.println("Fragment shader compiling error:\n" + glGetShaderInfoLog(fragmentShader));

		ID = glCreateProgram();
		glAttachShader(ID, vertexShader);
		glAttachShader(ID, fragmentShader);

		glLinkProgram(ID);
		glUseProgram(ID);

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);

		// Initialize the projection matrix.
		projection.setOrtho(0.0f, Game.win_width, Game.win_height, 0.0f, -1.0f, 1.0f);

		updateProjection();

		setMat4("projection", projection);
		setMat4("model", model);
		setMat4("transform", transform);
		setVec4("ourColor", new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
	}

	/**
	 * @param uniform  The name of the uniform to be modified.
	 * @param matrix4f The {@code Matrix4f} to be passed into the program shader so
	 *                 it can be set/modified.
	 */

	public static void setMat4(String uniform, Matrix4f matrix4f) {
		int uniformLocation = glGetUniformLocation(ShaderProgram.ID, uniform);
		glUniformMatrix4fv(uniformLocation, false, matrix4f.get(new float[16]));
	}

	/**
	 * @param uniform  The name of the uniform to be modified.
	 * @param vector4f The {@code Vector4f} to be passed into the shader program so
	 *                 it can be set/modified.
	 */
	
	public static void setVec4(String uniform, Vector4f vector4f) {
		int uniformLocation = glGetUniformLocation(ShaderProgram.ID, uniform);
		glUniform4f(uniformLocation, vector4f.x, vector4f.y, vector4f.z, vector4f.w);
	}

	/**
	 * Updates the projection matrix, this method is usually called after
	 * modification to the matrix.
	 */
	
	public static void updateProjection() {
		int projectionLoc = glGetUniformLocation(ShaderProgram.ID, "projection");
		glUniformMatrix4fv(projectionLoc, false, projection.get(new float[16]));
	}
	
	/**
	 * Updates the model matrix, this method is usually called after
	 * modification to the matrix.
	 */

	public static void updateModel() {
		int modelLoc = glGetUniformLocation(ShaderProgram.ID, "model");
		glUniformMatrix4fv(modelLoc, false, model.get(new float[16]));
	}
	
	/**
	 * Updates the transform matrix, this method is usually called after
	 * modification to the matrix.
	 */

	public static void updateTransform() {
		int transformLoc = glGetUniformLocation(ShaderProgram.ID, "transform");
		glUniformMatrix4fv(transformLoc, false, transform.get(new float[16]));
	}

	private static String load(String filePath) {
		try {
			File shaderFile = new File(filePath);
			FileReader reader = new FileReader(shaderFile);
			BufferedReader bufReader = new BufferedReader(reader);

			StringBuilder sb = new StringBuilder();

			String store;
			while ((store = bufReader.readLine()) != null) {
				sb.append(store + "\r\n");
			}

			String result = sb.toString();
			bufReader.close();

			return result;
		}

		catch (Exception e) {
			System.err.print("Error finding file: " + e);
		}

		return null;
	}
}
