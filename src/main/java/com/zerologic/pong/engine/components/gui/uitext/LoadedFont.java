package com.zerologic.pong.engine.components.gui.uitext;

import org.joml.Vector4f;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;

public class LoadedFont {

    // The LoadedFont class is simply instantiated with a created bitmap, so that any bitmaps
    // that are needed are created and stored in the FontLoader class.
    // TODO: Get the length for each text object in pixels

    private final String path;
    private final float fontSize;

    private STBTTBakedChar.Buffer cdata;

    private int ascent;
    private int descent;
    private int lineGap;
    private float scale;
    private int bmpSize;

    private int texture;

    /**
     * Creates a new {@code LoadedFont} with {@code UIFontLoader} for use
     * with {@code UIText} objects.
     * @param path The filepath to the font.
     * @param fontSize The font's size.
     */
    public LoadedFont(String path, float fontSize) {
        this.path = path;
        this.fontSize = fontSize;
    }

    public void setData(STBTTBakedChar.Buffer cdata, int ascent, int descent, int lineGap, float scale, int bmpSize, int texture) {
        this.cdata = cdata;
        this.ascent = ascent;
        this.descent = descent;
        this.lineGap = lineGap;
        this.scale = scale;
        this.bmpSize = bmpSize;
        this.texture = texture;
    }

    public STBTTBakedChar.Buffer getCharData() {
        return this.cdata;
    }

    public float getFontSize() { return this.fontSize; }

    public int getBmpSize() { return this.bmpSize; }

    public int textureID() { return this.texture; }

    public int ascent() { return this.ascent; }

    public int descent() { return this.descent; }

    public int lineGap() { return this.lineGap; }

    public float scale() { return this.scale; }
}
