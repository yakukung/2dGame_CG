package Game;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Item {
    private float x, y;
    private final float size = 40;
    private boolean collected = false;
    private int level;
    private int itemIndex;
 
    
    private Texture[] itemTextures = new Texture[5]; // เก็บภาพ 2 เฟรม
    private int currentFrame = 0; // เฟรมปัจจุบัน
    private long lastFrameTime = 0; // เวลาสุดท้ายที่เปลี่ยนเฟรม
    private static final long FRAME_DURATION = 200; // เวลาเปลี่ยนเฟรม (มิลลิวินาที)


    private static final int[][][] LEVEL_ITEM_POSITIONS = {
        {{5, 2}}, // Level 1 - 1 item
        {{3, 5}, {5, 3}}, // Level 2 - 2 items
        {{11, 3}, {1, 7}, {7, 9}} // Level 3 - 3 items
    };

    public Item(int level, int itemIndex) {
        this.level = level;
        this.itemIndex = itemIndex;
        loadItemTexture();
    }

    private void loadItemTexture() {
        try {
        	String imagePath = MainPage.filePath;
        	itemTextures = new Texture[5]; // 5 frame
            for (int i = 0; i < itemTextures.length; i++) {
                File coinCastFile = new File(imagePath + "coincase" + (i + 1) + ".png");
                
                // ตรวจสอบว่าไฟล์มีอยู่จริงหรือไม่
                if (coinCastFile.exists()) {
                	itemTextures[i] = TextureIO.newTexture(coinCastFile, true);
                } else {
                    System.err.println("Cannot load coin textures from : " + coinCastFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load robot textures: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void updateFrame() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > FRAME_DURATION) {
            currentFrame = (currentFrame + 1) % itemTextures.length; // สลับระหว่าง 0 และ 1
            lastFrameTime = currentTime; // อัปเดตเวลา
        }
    }



    public void init(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        respawnItem(mazeOffsetX, mazeOffsetY, cellWidth, cellHeight, maze);
    }

    public void checkCollision(float playerX, float playerY, float playerWidth, float playerHeight) {
        if (!collected) {
            boolean collisionX = playerX + playerWidth >= x && x + size >= playerX;
            boolean collisionY = playerY + playerHeight >= y && y + size >= playerY;

            if (collisionX && collisionY) {
                collected = true;
                System.out.println("Item " + itemIndex + " collected in level " + level);
            }
        }
    }

    public void respawnItem(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        if (level <= LEVEL_ITEM_POSITIONS.length && itemIndex < LEVEL_ITEM_POSITIONS[level-1].length) {
            int[] pos = LEVEL_ITEM_POSITIONS[level-1][itemIndex];
            x = mazeOffsetX + pos[0] * cellWidth + (cellWidth - size) / 2;
            y = mazeOffsetY + pos[1] * cellHeight + (cellHeight - size) / 2 - 1;
            collected = false;
        }
    }

    public void draw(GL2 gl) {
        if (!collected && itemTextures[currentFrame] != null) {
            updateFrame(); // อัปเดตเฟรมก่อนวาด
            
            gl.glEnable(GL2.GL_TEXTURE_2D);
            itemTextures[currentFrame].bind(gl); // ใช้ภาพตามเฟรมปัจจุบัน

            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(x, y);
            gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(x + size, y);
            gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(x + size, y + size);
            gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(x, y + size);
            gl.glEnd();

            gl.glDisable(GL2.GL_TEXTURE_2D);
        }
    }


    public boolean isCollected() {
        return collected;
    }

    public void updateForNewLevel(int newLevel, float mazeOffsetX, float mazeOffsetY, 
            float cellWidth, float cellHeight, Maze maze) {
        this.level = newLevel;
        loadItemTexture(); // โหลด Texture ใหม่เมื่อเปลี่ยนเลเวล
        init(mazeOffsetX, mazeOffsetY, cellWidth, cellHeight, maze);
    }

    public void updatePositions(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        if (!collected) {
            int[] pos = LEVEL_ITEM_POSITIONS[level-1][itemIndex];
            x = mazeOffsetX + pos[0] * cellWidth + (cellWidth - size) / 2;
            y = mazeOffsetY + pos[1] * cellHeight + (cellHeight - size) / 2;
        }
    }

    public int getRemainingItems() {
        return collected ? 0 : 1;
    }

    public void reset(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        collected = false;
        respawnItem(mazeOffsetX, mazeOffsetY, cellWidth, cellHeight, maze);
    }

    public static int getItemCountForLevel(int level) {
        if (level > 0 && level <= LEVEL_ITEM_POSITIONS.length) {
            return LEVEL_ITEM_POSITIONS[level-1].length;
        }
        return 0;
    }
}