package Game;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.graph.ui.shapes.Rectangle;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

public class Robot implements GLEventListener {
    private Texture mainPageButtonTexture;
    private Texture pauseButtonTexture;
    private Texture restartButtonTexture;
    private Texture levelSelectionButtonTexture;

    private float x, y;
    private final float width = 50, height = 50;
    private final float speed = 10.0f;

    private int screenWidth, screenHeight;
    private int health = 3;
    private boolean gameOver = false;
    private boolean isPaused = false;
    private boolean hasWon = false;

    private TextRenderer textRenderer;
    private Texture heartTexture;
    private Texture robotTexture;
    private Texture wallTexture; // Wall texture for maze
    private final int heartSize = 30;

    private float mazeOffsetX;
    private float mazeOffsetY;

    private Maze maze;
    private Item item;
    private GameUI gameUI;

    private Rectangle2D mainPageButtonRect; // Define button area
    private Rectangle2D pauseButtonRect; // Define pause button area
    private Rectangle2D restartButtonRect; // Define restart button area
    private Rectangle2D levelSelectionButtonRect;

    public Robot(Maze maze) {
        this.maze = maze;
        item = new Item();
        gameUI = new GameUI();
        resetPosition();
    }

    private void loadRobotTexture() {
        try {
            File robotFile = new File("D:\\Computer Graphics\\MyFinal\\Human.jpg"); // Corrected file path
            if (robotFile.exists()) {
                robotTexture = TextureIO.newTexture(robotFile, true);
                System.out.println("Robot texture loaded successfully");
            } else {
                System.err.println("Robot texture file not found: " + robotFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to load robot texture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void calculateCenterPosition() {
        mazeOffsetX = (screenWidth - (maze.getWidth() * width)) / 2;
        mazeOffsetY = (screenHeight - (maze.getHeight() * height)) / 2;
    }

    public void resetPosition() {
        x = mazeOffsetX + width;
        y = mazeOffsetY + height;
    }

    public void move(float dx, float dy) {
        if (gameOver || isPaused)
            return;

        int nextX = (int) ((x + dx - mazeOffsetX) / width);
        int nextY = (int) ((y + dy - mazeOffsetY) / height);

        if (maze.isWalkable(nextX, nextY)) {
            if (maze.isExit(nextX, nextY)) {
                if (item.isCollected()) {
                    System.out.println("Robot รู้ว่าไอเทมถูกเก็บแล้ว!");
                    System.out.println("🎉 Game Win! 🎉");
                    gameOver = true;
                    hasWon = true;
                } else {
                    System.out.println("🚫 ต้องเก็บไอเทมก่อนถึงจะออกได้!");
                    gameOver = true;
                    hasWon = false;
                    System.out.println("Game Over!");
                }
                return;
            }

            x += dx;
            y += dy;
            item.checkCollision(x, y, width, height); // Ensure collision check uses robot's width and height
        } else {
            takeDamage();
        }
    }

    private void takeDamage() {
        health--;
        System.out.println("หุ่นยนต์ชนผนัง! ชีวิตที่เหลือ: " + health);

        if (health <= 0) {
            gameOver = true;
            hasWon = false;
            System.out.println("Game Over!");
        } else {
            resetPosition();
        }
    }

    public void togglePause() {
        isPaused = !isPaused;
        System.out.println("Paused: " + isPaused);
    }

    private void restartGame() {
        health = 3;
        gameOver = false;
        hasWon = false;
        resetPosition();
        item.reset(mazeOffsetX, mazeOffsetY, width, height, maze);
        System.out.println("เริ่มเกมใหม่!");
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));

        screenWidth = drawable.getSurfaceWidth();
        screenHeight = drawable.getSurfaceHeight();

        calculateCenterPosition();
        
     // Load wall texture
        try {
            File wallFile = new File("D:\\Computer Graphics\\Project2D\\wall.jpg");
            if (wallFile.exists()) {
                wallTexture = TextureIO.newTexture(wallFile, true);
                System.out.println("Wall texture loaded successfully");
            } else {
                System.err.println("Wall texture file not found: " + wallFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to load wall texture: " + e.getMessage());
            e.printStackTrace();
        }

     // Adjust button positions with increased spacing
        mainPageButtonRect = new Rectangle2D.Float(screenWidth - 60, screenHeight - 40, 50, 30);
        pauseButtonRect = new Rectangle2D.Float(screenWidth - 120, screenHeight - 40, 50, 30); // Increased spacing
        restartButtonRect = new Rectangle2D.Float(screenWidth - 180, screenHeight - 40, 50, 30); // Increased spacing
        levelSelectionButtonRect = new Rectangle2D.Float(screenWidth - 240, screenHeight - 40, 50, 30); // 

        // Load heart texture
        try {
            File heartFile = new File("D:\\Computer Graphics\\Project2D\\heart.png");
            if (heartFile.exists()) {
                heartTexture = TextureIO.newTexture(heartFile, true);
                System.out.println("Heart texture loaded successfully");
            } else {
                System.err.println("Heart texture file not found: " + heartFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to load heart texture: " + e.getMessage());
            e.printStackTrace();
        }

        // Load textures for buttons
        try {
            File mainPageButtonFile = new File("D:\\Computer Graphics\\Project2D\\Pause.png");
            if (mainPageButtonFile.exists()) {
                mainPageButtonTexture = TextureIO.newTexture(mainPageButtonFile, true);
                System.out.println("Main Page button texture loaded successfully");
            } else {
                System.err.println("Main Page button texture file not found: " + mainPageButtonFile.getAbsolutePath());
            }

            File pauseButtonFile = new File("D:\\Computer Graphics\\Project2D\\Pause.png");
            if (pauseButtonFile.exists()) {
                pauseButtonTexture = TextureIO.newTexture(pauseButtonFile, true);
                System.out.println("Pause button texture loaded successfully");
            } else {
                System.err.println("Pause button texture file not found: " + pauseButtonFile.getAbsolutePath());
            }

            File restartButtonFile = new File("D:\\Computer Graphics\\Project2D\\Pause.png");
            if (restartButtonFile.exists()) {
                restartButtonTexture = TextureIO.newTexture(restartButtonFile, true);
                System.out.println("Restart button texture loaded successfully");
            } else {
                System.err.println("Restart button texture file not found: " + restartButtonFile.getAbsolutePath());
            }

            File levelSelectionButtonFile = new File("D:\\Computer Graphics\\Project2D\\Pause.png");
            if (levelSelectionButtonFile.exists()) {
                levelSelectionButtonTexture = TextureIO.newTexture(levelSelectionButtonFile, true);
                System.out.println("Level Selection button texture loaded successfully");
            } else {
                System.err.println(
                        "Level Selection button texture file not found: " + levelSelectionButtonFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to load button textures: " + e.getMessage());
            e.printStackTrace();
        }

        loadRobotTexture(); // Load robot texture separately

        resetPosition();
        gameUI.init(screenWidth, screenHeight);
        item.init(mazeOffsetX, mazeOffsetY, width, height, maze);

        GLWindow window = (GLWindow) drawable;
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        restartGame();
                    }
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        move(0, speed);
                        break;
                    case KeyEvent.VK_S:
                        move(0, -speed);
                        break;
                    case KeyEvent.VK_A:
                        move(-speed, 0);
                        break;
                    case KeyEvent.VK_D:
                        move(speed, 0);
                        break;
                    case KeyEvent.VK_P:
                        togglePause();
                        break;
                }
            }
        });

        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                float mouseX = e.getX();
                float mouseY = screenHeight - e.getY(); // Convert to OpenGL coordinates

                // Check if the MainPage button is clicked
                if (mainPageButtonRect.contains(mouseX, mouseY)) {
                    goToMainPage();
                }

                // Check if the pause button is clicked
                if (pauseButtonRect.contains(mouseX, mouseY)) {
                    togglePause();
                }

                // Check if the restart button is clicked
                if (restartButtonRect.contains(mouseX, mouseY)) {
                    restartGame();
                }

                // Check if the level selection button is clicked
                if (levelSelectionButtonRect.contains(mouseX, mouseY)) {
                    goToLevelSelection();
                }
            }
        });
    }

