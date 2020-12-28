package com.zerologic.pong.engine.components;

import com.zerologic.pong.engine.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Renderer {

	static Matrix4f model = new Matrix4f();

	public static void draw(GameObject gameObject, float sizeMul) {
		model.identity();
		model.translate(new Vector3f(gameObject.pos, 0.0f));
		model.scale(sizeMul);
		model.get(ShaderProgram.model);

		ShaderProgram.updateModel();
		ShaderProgram.model.identity();

		gameObject.draw();
	}

	public static void draw(GameObject gameObject, float width, float height) {
		model.identity();
		model.translate(new Vector3f(gameObject.pos, 0.0f));
		model.scale(width, height, 0.0f);
		model.get(ShaderProgram.model);

		ShaderProgram.updateModel();
		ShaderProgram.model.identity();

		gameObject.draw();
	}
}
