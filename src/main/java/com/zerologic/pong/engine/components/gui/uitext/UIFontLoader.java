package com.zerologic.pong.engine.components.gui.uitext;

import com.zerologic.pong.engine.ShaderProgram;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;

import static org.lwjgl.stb.STBTruetype.*;

import java.io.*;
import java.nio.*;
import java.util.Vector;

import static org.lwjgl.opengl.GL40.*;

public class UIFontLoader {

    /* TODO: FontLoader can only load a single font at a time, with only a single size,
        obviously this is not smart for a game.*/

    private static int bmpSize = 1024;

    private static STBTTFontinfo fontInfo;
    private static ByteBuffer data; // The buffer to store the font data
    private final static ByteBuffer bitmap = BufferUtils.createByteBuffer(bmpSize*bmpSize); // The buffer that has the actual drawn image of the bitmap
    private final static STBTTBakedChar.Buffer cdata = STBTTBakedChar.calloc(143); // Character data from the font, is 96 because the standard range of chars is from 32-96, 143 for special chars

    private final static String defaultFont = "C:/Windows/Fonts/Arial.ttf"; // To be defined by programmer, this is a font that the loader defaults to if there is not fonts found at the given parameter
    private static String fontPath;

    private static int ascent;
    private static int descent;
    private static int lineGap;
    private static float scale;

    private static int texture; // bitmap texture handle

    private static float fontHeight;

    private static ShaderProgram txtShader;

    private static Vector<LoadedFont> loadedFonts = new Vector<>();

    // Returns true on successful font load
    protected static boolean generateBitmap(float size) {
        fontHeight = size;

        if(checkFontExists(fontHeight)) {
            return true;
        }

        if(fontHeight < 0) {
            fontHeight = 0;
            System.err.println("Font size cannot be below 0!");
        }

        fontInfo = STBTTFontinfo.create();

        if(!stbtt_InitFont(fontInfo, data)) {
            return false;
        }

        IntBuffer bAscent = BufferUtils.createIntBuffer(1);
        IntBuffer bDescent = BufferUtils.createIntBuffer(1);
        IntBuffer bLineGap = BufferUtils.createIntBuffer(1);

        stbtt_GetFontVMetrics(fontInfo, bAscent, bDescent, bLineGap);

        ascent = bAscent.get();
        descent = bDescent.get();
        lineGap = bLineGap.get();

        scale = stbtt_ScaleForPixelHeight(fontInfo, fontHeight);

        // Bakes the font to a texture of size bmpSize*bmpSize with the first character as codepoint 32
        stbtt_BakeFontBitmap(data, fontHeight, bitmap, bmpSize, bmpSize, 32, cdata);
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        LoadedFont font = new LoadedFont(fontPath, fontHeight);
        font.setData(cdata, ascent, descent, lineGap, scale, bmpSize, bitmap, texture);
        loadedFonts.add(font);

        glPixelStorei(GL_UNPACK_ALIGNMENT, GL_TRUE); // Disable 4-byte pixel alignment

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, font.getBmpSize(), font.getBmpSize(), 0, GL_RED, GL_UNSIGNED_BYTE, getFontBySize(fontHeight).getBitmap());
        glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);

        // Clear/free all data from UIFontLoader when there's no need to hold any data.
        cdata.clear();
        data.clear();
        bitmap.clear();

        return true;
    }

    // Defaults to 1024 if no size is specified.. no guarantee the font fits on the bitmap!
    public static void init(ShaderProgram shader, String filepath) {
        txtShader = shader;
        data = fileToBytebuffer(filepath);
        fontPath = filepath;
    }

    public static void init(int bitmapSize, ShaderProgram shader, String filepath) {
        bmpSize = bitmapSize;
        txtShader = shader;
        data = fileToBytebuffer(filepath);
        fontPath = filepath;
    }

    static ByteBuffer fileToBytebuffer(String fpath) {
        File file = new File(fpath);

        if(!file.exists()) {
            System.err.println("Error loading font. Loading default font (" + defaultFont + ")");
            file = new File(defaultFont);
        }

        try (InputStream stream = new FileInputStream(file)) {
            ByteBuffer buf = BufferUtils.createByteBuffer((int) file.length());

            buf.put(stream.readAllBytes());
            buf.flip();

            return buf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ByteBuffer.allocate(0);
    }

    public static void destroy() {
        fontInfo.free();
        stbtt_FreeBitmap(bitmap);

        cdata.clear();
        data.clear();
        bitmap.clear();

        loadedFonts.clear();
    }

    public static ShaderProgram getShaderProgram() { return txtShader; }

    public static void printLoadedFonts() {
        for(LoadedFont f : loadedFonts) {
            System.out.println("Texture ID: " + f.getTextureID() + "\n" +
                    "Texture size: " + f.getFontSize() + "\n" +
                    "X advance: " + f.getCharData().xadvance());
        }
    }

    public static LoadedFont getFontBySize(float size) {
        for(LoadedFont f : loadedFonts) {
            if (size == f.getFontSize()) {
                return f;
            }
        }
        System.out.println("Unable to find font, generating new one!");
        generateBitmap(size);
        return loadedFonts.lastElement();
    }

    private static boolean checkFontExists(float size) {
        for (LoadedFont f : loadedFonts) {
            if(f.getFontSize() == size){
                return true;
            }
        }
        return false;
    }

    public static STBTTBakedChar.Buffer getCharData() {
        return cdata;
    }
}