    private void goToMainPage() {
        // Logic to switch to the MainPage
        System.out.println("Returning to MainPage");
        Randers.setGLEventListener(new MainPage());
    }

    private void goToLevelSelection() {
        // Logic to switch to the LevelSelection
        System.out.println("Returning to Level Selection");
        Randers.setGLEventListener(new LevelSelection());
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        // Draw the maze with wall texture
        drawMaze(gl, mazeOffsetX, mazeOffsetY, width, height);

        if (isPaused && !gameOver) {
            gameUI.drawPauseScreen(textRenderer, screenWidth, screenHeight);
            return;
        }

        if (!gameOver) {
            // Draw the robot with texture
            if (robotTexture != null) {
                gl.glEnable(GL2.GL_TEXTURE_2D);
                robotTexture.bind(gl);
                gl.glColor3f(1.0f, 1.0f, 1.0f);

                gl.glBegin(GL2.GL_QUADS);
                gl.glTexCoord2f(0.0f, 0.0f);
                gl.glVertex2f(x, y);
                gl.glTexCoord2f(1.0f, 0.0f);
                gl.glVertex2f(x + width, y);
                gl.glTexCoord2f(1.0f, 1.0f);
                gl.glVertex2f(x + width, y + height);
                gl.glTexCoord2f(0.0f, 1.0f);
                gl.glVertex2f(x, y + height);
                gl.glEnd();

                gl.glDisable(GL2.GL_TEXTURE_2D);
            }
            // Draw items
            item.draw(gl);

            // Draw health
            drawHealth(gl);
        } else {
            // Check if player has won or lost
            if (hasWon) {
                gameUI.drawWinScreen(textRenderer, screenWidth, screenHeight);
            } else {
                gameUI.drawGameOverScreen(textRenderer, screenWidth, screenHeight);
            }
        }

        // Begin text rendering
        textRenderer.beginRendering(screenWidth, screenHeight);

        // Use the same texture for all buttons
        if (pauseButtonTexture != null) {
            gl.glEnable(GL2.GL_TEXTURE_2D);
            pauseButtonTexture.bind(gl);
            gl.glColor3f(1.0f, 1.0f, 1.0f); // Set color to white for texture rendering

            // Draw MainPage button
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2f((float) mainPageButtonRect.getX(), (float) mainPageButtonRect.getY());
            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex2f((float) (mainPageButtonRect.getX() + mainPageButtonRect.getWidth()),
                    (float) mainPageButtonRect.getY());
            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex2f((float) (mainPageButtonRect.getX() + mainPageButtonRect.getWidth()),
                    (float) (mainPageButtonRect.getY() + mainPageButtonRect.getHeight()));
            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex2f((float) mainPageButtonRect.getX(),
                    (float) (mainPageButtonRect.getY() + mainPageButtonRect.getHeight()));
            gl.glEnd();

            // Draw Pause button
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2f((float) pauseButtonRect.getX(), (float) pauseButtonRect.getY());
            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex2f((float) (pauseButtonRect.getX() + pauseButtonRect.getWidth()),
                    (float) pauseButtonRect.getY());
            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex2f((float) (pauseButtonRect.getX() + pauseButtonRect.getWidth()),
                    (float) (pauseButtonRect.getY() + pauseButtonRect.getHeight()));
            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex2f((float) pauseButtonRect.getX(),
                    (float) (pauseButtonRect.getY() + pauseButtonRect.getHeight()));
            gl.glEnd();

            // Draw Restart button
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2f((float) restartButtonRect.getX(), (float) restartButtonRect.getY());
            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex2f((float) (restartButtonRect.getX() + restartButtonRect.getWidth()),
                    (float) restartButtonRect.getY());
            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex2f((float) (restartButtonRect.getX() + restartButtonRect.getWidth()),
                    (float) (restartButtonRect.getY() + restartButtonRect.getHeight()));
            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex2f((float) restartButtonRect.getX(),
                    (float) (restartButtonRect.getY() + restartButtonRect.getHeight()));
            gl.glEnd();

            // Draw Level Selection button
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2f((float) levelSelectionButtonRect.getX(), (float) levelSelectionButtonRect.getY());
            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex2f((float) (levelSelectionButtonRect.getX() + levelSelectionButtonRect.getWidth()),
                    (float) levelSelectionButtonRect.getY());
            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex2f((float) (levelSelectionButtonRect.getX() + levelSelectionButtonRect.getWidth()),
                    (float) (levelSelectionButtonRect.getY() + levelSelectionButtonRect.getHeight()));
            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex2f((float) levelSelectionButtonRect.getX(),
                    (float) (levelSelectionButtonRect.getY() + levelSelectionButtonRect.getHeight()));
            gl.glEnd();

            gl.glDisable(GL2.GL_TEXTURE_2D);
        }

        // End text rendering
        textRenderer.endRendering();
    }

