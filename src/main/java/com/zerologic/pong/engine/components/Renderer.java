package com.zerologic.pong.engine.components;

import com.zerologic.pong.engine.Game;
import com.zerologic.pong.engine.components.gui.uitext.*;
import org.joml.*;

public class Renderer {

	static Matrix4f model = new Matrix4f();

	public static void draw(GameObject gameObject, float sizeMul) {
		model.translation(new Vector3f(gameObject.pos, 0.0f));
		//model.scale(sizeMul);
		Game.getShaderProgram().setModel(model);

		Game.getShaderProgram().updateModel();
		//Game.getShaderProgram().setModelIdentity();

		gameObject.draw();
	}

	public static void draw(UIText text) {
		// Activate text shader program
		UIFontLoader.getShaderProgram().use();

		model.identity();
		model.translate(text.x(), text.y(), 0.0f);
		UIFontLoader.getShaderProgram().setModel(model);

		UIFontLoader.getShaderProgram().updateModel();
		//UIFontLoader.getShaderProgram().setModelIdentity();

		text.draw();
		Game.getShaderProgram().use(); // Reactivate game shader program for correct drawing of everything else
	}

	public static void draw(GameObject gameObject, float width, float height) {
		model.identity();
		model.translate(new Vector3f(gameObject.pos, 0.0f));
		model.scale(width, height, 0.0f);
		Game.getShaderProgram().setModel(model);

		Game.getShaderProgram().updateModel();
		//Game.getShaderProgram().setModelIdentity();

		gameObject.draw();
	}

	/* DEPRECATED.... uses small brain method to render text, keep as reminder of how hard you tried just to fail :)
	public static void draw(Text text) {
		model.identity();
		model.translate(new Vector3f(text.x(), text.y(), 0.0f));
		model.get(Game.getShaderProgram().model());

		Game.getShaderProgram().updateModel();
		Game.getShaderProgram().setModelIdentity();

		text.write();
	}
	*/
}
