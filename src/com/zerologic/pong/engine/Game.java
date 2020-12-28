package com.zerologic.pong.engine;

import com.zerologic.pong.engine.components.GameObject;
import com.zerologic.pong.engine.components.Renderer;
import org.joml.Vector2f;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.util.Random;


/**
 * @author Dilan Shabani
 * @version 1.0
 * @since 12/2/2020
 */

public class Game {

	// Window handle + variables
	public static long window;
	static float win_width = 1280.0f;
	static float win_height = 720.0f;
	String win_title = "PONG";
	
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

	float paddle1Speed = 1000;
	float paddle2Speed = 1000;

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
		GAME_ACTIVE, 
		PAUSED,
		GAME_WIN
	}
	
	GAMESTATE state = GAMESTATE.MENU;
	
	void init() {
		glfwInit();

		window = glfwCreateWindow((int) win_width, (int) win_height, win_title, NULL, NULL);
		glfwMakeContextCurrent(window);

		glfwWindowHint(GLFW_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_VERSION_REVISION, 2);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

		GL.createCapabilities();

		ShaderProgram.init();
		
		
		// Callback for any key events that don't need to be constantly and instantly
		// updated, this is good for single key-press events.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS && state == GAMESTATE.PAUSED) {
				state = GAMESTATE.GAME_ACTIVE;
			} else if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS && state == GAMESTATE.GAME_ACTIVE) {
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
					state = GAMESTATE.GAME_ACTIVE;
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
		
		glfwSetWindowSizeCallback(window, (window, width, height) -> {
			glViewport(0, 0, width, height);
		});
		
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
		
		origBallSpeed = ballSpeed;
	}
	
	// Game loop
	void loop() {

		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT);
			
			// Check for win condition before the game draws a new frame to avoid weird effects
			if(pts_p1 == 5 || pts_p2 == 5)
				state = GAMESTATE.GAME_WIN;

			if (state == GAMESTATE.GAME_ACTIVE)
				drawGame();
			
			else if (state == GAMESTATE.MENU)
				drawMenu();
			
			else if (state == GAMESTATE.PAUSED)
				drawPause();
			
			else if(state == GAMESTATE.GAME_WIN)
				drawWin();
			
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
				paddle1.pos.y -= paddle1Speed * Time.deltaTime;
			} else if (paddle1.pos.y < 0) {
				paddle1.pos.y = 0;
			}
		}

		if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
			if (paddle1.pos.y < win_height - paddle1.size.y) {
				paddle1.pos.y += paddle1Speed * Time.deltaTime;
			} else if (paddle1.pos.y > win_height - paddle1.size.y) {
				paddle1.pos.y = win_height - paddle1.size.y;
			}
		}

		// Player 2
		if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
			if (paddle2.pos.y > 0) {
				paddle2.pos.y -= paddle2Speed * Time.deltaTime;
			} else if (paddle2.pos.y < 0) {
				paddle2.pos.y = 0;
			}
		}

		if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
			if (paddle2.pos.y < win_height - paddle2.size.y) {
				paddle2.pos.y += paddle2Speed * Time.deltaTime;
			} else if (paddle2.pos.y > win_height - paddle2.size.y) {
				paddle2.pos.y = win_height - paddle2.size.y;
			}
		}
		
		if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && state == GAMESTATE.GAME_ACTIVE && ballDirection == -1) {
			ballDirection = dirToServe;
		}
	}
	
	void drawMenu() {
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
	}
	
	void drawPause() {
		Renderer.draw(pausepln, 1.0f);
		Renderer.draw(quitButton, 1.0f);
	}
	
	void drawWin()
	{
		Renderer.draw(quitButton, 1.0f);
	}
	
	boolean checkLMB(GameObject object) {
		if (glfwGetMouseButton(window, 0) == GLFW_PRESS) {
			if(m_pos.x >= object.pos.x && m_pos.x <= object.pos.x + object.size.x) {
				if(m_pos.y >= object.pos.y && m_pos.y <= object.pos.y + object.size.y) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	void checkBall() {
		
		// Check direction of ball via an integer value
		if (ballDirection == -1) { 
			ball.pos.x = ball.pos.x;
		} else if (ballDirection == 0) {
			ball.pos.x -= ballSpeed * Time.deltaTime;
			ball.pos.y += Math.cos(ballAngle) * 500 * Time.deltaTime;
		} else if (ballDirection == 1) {
			ball.pos.x += ballSpeed * Time.deltaTime;
			ball.pos.y += Math.cos(ballAngle) * 500 * Time.deltaTime;
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
		
		if(first.pos.x + first.size.x >= second.pos.x && first.pos.x <= second.pos.x + second.size.x) {    	    // Checking for x-axis collision
			if(first.pos.y + first.size.y >= second.pos.y && first.pos.y <= second.pos.y + second.size.y) { 	// Checking for y-axis collision
				return true;
			}
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

}
