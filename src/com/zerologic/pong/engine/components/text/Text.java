package com.zerologic.pong.engine.components.text;

import com.zerologic.pong.engine.*;
import com.zerologic.pong.engine.Texture;
import com.zerologic.pong.engine.components.*;
import org.joml.Matrix4f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL40.*;

public class Text {

    protected static int VAO, VBO, EBO;
    static Character[] characters;

    Matrix4f model = new Matrix4f();
    Matrix4f transform = new Matrix4f();

    ArrayList<float[]>quads;
    String text;
    Character[] selectedChars;

    float xpos, ypos;
    float size;

    float lineWidth;

    int[] indices = {
            0, 1, 3, 1, 2, 3
    };

    Texture glyph = FontLoader.getCharAtlas();

    // Set properties
    public Text(String input, int xpos, int ypos, float size) {
        text = input;
        this.xpos = (float) xpos;
        this.ypos = (float) ypos;
        this.size = size;
        init();
    }

    public Text(int input, int xpos, int ypos, float size) {
        text = Integer.toString(input);
        this.xpos = (float) xpos;
        this.ypos = (float) ypos;
        this.size = size;
        init();
    }

    public Text(float input, int xpos, int ypos, float size) {
        text = Float.toString(input);
        this.xpos = (float) xpos;
        this.ypos = (float) ypos;
        this.size = size;
        init();
    }

    public Text(double input, int xpos, int ypos, float size) {
        text = Double.toString(input);
        this.xpos = (float) xpos;
        this.ypos = (float) ypos;
        this.size = size;
        init();
    }

    void init() {
        quads = new ArrayList<>(); // Create a new array list for each quad to draw
        characters = FontLoader.loadFont(); // Load glyph data
        selectedChars = new Character[text.length()]; // Create an array of characters based on the length of the input string

        // Loop through each character of the string and assign the correct character object to it
        for (int i=0; i<text.length(); i++) {
            selectedChars[i] = getCharacterByChar(text.charAt(i), characters);
        }

        // Loop through each character and get the line width
        for (int i=0; i<text.length(); i++) {
            lineWidth += selectedChars[i].xadvance();
        }

        // Create VAO, VBO, EBO
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO); // Bind but do not set any buffer data here

        EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Set the attribute pointer so that GL knows how to interpret the data array
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(0);

        // Create the separate arrays for top left, top right, bottom left, bottom right
        float[] tl, tr, br, bl;

        float vCursorPos = 0f; // Virtual cursor position

        // Loop through each of the selected characters, extract their respective properties to create an array for a quad
        for (Character selectedChar : selectedChars) {
            // Top left
            tl = new float[]{
                    vCursorPos,
                    0f,
                    selectedChar.x() / glyph.width,
                    selectedChar.y() / glyph.width,
            };

            // Top right
            tr = new float[]{
                    vCursorPos + selectedChar.width() * size,
                    0,
                    (selectedChar.x() + selectedChar.width()) / glyph.width,
                    selectedChar.y() / glyph.width
            };

            br = new float[]{
                    vCursorPos + selectedChar.width() * size,
                    selectedChar.height() * size,
                    (selectedChar.x() + selectedChar.width()) / glyph.width,
                    (selectedChar.y() + selectedChar.height()) / glyph.width
            };

            bl = new float[]{
                    vCursorPos,
                    selectedChar.height() * size,
                    selectedChar.x() / glyph.width,
                    (selectedChar.y() + selectedChar.height()) / glyph.width
            };

            // Create the 4 different points, then we will combine them to one single array then store them in the array list
            float[] quad = new float[16]; // 16 because 4 attributes by 4 points
            System.arraycopy(tl, 0, quad, 0,  tl.length);
            System.arraycopy(tr, 0, quad, 4,  tr.length);
            System.arraycopy(br, 0, quad, 8,  br.length);
            System.arraycopy(bl, 0, quad, 12, bl.length);

            quads.add(quad); // Add the finalized quad into the list of quads to draw

            vCursorPos += selectedChar.xadvance() * size; // Increment virtual cursor position
        }
    }

    public void write() {
        glyph.use();
        //updatePos();
        glBindVertexArray(VAO);

        // Loop through the ArrayList of quads, bind the VBO and respective buffer data and draw it
        for (float[] quad : quads) {
            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            glBufferData(GL_ARRAY_BUFFER, quad, GL_DYNAMIC_DRAW);

            glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        }
    }

    // Update the text, efficient because even if it is in a loop, it will not update the same text twice
    public void updateText(String newText) {
        if(!newText.equals(text)) {
            text = newText;
            init();
        }
    }

    public void updateText(int newText) {
        if(!Integer.toString(newText).equals(text)) {
            text = Integer.toString(newText);
            init();
        }
    }

    private void updatePos() {
        // Model matrix for initial placement
        model.translate(xpos, ypos, 0f);
        ShaderProgram.addModel(model);
        ShaderProgram.updateModel();
        model.identity();

        // Transform matrix for updated movement
        transform.translate(xpos, ypos, 0.0f);
        ShaderProgram.addTransform(transform);
        ShaderProgram.updateTransform();
        ShaderProgram.resetTransform();
        transform.identity();
    }

    public void setPos(int xpos, int ypos) {
        this.xpos = xpos; this.ypos = ypos;
    }

    public float x() { return this.xpos; }
    public float y() { return this.ypos; }
    public float lineWidth() { return this.lineWidth * size; }

    private static Character getCharacterByChar(char c, Character[] charArray) {
        for (Character character : charArray) {
            if ((char) character.id() == c) {
                return character;
            }
        }
        return new Character(0,0,0,0,0,0,0,0);
    }
}