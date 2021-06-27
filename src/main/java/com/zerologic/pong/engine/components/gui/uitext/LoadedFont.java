package com.zerologic.pong.engine.components.gui.uitext;

import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;

public class LoadedFont {

    // The LoadedFont class is simply instantiated with a created bitmap, so that any bitmaps
    // that are needed are created and stored in the FontLoader class.
    // TODO: Get UIFontLoader to create LoadedFonts for every new UIText instantiation

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
        return cdata;
    }

    public float getFontSize() { return fontSize; }

    public int getBmpSize() { return bmpSize; }

    public int textureID() { return texture; }

    public int ascent() { return ascent; }

    public int descent() { return descent; }

    public int lineGap() { return lineGap; }

    public float scale() { return scale; }
}
