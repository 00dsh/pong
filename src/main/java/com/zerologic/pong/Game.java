package com.zerologic.pong;

import com.zerologic.pong.engine.ShaderProgram;
import com.zerologic.pong.engine.Time;
import com.zerologic.pong.engine.components.GameObject;
import com.zerologic.pong.engine.components.Renderer;

import com.zerologic.pong.engine.components.gui.uitext.UIFontLoader;
import com.zerologic.pong.engine.components.gui.uitext.UIText;
import org.joml.Vector2f;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.util.Random;

/**
 * @author Dilan Shabani
 * @version 0.3a
 * @since 12/2/2020
 */

public class Game {

	// Window handle + variables
	private static long window;
	private final static float win_width = 1280.0f;
	private final static float win_height = 720.0f;
	private final static String win_title = "PONG";

	private static ShaderProgram program;
	private static ShaderProgram textShader;
	
	// Mouse position vector
	Vector2f m_pos = new Vector2f();

	// Menu objects
	GameObject logo;
	GameObject playButton;
	GameObject quitButton;
	GameObject pausepln;
	GameObject ownership;

	// Game objects
	GameObject paddle1;
	GameObject paddle2;
	GameObject ball;

	// Text objects
	UIText text_pts_p1;
	UIText text_pts_p2;

	UIText speed;

	// Game attributes
	float paddle1Speed = 1000f;
	float paddle2Speed = 1000f;

	int ballDirection = -1;
	int dirToServe = 1;

	float ballSpeed = 500.0f;
	float ballIncSpeed = 50.0f;
	float ballAngle = 45.0f;

	int ballIncAngle;
	int maxRandomAngle = 30;
	
	float origBallSpeed; // Purpose is to reset the speed to the same if someone loses a point.
	
	// Player points
	int pts_p1 = 0;
	int pts_p2 = 0;
	
	Random random = new Random();

	enum GAMESTATE {
		MENU,
		ACTIVE,
		PAUSED,
		PLAYER_WIN
	}
	
	GAMESTATE state = GAMESTATE.MENU;
	
