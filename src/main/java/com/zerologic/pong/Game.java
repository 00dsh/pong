package com.zerologic.pong;

import com.zerologic.pong.engine.ShaderProgram;
import com.zerologic.pong.engine.Time;
import com.zerologic.pong.engine.components.GameObject;
import com.zerologic.pong.engine.components.Renderer;

import com.zerologic.pong.engine.components.gui.input.Button;
import com.zerologic.pong.engine.components.gui.uitext.UIFontLoader;
import com.zerologic.pong.engine.components.gui.uitext.UIText;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.CallbackI;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;
import java.util.Random;

/**
 * @author Dilan Shabani
 * @version 0.4a
 * @since 12/2/2020
 */

public class Game {

	// Window handle + variables
	private static long monitor;
	private static long window;
	private static GLFWVidMode mode;

	private static float win_width = 1280f;
	private static float win_height = 720f;
	private final static String win_title = "PONG";

	private int monitorWidth;
	private int monitorHeight;

	private static ShaderProgram program;
	private static ShaderProgram textShader;

	private int debugCounter = 0;
	private boolean debug = false;
	private boolean debugFPS = false;
	
	// Mouse position vector
	private final Vector2f m_pos = new Vector2f();

	// Menu objects
	private Button someButton;
	private GameObject logo;
	private GameObject playButton;
	private GameObject quitButton;

	// Game objects
	private GameObject paddle1;
	private GameObject paddle2;
	private GameObject ball;

	// Text objects
	private UIText text_pts_p1;
	private UIText text_pts_p2;
	private UIText pauseText;
	private UIText ownership;

	// Debug objects
	private UIText debugText;

	// Game attributes
	private float paddle1Speed = 1000f;
	private float paddle2Speed = 1000f;

	private int ballDirection = -1;
	private int dirToServe = 1;

	private float ballSpeed = 500.0f;
	private float ballIncSpeed = 50.0f;
	private float ballAngle = 45.0f;

	private int ballIncAngle;
	private final int maxRandomAngle = 30;

	private float origBallSpeed = ballSpeed; // Purpose is to reset the speed to the same if someone loses a point.
	
	// Player points
	private int pts_p1 = 0;
	private int pts_p2 = 0;
	
	Random random = new Random();
	private GLFWVidMode mode1;

	enum GAMESTATE {
		MENU,
		ACTIVE,
		PAUSED,
		PLAYER_WIN
	}
	
	GAMESTATE state = GAMESTATE.MENU;
	
	void init() {
		glfwInit();

		monitor = glfwGetPrimaryMonitor();
		mode = glfwGetVideoMode(monitor);
		window = glfwCreateWindow((int)win_width, (int)win_height, win_title, NULL, NULL);
		glfwMakeContextCurrent(window);

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_VERSION_REVISION, 2);
		glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

		// Get monitor attribs
		monitorWidth = mode.width();
		monitorHeight = mode.height();

		// Set position before window is visible
		glfwSetWindowPos(window, (int)(monitorWidth/2 - win_width/2), (int)(monitorHeight/2 - win_height/2));

		GL.createCapabilities(); // Call after window has been placed correctly

		// Load shader programs (create the main program last to avoid an unnecessary use() call)
		textShader = new ShaderProgram("./src/main/resources/shaders/textVert.glsl", "./src/main/resources/shaders/textFrag.glsl");
		program = new ShaderProgram("./src/main/resources/shaders/vertex.glsl", "./src/main/resources/shaders/fragment.glsl");

		UIFontLoader.init(textShader, "C:/Windows/Fonts/Arial.ttf"); // Initialize font loader
		//UIFontLoader.init(textShader, "C:/Windows/Fonts/Consola.ttf");