    private void drawMaze(GL2 gl, float offsetX, float offsetY, float cellWidth, float cellHeight) {
        for (int j = 0; j < maze.getHeight(); j++) {
            for (int i = 0; i < maze.getWidth(); i++) {
                float cellX = offsetX + i * cellWidth;
                float cellY = offsetY + j * cellHeight;

                if (maze.getMazeData()[j][i] == 1) {
                    if (wallTexture != null) {
                        gl.glEnable(GL2.GL_TEXTURE_2D);
                        wallTexture.bind(gl);
                        gl.glColor3f(1.0f, 1.0f, 1.0f);

                        gl.glBegin(GL2.GL_QUADS);
                        gl.glTexCoord2f(0.0f, 0.0f);
                        gl.glVertex2f(cellX, cellY);
                        gl.glTexCoord2f(1.0f, 0.0f);
                        gl.glVertex2f(cellX + cellWidth, cellY);
                        gl.glTexCoord2f(1.0f, 1.0f);
                        gl.glVertex2f(cellX + cellWidth, cellY + cellHeight);
                        gl.glTexCoord2f(0.0f, 1.0f);
                        gl.glVertex2f(cellX, cellY + cellHeight);
                        gl.glEnd();

                        gl.glDisable(GL2.GL_TEXTURE_2D);
                    } else {
                        // Fallback to alternating red shades
                        switch ((i + j) % 3) {
                            case 0:
                                gl.glColor3f(1.0f, 0.5f, 0.5f);
                                break;
                            case 1:
                                gl.glColor3f(1.0f, 0.3f, 0.3f);
                                break;
                            case 2:
                                gl.glColor3f(1.0f, 0.1f, 0.1f);
                                break;
                        }
                        gl.glBegin(GL2.GL_QUADS);
                        gl.glVertex2f(cellX, cellY);
                        gl.glVertex2f(cellX + cellWidth, cellY);
                        gl.glVertex2f(cellX + cellWidth, cellY + cellHeight);
                        gl.glVertex2f(cellX, cellY + cellHeight);
                        gl.glEnd();
                    }
                } else if (maze.getMazeData()[j][i] == 2) {
                    gl.glColor3f(0.0f, 0.8f, 0.0f);
                    gl.glBegin(GL2.GL_QUADS);
                    gl.glVertex2f(cellX, cellY);
                    gl.glVertex2f(cellX + cellWidth, cellY);
                    gl.glVertex2f(cellX + cellWidth, cellY + cellHeight);
                    gl.glVertex2f(cellX, cellY + cellHeight);
                    gl.glEnd();
                }
            }
        }
    }

