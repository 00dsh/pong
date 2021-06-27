package com.zerologic.pong.engine.components.text;

public class Character {

    private float id, x, y, width, height, xoffset, yoffset, xadvance;

    public Character(int id, int x, int y, int width, int height, int xoffset, int yoffset, int xadvance) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xoffset = xoffset;
        this.yoffset = yoffset;
        this.xadvance = xadvance;
    }

    // Properties
    public float id() {
        return this.id;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float width() {
        return this.width;
    }
    public void width(float newWidth) { this.width = newWidth; }

    public float height() {
        return this.height;
    }

    public float xoffset() {
        return this.xoffset;
    }

    public float yoffset() {
        return this.yoffset;
    }

    public float xadvance() {
        return this.xadvance;
    }

    void printAttr() {
        System.out.println("ID: " + id + ", x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + " ASCII \"" + (char)id + "\"");
    }

}