		// Callback for any key events that don't need to be constantly and instantly
		// updated, this is good for single key-press events.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ((key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) && state == GAMESTATE.PAUSED) {
				state = GAMESTATE.ACTIVE;
			} else if ((key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) && state == GAMESTATE.ACTIVE) {
				state = GAMESTATE.PAUSED;
			}

			if (key == GLFW_KEY_F10 && action == GLFW_PRESS) {
				glfwSetWindowMonitor(window, NULL, 0, 0, 1280, 720, 165);
				glfwSetWindowPos(window, (int)(monitorWidth/2 - win_width/2), (int)(monitorHeight/2 - win_height/2));
			}

			if (key == GLFW_KEY_F11 && action == GLFW_PRESS) {
				glfwSetWindowMonitor(window, monitor, 0, 0, 1280, 720, 165);
			}

			if (debug) {
				if (key == GLFW_KEY_Y && action == GLFW_PRESS) {
					win_width = 1920f; win_height = 1080f;
					glViewport(0, 0, (int)win_width, (int)win_height);
					program.initMatrices();
					textShader.initMatrices();
					glfwSetWindowSize(window, (int)win_width, (int)win_height);
				}

				if (key == GLFW_KEY_U && action == GLFW_PRESS) {
					UIFontLoader.printLoadedFonts();
				}

				if (key == GLFW_KEY_P && action == GLFW_PRESS) {
					debugFPS = !debugFPS;
				}
			}

			// Used to enable debug mode
			if (key == GLFW_KEY_GRAVE_ACCENT && action == GLFW_PRESS) {
				if(!debug)
					debugCounter++;
				else
					debugCounter--;

				if (debugCounter == 3) {
					debug = true;
				} else if (debugCounter == 0) {
					debug = false;
				}
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

		// Framebuffer callback to correctly update the viewport
		glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
			win_width  = width;
			win_height = height;

			// Auto resize of objects from callback
			// Auto set menu
			logo.setPos(win_width / 2 - logo.size.x / 2, 100);
			playButton.setPos(win_width / 2f - playButton.size.x / 2f, win_height / 2f - playButton.size.y / 2f);
			quitButton.setPos(win_width / 2 - quitButton.size.x / 2, playButton.pos.y + quitButton.size.y + 20);
			ownership.setPos(5, win_height - ownership.height());
			pauseText.setPos(win_width / 2 - pauseText.width() / 2, (win_height / 2) - 100f - pauseText.height() / 2);

			// Auto set game objects
			paddle1.setPos(50.0f, win_height / 2f - paddle1.size.y / 2f);
			paddle2.setPos(win_width - paddle2.size.x - 50.0f, (win_height / 2f) - paddle2.size.y / 2f);
			ball.setPos(win_width / 2f - ball.size.x / 2f, win_height / 2f - ball.size.y / 2f);
			text_pts_p2.setPos(win_width - text_pts_p2.width(), 0);

			// Scaling paddle and ball speeds
			paddle1Speed = 1000 * (win_height / 720f);
			paddle2Speed = paddle1Speed;

			ballSpeed = 500 * (win_width / 1280f);
			origBallSpeed = 500 * (win_width / 1280f);
			ballIncSpeed = 50 * (win_width / 1280f);

			program.initMatrices();
			textShader.initMatrices();
			glViewport(0, 0, width, height);
		});

		glfwSetWindowAspectRatio(window, 16, 9);
		glfwSetWindowSizeLimits(window, 915, 515, GLFW_DONT_CARE, GLFW_DONT_CARE);
		
		// GameObjects for the menu
		logo = new GameObject(258.0f, 116.0f);
		logo.setTexture("src/main/resources/textures/menu/logo.png", true, GL_RGBA);
		logo.setPos(win_width / 2 - logo.size.x / 2, 100);

		someButton = new Button("Hello world", 51f, 0f, 0f);
		someButton.setPos(10f, 10f);
		
		playButton = new GameObject(169.0f, 51.0f);
		playButton.setTexture("src/main/resources/textures/menu/playbtn.png", true, GL_RGBA);
		playButton.setPos(win_width / 2f - playButton.size.x / 2f, win_height / 2f - playButton.size.y / 2f);
		
		quitButton = new GameObject(169.0f, 61.0f);
		quitButton.setTexture("src/main/resources/textures/menu/quitbtn.png", true, GL_RGBA);
		quitButton.setPos(playButton.pos.x, playButton.pos.y + quitButton.size.y + 20);

		pauseText = new UIText("Game Paused", 70f);
		pauseText.setColor(1f, 1f, 1f, 1f);
		pauseText.setPos(win_width / 2 - pauseText.width() / 2, (win_height / 2) - 100f - pauseText.height() / 2);
		
		ownership = new UIText("ZeroLogic Games", 30f);
		ownership.setColor(1f, 1f, 1f, 1f);
		ownership.setPos(5, win_height - ownership.height());
		
		// GameObjects for actual game
		paddle1 = new GameObject(25.0f, 200.0f);
		paddle1.setPos(50.0f, win_height / 2.0f - paddle1.size.y / 2.0f);
		
		paddle2 = new GameObject(25.0f, 200.0f);
		paddle2.setPos(win_width - paddle2.size.x - 50.0f, (win_height / 2f) - paddle2.size.y / 2f);
		
		ball = new GameObject(20.0f, 20.0f);
		ball.setPos(win_width / 2f - ball.size.x / 2f, win_height / 2f - ball.size.y / 2f);

		text_pts_p1 = new UIText(Integer.toString(pts_p1), 90f);
		text_pts_p1.setColor(1f, 1f, 1f, 1f);

		text_pts_p2 = new UIText(Integer.toString(pts_p2), 90f);
		text_pts_p2.setColor(1f, 1f, 1f, 1f);
		text_pts_p2.setPos(win_width - text_pts_p2.width(), 0);

		// Debug components
		debugText = new UIText(
				"mouse pos (x, y): " + m_pos.x + ", " + m_pos.y + "\n" +
				"delta time: " + Time.deltaTimef() + "\n" +
				"ball speed: " + ballSpeed + "\n" +
				"ball pos (x, y): " + ball.pos.x + ", " + ball.pos.y + "\n" +
				"paddle1 pos(y): " + paddle1.pos.y + "\n" +
				"paddle2 pos(y): " + paddle2.pos.y
				, 20f);
		debugText.setPos(0f, win_height - debugText.height());

		glfwShowWindow(window);
	}
	
	// Game loop
	void loop() {
		glClearColor(0.2f, 0.2f, 0.4f, 1.0f);

		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT);

			// Check for win condition before the game draws a new frame to avoid weird effects
			if(pts_p1 == 5 || pts_p2 == 5) {
				state = GAMESTATE.PLAYER_WIN;
			}

			switch (state) {
				case MENU -> drawMenu();
				case ACTIVE -> drawGame();
				case PAUSED -> drawPause();
				case PLAYER_WIN -> drawWin();
			}

			// Buggy, causes game to slow down but obviously debugging isn't meant to be enabled for regular gameplay
			// Use at your own peril
			if(debug) {
				Renderer.draw(debugText);
				Renderer.draw(someButton);

				if(debugFPS) {
					debugText.setText(
							"mouse pos (x, y): " + (int)m_pos.x + ", " + (int)m_pos.y + "\n" +
									"delta time: " + (int)Time.deltaTimef() + "\n" +
									"ball speed: " + (int)ballSpeed + "\n" +
									"ball pos (x, y) press P to disable: " + (int)ball.pos.x + ", " + (int)ball.pos.y + "\n" +
									"paddle1 pos(y): " + (int)paddle1.pos.y + "\n" +
									"paddle2 pos(y): " + (int)paddle2.pos.y);
				} else {
					debugText.setText(
							"mouse pos (x, y): " + (int)m_pos.x + ", " + (int)m_pos.y + "\n" +
									"delta time: " + (int)Time.deltaTimef() + "\n" +
									"ball speed: " + (int)ballSpeed + "\n" +
									"ball pos: (disabled, press P to enable) \n" +
									"paddle1 pos(y): " + (int)paddle1.pos.y + "\n" +
									"paddle2 pos(y): " + (int)paddle2.pos.y);
				}

			}

			processInput();
			glfwPollEvents();
			glfwSwapBuffers(window);
			Time.calcTime();
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
		Renderer.draw(logo);
		Renderer.draw(playButton);
		Renderer.draw(quitButton);
		Renderer.draw(ownership);
	}
	
	void drawGame() {
		checkBall();
		Renderer.draw(paddle1);
		Renderer.draw(paddle2);
		Renderer.draw(ball);

		Renderer.draw(text_pts_p1);
		Renderer.draw(text_pts_p2);

		text_pts_p1.setText(pts_p1);
		text_pts_p2.setText(pts_p2);
	}
	
	void drawPause() {
		Renderer.draw(pauseText);
		Renderer.draw(quitButton);
	}
	
	void drawWin()	{
		UIText playerWon;

		if (pts_p1 == 5) {
			playerWon = new UIText("Player 1 has won the game!", 100f);
			playerWon.setPos(1280f/2f - playerWon.width()/2f, 720f/2f - playerWon.height()/2f - 100);
			playerWon.setColor(1f, 1f, 1f, 1f);
			Renderer.draw(playerWon);
		} else if (pts_p2 == 5) {
			playerWon = new UIText("Player 2 has won the game!", 100f);
			playerWon.setPos(1280f/2f - playerWon.width()/2f, 720f/2f - playerWon.height()/2f);
			playerWon.setColor(1f, 1f, 1f, 1f);
			Renderer.draw(playerWon);
		}
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
	
	private boolean checkCollision(GameObject first, GameObject second) {
		if(first.pos.x + first.size.x >= second.pos.x && first.pos.x <= second.pos.x + second.size.x
			&& first.pos.y + first.size.y >= second.pos.y && first.pos.y <= second.pos.y + second.size.y) {    	    // Checking for x-axis collision
			return true;
		}
		return false;
	}

	void cleanUp() {
		UIFontLoader.destroy();
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

	public static float getWinWidth() {
		return win_width;
	}

	public static float getWinHeight() {
		return win_height;
	}

}
