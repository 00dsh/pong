package com.zerologic.pong.engine.components.gui.input;

import static org.lwjgl.opengl.GL33.*;
import com.zerologic.pong.engine.components.gui.uitext.UIText;
import org.joml.Vector4f;

public class Button {
    //TODO: create a button class that draws a filled rectangle with text in the centre

    private int VAO, VBO, EBO;

    private UIText text;
    private Vector4f dim; // xpos, ypos, width, height (xyzw)

    public Button() { this("No text", 50f, 0f, 0f); }

    public Button(String text, float fontSize, float xpos, float ypos) {
        this.text = new UIText(text, fontSize, xpos, ypos);
        this.dim = new Vector4f(xpos, ypos, this.text.width(), fontSize);

        init();
    }

    private void init() {
        float[] data = {
                // Vertex positions	   // Tex coords
                0.0f,  0.0f,   0.0f, 1.0f, // Top left
                dim.z, 0.0f,   1.0f, 1.0f, // Top right
                dim.z, dim.w,  1.0f, 0.0f, // Bottom right
                0.0f,  dim.w,  0.0f, 0.0f  // Bottom left
        };

        int[] indices = {
                0, 1, 3,
                1, 2, 3
        };

        this.VAO = glGenVertexArrays();
        glBindVertexArray(this.VAO);

        this.VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

        this.EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 4, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(0);

        // Cleanup
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void draw() {
        glBindVertexArray(this.VAO);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        text.draw();
    }

    public float x() {
        return this.dim.x;
    }

    public float y() {
        return this.dim.y;
    }

    public void setPos(float x, float y) {
        this.dim.x = x;
        this.dim.y = y;
    }

}
