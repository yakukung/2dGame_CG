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

        int newItemX, newItemY;

        // Determine item position based on maze level
        if (maze instanceof MazeLV_1) {
            newItemX = 5; // X-coordinate for MazeLV_1
            newItemY = 2; // Y-coordinate for MazeLV_1
        } else if (maze instanceof MazeLV_2) {
        	 newItemX = 3; // X-coordinate for MazeLV_2
             newItemY = 5; // Y-coordinate for MazeLV_2
        } else if (maze instanceof MazeLV_3) {
        	newItemX = 7; // X-coordinate for MazeLV_3
            newItemY = 7; // Y-coordinate for MazeLV_3
        } else {
            newItemX = 5; // Default X-coordinate
            newItemY = 2; // Default Y-coordinate
        }

        // Convert maze position to screen coordinates
        x = mazeOffsetX + newItemX * cellWidth + (cellWidth - size) / 2;
        y = mazeOffsetY + newItemY * cellHeight + (cellHeight - size) / 2;
        collected = false;
    }

    public void updatePositions(float mazeOffsetX, float mazeOffsetY, float cellWidth, float cellHeight, Maze maze) {
        if (!collected) {
            int newItemX, newItemY;

            // Determine item position based on maze level
            if (maze instanceof MazeLV_1) {
                newItemX = 5; // X-coordinate for MazeLV_1
                newItemY = 2; // Y-coordinate for MazeLV_1
            } else if (maze instanceof MazeLV_2) {
                newItemX = 3; // X-coordinate for MazeLV_2
                newItemY = 5; // Y-coordinate for MazeLV_2
            } else if (maze instanceof MazeLV_3) {
                newItemX = 7; // X-coordinate for MazeLV_3
                newItemY = 7; // Y-coordinate for MazeLV_3
            } else {
                newItemX = 5; // Default X-coordinate
                newItemY = 2; // Default Y-coordinate
            }

            // Update to fixed screen coordinates
            x = mazeOffsetX + newItemX * cellWidth + (cellWidth - size) / 2;
            y = mazeOffsetY + newItemY * cellHeight + (cellHeight - size) / 2;
        }
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

    public boolean isCollected() {
        return collected;
    }
}