    private void drawHealth(GL2 gl) {
        if (heartTexture != null) {
            gl.glEnable(GL2.GL_TEXTURE_2D);
            heartTexture.bind(gl);
            gl.glColor3f(1.0f, 1.0f, 1.0f); // Set color to white for texture rendering

            for (int i = 0; i < health; i++) {
                float heartX = 20 + i * (heartSize + 5);
                float heartY = screenHeight - heartSize - 20;

                gl.glBegin(GL2.GL_QUADS);
                gl.glTexCoord2f(0, 0);
                gl.glVertex2f(heartX, heartY);
                gl.glTexCoord2f(1, 0);
                gl.glVertex2f(heartX + heartSize, heartY);
                gl.glTexCoord2f(1, 1);
                gl.glVertex2f(heartX + heartSize, heartY + heartSize);
                gl.glTexCoord2f(0, 1);
                gl.glVertex2f(heartX, heartY + heartSize);
                gl.glEnd();
            }

            gl.glDisable(GL2.GL_TEXTURE_2D);
        } else {
            // Fallback if texture loading failed
            textRenderer.beginRendering(screenWidth, screenHeight);
            textRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            textRenderer.draw("Health: " + health, 20, screenHeight - 40);
            textRenderer.endRendering();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (heartTexture != null) {
            heartTexture.destroy(drawable.getGL().getGL2());
        }
        if (wallTexture != null) {
            wallTexture.destroy(drawable.getGL().getGL2());
        }
        if (robotTexture != null) {
            robotTexture.destroy(drawable.getGL().getGL2());
        }
        if (textRenderer != null) {
            textRenderer.dispose();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        screenWidth = width;
        screenHeight = height;
        calculateCenterPosition();
        resetPosition();
        item.updatePositions(mazeOffsetX, mazeOffsetY, this.width, this.height, maze); // Pass maze object

     // Recalculate button positions with increased spacing
        mainPageButtonRect.setRect(screenWidth - 60, screenHeight - 40, 50, 30);
        pauseButtonRect.setRect(screenWidth - 120, screenHeight - 40, 50, 30); // Increased spacing
        restartButtonRect.setRect(screenWidth - 180, screenHeight - 40, 50, 30); // Increased spacing
        levelSelectionButtonRect.setRect(screenWidth - 240, screenHeight - 40, 50, 30); // Increased spacing
    }
}