package com.zerologic.pong.engine.components.text;

import com.zerologic.pong.engine.Texture;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.lwjgl.opengl.GL11.GL_RGBA;

public class FontLoader {

    // This class needs to read in a certain number of things from the fnt file.
    // id(ascii code), x position, y position, width, height, xoffset, yoffset, xadvance
    static Character[] characters;
    static Texture charAtlas;
    static String charData;

    BufferedReader reader;

    public static Texture getCharAtlas() {
        return charAtlas;
    }

    public static void init(String charAtlasPath, String charDataPath, boolean flipTex) {

        try {
            charAtlas = new Texture(charAtlasPath, flipTex, GL_RGBA);
        } catch (Exception e) {
            System.err.println("ERROR: Texture not found for the FontLoader! Using default font...");
            charAtlas = new Texture ("./res/font/arial.png", flipTex, GL_RGBA);
            charData = "./res/font/arial.fnt";
        }

        charData = charDataPath;
    }

    public static Character[] loadFont() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(charData));

            int charCount = 0;
            String[] characterData;

            // Get char count
            for(int i=0; i<4; i++) {
                if(i<3) {
                    reader.readLine();
                } else {
                    String line = reader.readLine().replaceAll("[^0-9]", "");
                    charCount = Integer.parseInt(line);
                }
            }

            characters = new Character[charCount];

            for (int i=0; i<charCount; i++) {
                characterData = reader.readLine().split("\\s+");

                for(int b=0; b<characterData.length; b++) {
                    characterData[b] = characterData[b].replaceAll("[^0-9]", "");
                    characterData[b] = characterData[b].replaceAll("\\s+","");
                }

                characters[i] = new Character(
                        Integer.parseInt(characterData[1]),
                        Integer.parseInt(characterData[2]),
                        Integer.parseInt(characterData[3]),
                        Integer.parseInt(characterData[4]),
                        Integer.parseInt(characterData[5]),
                        Integer.parseInt(characterData[6]),
                        Integer.parseInt(characterData[7]),
                        Integer.parseInt(characterData[8])
                );
            }
            return characters;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Character[]{};
    }
}
