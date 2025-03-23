// Item
package Game;

import com.jogamp.opengl.GL2;
import java.util.Random;

public class Item {
    private float x, y; // ตำแหน่งของไอเทม
    private final float size = 20; // ขนาดของไอเทม
    private boolean collected = false; // สถานะการเก็บไอเทม
    private Random random = new Random(); // Random number generator
    private int collectedCount = 0; // Counter for item collection
    private final int maxDrops = 2; // จำนวนการดรอปของไอเทม

    public Item() {
        // Constructor
    }

    public void init(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        respawnItem(mazeOffsetX, mazeOffsetY, cellWidth, cellHeight, maze);
    }

    public void reset(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        collected = false;
        collectedCount = 0;
        respawnItem(mazeOffsetX, mazeOffsetY, cellWidth, cellHeight, maze);
    }

    public void updatePosition(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        if (collected) {
            respawnItem(mazeOffsetX, mazeOffsetY, cellWidth, cellHeight, maze);
        }
    }

    public void checkCollision(float playerX, float playerY, float playerWidth, float playerHeight) {
        if (!collected) {
            // ตรวจสอบการชนกันระหว่างผู้เล่นและไอเทม
            boolean collisionX = playerX + playerWidth >= x && x + size >= playerX;
            boolean collisionY = playerY + playerHeight >= y && y + size >= playerY;

            if (collisionX && collisionY) {
                collected = true;
                collectedCount++;
                System.out.println("เก็บไอเทมได้! จำนวนที่เก็บได้: " + collectedCount);
            }
        }
    }

    private void respawnItem(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        if (collectedCount >= maxDrops) {
            collected = true; // Do not show item if max drops reached
            return;
        }

        // Fixed position where mazeData is 0
        int newItemX = 5; // X-coordinate in the maze
        int newItemY = 2; // Y-coordinate in the maze

        // Convert maze position to screen coordinates
        x = mazeOffsetX + newItemX * cellWidth + (cellWidth - size) / 2;
        y = mazeOffsetY + newItemY * cellHeight + (cellHeight - size) / 2;
        collected = false;
    }

    public void draw(GL2 gl) {
        if (!collected) {
            gl.glColor3f(1.0f, 0.5f, 0.0f); // สีของไอเทม (สีส้ม)
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex2f(x, y);
            gl.glVertex2f(x + size, y);
            gl.glVertex2f(x + size, y + size);
            gl.glVertex2f(x, y + size);
            gl.glEnd();
        }
    }

    public void updatePositions(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight) {
        // Keep the item at the fixed position (5, 2) when the window is resized
        if (!collected) {
            int newItemX = 5; // X-coordinate in the maze
            int newItemY = 2; // Y-coordinate in the maze

            // Update to fixed screen coordinates
            x = mazeOffsetX + newItemX * cellWidth + (cellWidth - size) / 2;
            y = mazeOffsetY + newItemY * cellHeight + (cellHeight - size) / 2;
        }
    }

    public boolean isCollected() {
        return collected;
    }
}