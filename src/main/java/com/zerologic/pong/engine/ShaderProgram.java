package com.zerologic.pong.engine;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.lwjgl.opengl.GL40.*;

public class ShaderProgram {

	int ID; // Program handle

	int vertexShader; // Vertex shader handle
	String vertexShaderSource;

	int fragmentShader; // Fragment shader handle
	String fragmentShaderSource;

	// Matrices for vertices drawn
	public Matrix4f projection = new Matrix4f();
	public Matrix4f model      = new Matrix4f();
	public Matrix4f transform  = new Matrix4f();

	public ShaderProgram(String vertPath, String fragPath) {
		vertexShaderSource = ShaderProgram.readStringFromFile(vertPath);
		fragmentShaderSource = ShaderProgram.readStringFromFile(fragPath);

		init();
	}

	private void init() {
		ID = glCreateProgram(); // Create the shader program
		vertexShader = glCreateShader(GL_VERTEX_SHADER); // Create the vertex shader
		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER); // Create the fragment shader

		glShaderSource(vertexShader, vertexShaderSource); // Add shader source to shader
		glCompileShader(vertexShader); // Compile shader
		checkShader(vertexShader);

		glShaderSource(fragmentShader, fragmentShaderSource); // Add shader source to shader
		glCompileShader(fragmentShader); // Compile shader
		checkShader(fragmentShader);

		// Attach shaders and link program, then activate it so that we can initialize the matrices
		glAttachShader(ID, vertexShader);
		glAttachShader(ID, fragmentShader);
		glLinkProgram(ID);

		glUseProgram(ID);
		initMatrices();
		glUseProgram(0);

		// Delete the shaders as we no longer need them
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
	}

	public void initMatrices() {
		// Set up matrices
		projection.ortho(0.0f, Game.getWinWidth(), Game.getWinHeight(), 0.0f, -1.0f, 1.0f);

		// Apply to shader program
		setMatrix4f(projection, "projection");
		setMatrix4f(model, "model");
		setMatrix4f(transform, "transform");
	}

	public void setModel(Matrix4f m) {
		m.get(this.model);
	}

	public void setTransform(Matrix4f t) {
		t.get(this.model);
	}

	public void updateModel() {
		setMatrix4f(this.model, "model");
	}

	public void updateTransform() {
		setMatrix4f(this.transform, "transform");
	}

	public void resetModel() {
		this.model.identity();
		setMatrix4f(this.model, "model");
	}

	public void resetTransform() {
		this.transform.identity();
		setMatrix4f(this.transform, "transform");
	}

	public void use() {
		glUseProgram(this.ID);
	}

	public static void use(int id) {
		glUseProgram(id);
	}

	public void setMatrix4f(Matrix4f matrix, String query) {
		int loc = glGetUniformLocation(this.ID, query);
		glUniformMatrix4fv(loc, false, matrix.get(new float[16]));
	}

	public void setVector4f(Vector4f vec, String query) {
		int loc = glGetUniformLocation(this.ID, query);
		glUniform4f(loc, vec.x(), vec.y(), vec.z(), vec.w());
	}

	private void checkShader(int shader) {
		int status = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (status != 1) {
			System.err.println("Status: " + status + " Info log: " + glGetShaderInfoLog(shader));
		}
	}

	private static String readStringFromFile(String filePath) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			StringBuilder sb = new StringBuilder();

			String store = "";

			while((store = reader.readLine()) != null) {
				sb.append(store + "\r\n");
			}
			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "An error occurred.";
	}
}
