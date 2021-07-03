package com.zerologic.pong.engine.components;

import com.zerologic.pong.Game;
import com.zerologic.pong.engine.components.gui.uitext.*;
import org.joml.*;

public class Renderer {

	static Matrix4f model = new Matrix4f();

	public static void draw(GameObject gameObject) {
		Game.getShaderProgram().use();
		model.translation(new Vector3f(gameObject.pos, 0.0f));

		Game.getShaderProgram().setModel(model);
		Game.getShaderProgram().updateModel();

		gameObject.draw();
	}

	public static void draw(UIText text) {
		// Activate text shader program
		UIFontLoader.getShaderProgram().use();

		model.translation(text.x(), text.y(), 0.0f);
		UIFontLoader.getShaderProgram().setModel(model);
		UIFontLoader.getShaderProgram().updateModel();

		text.draw();
		Game.getShaderProgram().use();
	}
}