	void init() {
		glfwInit();

		window = glfwCreateWindow((int)win_width, (int)win_height, win_title, NULL, NULL);
		glfwMakeContextCurrent(window);

		glfwWindowHint(GLFW_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_VERSION_REVISION, 2);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

		GL.createCapabilities();

		// Load shader programs (create the main program last to avoid an unnecessary use() call)
		textShader = new ShaderProgram("./res/shaders/textVert.glsl", "./res/shaders/textFrag.glsl");
		program = new ShaderProgram("./res/shaders/vertex.glsl", "./res/shaders/fragment.glsl");

		UIFontLoader.init(textShader, "C:/Windows/Fonts/Arial.ttf"); // Initialize font loader

		// Callback for any key events that don't need to be constantly and instantly
		// updated, this is good for single key-press events.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS && state == GAMESTATE.PAUSED) {
				state = GAMESTATE.ACTIVE;
			} else if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS && state == GAMESTATE.ACTIVE) {
				state = GAMESTATE.PAUSED;
			}
		});
		
		// Mouse cursor position callback
		glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
			m_pos.x = (float) xpos;
			m_pos.y = (float) ypos;
		});
		
		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			if (state == GAMESTATE.MENU) {
				if (checkLMB(playButton)) {
					state = GAMESTATE.ACTIVE;
				}

				if (checkLMB(quitButton)) {
					glfwSetWindowShouldClose(window, true);
				}
			}
			
			if (state == GAMESTATE.PAUSED) {
				if(checkLMB(quitButton)) {
					glfwSetWindowShouldClose(window, true);
				}
			}
		});
		
		glfwSetWindowSizeCallback(window, (window, width, height) -> glViewport(0, 0, width, height));
		
		// GameObjects for the menu
		logo = new GameObject(600.0f, 200.0f);
		logo.setTexture("./res/textures/menu/logo.jpg", true, GL_RGB);
		logo.setPos(win_width / 2 - logo.size.x / 2, 40);
		
		playButton = new GameObject(169.0f, 51.0f);
		playButton.setTexture("./res/textures/menu/playbtn.png", true, GL_RGBA);
		playButton.setPos(win_width / 2 - playButton.size.x / 2, 350);
		
		quitButton = new GameObject(169.0f, 61.0f);
		quitButton.setTexture("./res/textures/menu/quitbtn.png", true, GL_RGBA);
		quitButton.setPos(playButton.pos.x, playButton.pos.y + quitButton.size.y + 20);
		
		pausepln = new GameObject(263, 53);
		pausepln.setTexture("./res/textures/menu/pause.png", true, GL_RGBA);
		pausepln.setPos(win_width / 2 - pausepln.size.x / 2, win_height / 2 - pausepln.size.y / 2);
		
		ownership = new GameObject(300, 20);
		ownership.setTexture("./res/textures/menu/ownership.png", true, GL_RGBA);
		ownership.setPos(5, win_height - ownership.size.y - 2);
		
		// GameObjects for actual game
		paddle1 = new GameObject(25.0f, 200.0f);
		paddle1.setPos(50.0f, win_height/2.0f - paddle1.size.y/2.0f);
		
		paddle2 = new GameObject(25.0f, 200.0f);
		paddle2.setPos(1280.0f - paddle2.size.x - 50.0f, (720f / 2f) - paddle2.size.y/2.0f);
		
		ball = new GameObject(20.0f, 20.0f);
		ball.setPos(win_width/2 - ball.size.x / 2, win_height / 2 - ball.size.y / 2);

		text_pts_p1 = new UIText("pts_p1", 90f, 0f, 0f);
		text_pts_p1.setColor(1f, 1f, 1f, 1f);

		text_pts_p2 = new UIText(pts_p2, 90f, text_pts_p1.width(), 0f);
		text_pts_p2.setColor(1f, 1f, 1f, 1f);

		speed = new UIText(ballSpeed, 100f,  1280f/2f, 200);
		speed.setColor(1f, 1f, 1f, 1f);

		origBallSpeed = ballSpeed;

		glfwShowWindow(window);
	}
	
	// Game loop
	void loop() {

		glClearColor(0.2f, 0.2f, 0.4f, 1.0f);

		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT);
			
			// Check for win condition before the game draws a new frame to avoid weird effects
			if(pts_p1 == 5 || pts_p2 == 5)
				state = GAMESTATE.PLAYER_WIN;

			switch (state) {
				case ACTIVE -> drawGame();
				case MENU -> drawMenu();
				case PAUSED -> drawPause();
				case PLAYER_WIN -> drawWin();
			}

			Time.calcTime();
			processInput();
			glfwPollEvents();
			glfwSwapBuffers(window);
		}
	}

	void processInput() {
		// Player 1
		if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
			if (paddle1.pos.y > 0) {
				paddle1.pos.y -= paddle1Speed * Time.deltaTimef();
			} else if (paddle1.pos.y < 0) {
				paddle1.pos.y = 0;
			}
		}

		if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
			if (paddle1.pos.y < win_height - paddle1.size.y) {
				paddle1.pos.y += paddle1Speed * Time.deltaTimef();
			} else if (paddle1.pos.y > win_height - paddle1.size.y) {
				paddle1.pos.y = win_height - paddle1.size.y;
			}
		}

		// Player 2
		if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
			if (paddle2.pos.y > 0) {
				paddle2.pos.y -= paddle2Speed * Time.deltaTimef();
			} else if (paddle2.pos.y < 0) {
				paddle2.pos.y = 0;
			}
		}

		if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
			if (paddle2.pos.y < win_height - paddle2.size.y) {
				paddle2.pos.y += paddle2Speed * Time.deltaTimef();
			} else if (paddle2.pos.y > win_height - paddle2.size.y) {
				paddle2.pos.y = win_height - paddle2.size.y;
			}
		}
		
		if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && state == GAMESTATE.ACTIVE && ballDirection == -1) {
			ballDirection = dirToServe;
		}
	}
	
	void drawMenu() {
		Renderer.draw(new UIText((int)glfwGetTime(), 100f, 0f, 0f));
		Renderer.draw(logo, 1.0f);
		Renderer.draw(playButton, 1.0f);
		Renderer.draw(quitButton, 1.0f);
		Renderer.draw(ownership, 1.0f);
	}
	
	void drawGame() {
		checkBall();
		Renderer.draw(paddle1, 1);
		Renderer.draw(paddle2, 1);
		Renderer.draw(ball, 1);

		Renderer.draw(text_pts_p1);
		Renderer.draw(text_pts_p2);
		Renderer.draw(speed);

		//text_pts_p1.setText(pts_p1);
		text_pts_p2.setText(pts_p2);
		speed.setText((int)ballSpeed);
		speed.setPos(1280f/2f - speed.width()/2f, 0f);
		speed.setColor(1.0f, 0f, 0f, 1f);
	}
	
	void drawPause() {
		Renderer.draw(pausepln, 1.0f);
		Renderer.draw(quitButton, 1.0f);
	}
	
	void drawWin()	{
		Renderer.draw(quitButton, 1.0f);
	}
	
	boolean checkLMB(GameObject object) {
		if (glfwGetMouseButton(window, 0) == GLFW_PRESS) {
			if(m_pos.x >= object.pos.x && m_pos.x <= object.pos.x + object.size.x
				&& m_pos.y >= object.pos.y && m_pos.y <= object.pos.y + object.size.y) {
					return true;
			}
		}
		return false;
	}
	
	void checkBall() {
		
		// Check direction of ball via an integer value
		if (ballDirection == 0) {
			ball.pos.x -= ballSpeed * Time.deltaTimef();
			ball.pos.y += Math.cos(ballAngle) * 500 * Time.deltaTimef();
		} else if (ballDirection == 1) {
			ball.pos.x += ballSpeed * Time.deltaTimef();
			ball.pos.y += Math.cos(ballAngle) * 500 * Time.deltaTimef();
		}
		
		// Ball dynamics
		if (ball.pos.y <= 0) {
			ballAngle += 180;
		}
		
		if (ball.pos.y >= win_height - ball.size.y) {
			ballAngle += 180;
		}
	
		if(checkCollision(ball, paddle1)) {
			ballIncAngle = random.nextInt(maxRandomAngle);
			ballAngle += ballIncAngle;
			
			ballDirection = 1;
			ballSpeed += ballIncSpeed;
		}
		
		if(checkCollision(ball, paddle2)) {
			ballIncAngle = random.nextInt(maxRandomAngle);
			ballAngle += ballIncAngle;
			
			ballDirection = 0;
			ballSpeed += ballIncSpeed;
		}
		
		// Point score condition
		// Player one wins point
		if (ball.pos.x > win_width + 100) {
			ballDirection = -1;
			ballSpeed = origBallSpeed;
			dirToServe = 1;
			pts_p1 += 1;
			ball.setPos(win_width / 2 - ball.size.x / 2, win_height / 2 - ball.size.y / 2);
		}
		
		// Player two wins point
		if (ball.pos.x < -100) {
			ballDirection = -1;
			ballSpeed = origBallSpeed;
			dirToServe = 0;
			pts_p2 += 1;
			ball.setPos(win_width / 2 - ball.size.x / 2, win_height / 2 - ball.size.y / 2);
		}
	}
	
	boolean checkCollision(GameObject first, GameObject second) {
		
		if(first.pos.x + first.size.x >= second.pos.x && first.pos.x <= second.pos.x + second.size.x
			&& first.pos.y + first.size.y >= second.pos.y && first.pos.y <= second.pos.y + second.size.y) {    	    // Checking for x-axis collision
			return true;
		}
		return false;
	}
	
	float degToRad(float f) {
		return f * ((float) Math.PI / 180);
	}
	
	void cleanUp() {
		glfwDestroyWindow(window);
		glfwTerminate();
		GL.destroy();
	}

	void run() {
		init();
		loop();
		cleanUp();
	}

	public static void main(String[] args) {
		new Game().run();
	}

	// Game accessors
	public static ShaderProgram getShaderProgram() {
		return program;
	}

	public static long getWindow() {
		return window;
	}

	public static float getWinWidth() {
		return win_width;
	}

	public static float getWinHeight() {
		return win_height;
	}

}
