package Game;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
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
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Robot implements GLEventListener {
    private Texture mainPageButtonTexture;
    private Texture pauseButtonTexture;
    private Texture restartButtonTexture;
    private Texture levelSelectionButtonTexture;

    private float x, y;
    private final float width = 50, height = 50;
    private final float speed = 50.0f;

    private int screenWidth, screenHeight;
    private int health = 3;
    private boolean gameOver = false;
    private boolean isPaused = false;
    private boolean hasWon = false;

    private TextRenderer textRenderer;
    private Texture heartTexture;
    private Texture robotTexture;
    private Texture wallTexture;
    private Texture nextButtonTexture;
    private final int heartSize = 30;

    private float mazeOffsetX;
    private float mazeOffsetY;

    private Maze maze;
    private List<Item> items; // Changed from single Item to List<Item>
    private GameUI gameUI;
    private int level;

    private Rectangle2D mainPageButtonRect;
    private Rectangle2D pauseButtonRect;
    private Rectangle2D restartButtonRect;
    private Rectangle2D levelSelectionButtonRect;
    private Rectangle2D nextButtonRect;
    private Texture backgroundTexture;

    // ไปแก้ Path อยู่ที่ MainPage ทำไว้แบบใช้ทุกคลาสแล้ว
    String imagePath = MainPage.filePath;
    private Clip gameMusic;
    private MainPage mainPage;
    private Clip winSound;
    private Clip loseSound;

    public Robot(Maze maze, int level) {
        this.maze = maze;
        this.level = level;
        this.items = new ArrayList<>();
        this.gameUI = new GameUI();
    }

    private void loadRobotTexture() {
        try {
            File robotFile = new File(imagePath + "Human.jpg");
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

    private void resetPosition() {
        x = mazeOffsetX + width;
        y = mazeOffsetY + height;
    }

    private void move(float dx, float dy) {
        if (gameOver || isPaused)
            return;

        int nextX = (int) ((x + dx - mazeOffsetX) / width);
        int nextY = (int) ((y + dy - mazeOffsetY) / height);

        if (maze.isWalkable(nextX, nextY)) {
            if (maze.isExit(nextX, nextY)) {
                if (areAllItemsCollected()) {
                    System.out.println("All items collected!");
                    System.out.println("🎉 Game Win! 🎉");
                    gameOver = true;
                    hasWon = true;
                    
                    // ตรวจสอบก่อนหยุดเสียงเพื่อป้องกันการเรียกซ้ำ
                    if (gameMusic != null && gameMusic.isRunning()) {
                        gameMusic.stop();
                    }
                    
                    if (winSound != null) {
                        winSound.setFramePosition(0);
                        winSound.start();
                    }
                    // Display the Next button
                    if (level < 3) {
                        nextButtonRect.setRect((screenWidth - 100) / 2, screenHeight / 2 + -100, 100, 50);
                    }
                } else {
                    // ตรวจสอบก่อนหยุดเสียงเพื่อป้องกันการเรียกซ้ำ
                    if (gameMusic != null && gameMusic.isRunning()) {
                        gameMusic.stop();
                    }
                    
                    // ตรวจสอบว่าเสียง lose ไม่ได้เล่นอยู่แล้วก่อนเล่นใหม่
                    if (loseSound != null && !loseSound.isRunning()) {
                        loseSound.setFramePosition(0);
                        loseSound.start();
                    }
                    
                    System.out.println("🚫 Need to collect " + getRemainingItemsCount() + " more items!");
                    gameOver = true;
                    hasWon = false;
                }
                return;
            }

            x += dx;
            y += dy;
            checkItemCollisions();
        } else {
            takeDamage();
        }
    }

    private Clip itemCollectSound;
    private Clip damageSound; // New variable for damage sound
    
    private void checkItemCollisions() {
        for (Item item : items) {
            boolean wasCollected = item.isCollected();
            item.checkCollision(x, y, width, height);
            
            // Play sound if item was just collected
            if (!wasCollected && item.isCollected()) {
                if (itemCollectSound != null) {
                    itemCollectSound.setFramePosition(0);
                    itemCollectSound.start();
                }
            }
        }
    }
    
    private boolean areAllItemsCollected() {
        for (Item item : items) {
            if (!item.isCollected()) {
                return false;
            }
        }
        return true;
    }

    private int getRemainingItemsCount() {
        int count = 0;
        for (Item item : items) {
            if (!item.isCollected()) {
                count++;
            }
        }
        return count;
    }

    private void takeDamage() {
        health--;
        System.out.println("หุ่นยนต์ชนผนัง! ชีวิตที่เหลือ: " + health);

        // Play damage sound when health is reduced
        if (damageSound != null) {
            damageSound.setFramePosition(0);
            damageSound.start();
        }

        if (health <= 0) {
            gameOver = true;
            hasWon = false;
            System.out.println("Game Over!");
            
            // ตรวจสอบก่อนหยุดเสียงเพื่อป้องกันการเรียกซ้ำ
            if (gameMusic != null && gameMusic.isRunning()) {
                gameMusic.stop();
            }
            
            // ตรวจสอบว่าเสียง lose ไม่ได้เล่นอยู่แล้วก่อนเล่นใหม่
            if (loseSound != null && !loseSound.isRunning()) {
                loseSound.setFramePosition(0);
                loseSound.start();
            }
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
        for (Item item : items) {
            item.reset(mazeOffsetX, mazeOffsetY, width, height, maze);
        }
        
        // ตรวจสอบสถานะเสียงก่อนเริ่มใหม่
        if (gameMusic != null && !gameMusic.isRunning()) {
            gameMusic.setFramePosition(0);
            gameMusic.loop(Clip.LOOP_CONTINUOUSLY);
            gameMusic.start();
        }
        
        System.out.println("เริ่มเกมใหม่!");
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));

        screenWidth = drawable.getSurfaceWidth();
        screenHeight = drawable.getSurfaceHeight();

        calculateCenterPosition();

        // Load textures
        try {
            // Wall texture
            File wallFile = new File(imagePath +"wall.jpg");
            if (wallFile.exists()) {
                wallTexture = TextureIO.newTexture(wallFile, true);
            }

            File heartFile = new File(imagePath +"heart.png");
            if (heartFile.exists()) {
                heartTexture = TextureIO.newTexture(heartFile, true);
                heartTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
                heartTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
                heartTexture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                heartTexture.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            }

            // Button textures
            mainPageButtonTexture = TextureIO.newTexture(
                    new File(imagePath + "Back.png"), true);
            pauseButtonTexture = TextureIO.newTexture(
                    new File(imagePath + "Pause.png"), true);
            restartButtonTexture = TextureIO.newTexture(
                    new File(imagePath + "reset.png"), true);
            levelSelectionButtonTexture = TextureIO.newTexture(
                    new File(imagePath + "menu.png"), true);
            nextButtonTexture = TextureIO.newTexture(
                    new File(imagePath + "Next.png"), true);

            // Background texture
            File backgroundFile = new File(imagePath + "BG_Maze.jpg");
            if (backgroundFile.exists()) {
                backgroundTexture = TextureIO.newTexture(backgroundFile, true);
            }
        } catch (IOException e) {
            System.err.println("Failed to load textures: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            File musicFile = new File(imagePath + "bgMoze.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile);
            gameMusic = AudioSystem.getClip();
            gameMusic.open(audioInputStream);
            gameMusic.loop(Clip.LOOP_CONTINUOUSLY);
            gameMusic.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading game music: " + ((Throwable) e).getMessage());
        }

        try {
            File winSoundFile = new File(imagePath + "win.wav");
            AudioInputStream winAudioInputStream = AudioSystem.getAudioInputStream(winSoundFile);
            winSound = AudioSystem.getClip();
            winSound.open(winAudioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading movement sound: " + e.getMessage());
        }
        try {
            File itemSoundFile = new File(imagePath + "collect.wav");
            AudioInputStream itemAudioInputStream = AudioSystem.getAudioInputStream(itemSoundFile);
            itemCollectSound = AudioSystem.getClip();
            itemCollectSound.open(itemAudioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading item collection sound: " + e.getMessage());
        }
        try {
            File damageSoundFile = new File(imagePath + "classic_hurt.wav");
            AudioInputStream damageAudioInputStream = AudioSystem.getAudioInputStream(damageSoundFile);
            damageSound = AudioSystem.getClip();
            damageSound.open(damageAudioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading damage sound: " + e.getMessage());
        }
        try {
            File loseSoundFile = new File(imagePath + "dead_ef.wav");
            AudioInputStream loseAudioInputStream = AudioSystem.getAudioInputStream(loseSoundFile);
            loseSound = AudioSystem.getClip();
            loseSound.open(loseAudioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading movement sound: " + e.getMessage());
        }

        // Button positions
        mainPageButtonRect = new Rectangle2D.Float(screenWidth - 60, screenHeight - 40, 50, 30);
        pauseButtonRect = new Rectangle2D.Float(screenWidth - 120, screenHeight - 40, 50, 30);
        restartButtonRect = new Rectangle2D.Float(screenWidth - 180, screenHeight - 40, 50, 30);
        levelSelectionButtonRect = new Rectangle2D.Float(screenWidth - 240, screenHeight - 40, 50, 30);
        nextButtonRect = new Rectangle2D.Float(-100, -100, 100, 50); // Initially off-screen

        loadRobotTexture();
        resetPosition();
        gameUI.init(screenWidth, screenHeight);
        initializeItems(); // Replaces item.init()

        // Input listeners
        GLWindow window = (GLWindow) drawable;
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
            // ตรวจสอบสถานะเสียงก่อนเริ่มใหม่
            if (gameMusic != null && !gameMusic.isRunning()) {
                gameMusic.setFramePosition(0);
                gameMusic.loop(Clip.LOOP_CONTINUOUSLY);
                gameMusic.start();
            }
            restartGame();
            return;
        }

        if (gameOver)
            return;

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

    private void handleMouseClick(MouseEvent e) {
        float mouseX = e.getX();
        float mouseY = screenHeight - e.getY();

        // กรีที่เกมจบและชนะ
        if (gameOver && hasWon) {
            // ตรวจสอบเฉพาะปุ่ม Next
            if (level < 3 && nextButtonRect.contains(mouseX, mouseY)) {
                Maze nextMaze = null;
                switch (level) {
                    case 1:
                        nextMaze = new MazeLV_2();
                        break;
                    case 2:
                        nextMaze = new MazeLV_3();
                        break;
                }

                if (nextMaze != null) {
                    Robot nextLevelRobot = new Robot(nextMaze, level + 1);
                    Randers.setGLEventListener(nextLevelRobot);
                }
                return;
            }
            // ถ้าคลิกนอกเหนือจากปุ่ม Next ให้ไม่ทำอะไร
            return;
        }

        // ตรวจสอบปุ่มอื่นๆ เฉพาะเมื่อไม่ได้อยู่สถานะเกมจบ
        if (!gameOver) {
            if (mainPageButtonRect.contains(mouseX, mouseY)) {
                goToMainPage();
            } else if (pauseButtonRect.contains(mouseX, mouseY)) {
                togglePause();
            } else if (restartButtonRect.contains(mouseX, mouseY)) {
                restartGame();
            } else if (levelSelectionButtonRect.contains(mouseX, mouseY)) {
                gameMusic.stop();
                goToLevelSelection();
            }
        }
    }

    private void goToMainPage() {
        // ตรวจสอบก่อนหยุดเสียงเพื่อป้องกันการเรียกซ้ำ
        if (gameMusic != null && gameMusic.isRunning()) {
            gameMusic.stop();
        }
        Randers.setGLEventListener(new MainPage());
    }

    private void goToLevelSelection() {
        // ตรวจสอบก่อนหยุดเสียงเพื่อป้องกันการเรียกซ้ำ
        if (gameMusic != null && gameMusic.isRunning()) {
            gameMusic.stop();
        }
        Randers.setGLEventListener(new LevelSelection(mainPage));
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        // Draw the background first
        if (backgroundTexture != null) {
            backgroundTexture.bind(gl);
            gl.glEnable(GL2.GL_TEXTURE_2D);
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0, 0);
            gl.glVertex2f(0, 0);
            gl.glTexCoord2f(1, 0);
            gl.glVertex2f(screenWidth, 0);
            gl.glTexCoord2f(1, 1);
            gl.glVertex2f(screenWidth, screenHeight);
            gl.glTexCoord2f(0, 1);
            gl.glVertex2f(0, screenHeight);
            gl.glEnd();
            gl.glDisable(GL2.GL_TEXTURE_2D);
        }

        drawMaze(gl, mazeOffsetX, mazeOffsetY, width, height);

        if (isPaused && !gameOver) {
            gameUI.drawPauseScreen(textRenderer, screenWidth, screenHeight);
            return;
        }

        if (!gameOver) {
            drawItemCounter();
            drawItems(gl);
            drawRobot(gl);
            drawHealth(gl);
        } else {
            if (hasWon) {
                gameUI.drawWinScreen(textRenderer, screenWidth, screenHeight);

                // วาดปุ่ม Next เฉพาะเมื่อชนะและไม่ใช่ด่านสุดท้าย
                if (level < 3 && nextButtonTexture != null) {
                    // วางปุ่ม Next ให้ห่างจากปุ่มอื่นๆ
                    nextButtonRect.setRect(
                            screenWidth / 2 - 50, // จัดกึ่งกลางหน้าจอ
                            screenHeight / 2 - 100, // ลงมาด้านล่าง
                            100, 50);

                    nextButtonTexture.bind(gl);
                    gl.glEnable(GL2.GL_TEXTURE_2D);
                    gl.glBegin(GL2.GL_QUADS);
                    gl.glTexCoord2f(0, 0);
                    gl.glVertex2f((float) nextButtonRect.getX(), (float) nextButtonRect.getY());
                    gl.glTexCoord2f(1, 0);
                    gl.glVertex2f((float) (nextButtonRect.getX() + nextButtonRect.getWidth()),
                            (float) nextButtonRect.getY());
                    gl.glTexCoord2f(1, 1);
                    gl.glVertex2f((float) (nextButtonRect.getX() + nextButtonRect.getWidth()),
                            (float) (nextButtonRect.getY() + nextButtonRect.getHeight()));
                    gl.glTexCoord2f(0, 1);
                    gl.glVertex2f((float) nextButtonRect.getX(),
                            (float) (nextButtonRect.getY() + nextButtonRect.getHeight()));
                    gl.glEnd();
                    gl.glDisable(GL2.GL_TEXTURE_2D);
                }
            } else {
                gameUI.drawGameOverScreen(textRenderer, screenWidth, screenHeight);
            }
        }

        // วาดปุ่มอื่นๆ เฉพาะเมื่อเกมยังไม่จบ
        if (!gameOver) {
            drawButtons(gl);
        }
    }

    private void drawItems(GL2 gl) {
        for (Item item : items) {
            item.draw(gl);
        }
    }

    private void drawRobot(GL2 gl) {
        if (robotTexture != null) {
            gl.glEnable(GL2.GL_TEXTURE_2D);
            robotTexture.bind(gl);
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
    }

    private void drawItemCounter() {
        textRenderer.beginRendering(screenWidth, screenHeight);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        textRenderer.draw("Items: " + (items.size() - getRemainingItemsCount()) + "/" + items.size(),
                screenWidth - 600, screenHeight - 40);
        textRenderer.endRendering();

    }

    private void drawButtons(GL2 gl) {
        drawButton(gl, mainPageButtonTexture, mainPageButtonRect);
        drawButton(gl, pauseButtonTexture, pauseButtonRect);
        drawButton(gl, restartButtonTexture, restartButtonRect);
        drawButton(gl, levelSelectionButtonTexture, levelSelectionButtonRect);
    }

    private void drawButton(GL2 gl, Texture texture, Rectangle2D rect) {
        if (texture != null) {
            gl.glEnable(GL2.GL_TEXTURE_2D);
            texture.bind(gl);
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2f((float) rect.getX(), (float) rect.getY());
            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex2f((float) (rect.getX() + rect.getWidth()), (float) rect.getY());
            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex2f((float) (rect.getX() + rect.getWidth()), (float) (rect.getY() + rect.getHeight()));
            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex2f((float) rect.getX(), (float) (rect.getY() + rect.getHeight()));
            gl.glEnd();
            gl.glDisable(GL2.GL_TEXTURE_2D);
        }
    }

    private void drawMaze(GL2 gl, float offsetX, float offsetY, float cellWidth, float cellHeight) {
        for (int j = 0; j < maze.getHeight(); j++) {
            for (int i = 0; i < maze.getWidth(); i++) {
                float cellX = offsetX + i * cellWidth;
                float cellY = offsetY + j * cellHeight;

                if (maze.getMazeData()[j][i] == 1) { // Wall
                    drawWall(gl, cellX, cellY, cellWidth, cellHeight);
                }
            }
        }

        // Draw exits after walls to ensure they are not overlapped
        for (int j = 0; j < maze.getHeight(); j++) {
            for (int i = 0; i < maze.getWidth(); i++) {
                float cellX = offsetX + i * cellWidth;
                float cellY = offsetY + j * cellHeight;

                if (maze.getMazeData()[j][i] == 2) { // Exit
                    drawExit(gl, cellX, cellY, cellWidth, cellHeight);
                }
            }
        }
    }

    private void drawWall(GL2 gl, float x, float y, float width, float height) {
        if (wallTexture != null) {
            gl.glEnable(GL2.GL_TEXTURE_2D);
            wallTexture.bind(gl);
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
        } else {
            // Fallback color - ตรงนี้ใช้สีแดงเข้ม
            gl.glColor3f(0.5f, 0.2f, 0.2f);
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex2f(x, y);
            gl.glVertex2f(x + width, y);
            gl.glVertex2f(x + width, y + height);
            gl.glVertex2f(x, y + height);
            gl.glEnd();
        }
    }

    private void drawExit(GL2 gl, float x, float y, float width, float height) {
        gl.glColor3f(0.0f, 0.8f, 0.0f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }

    private void drawHealth(GL2 gl) {
        if (heartTexture != null) {
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            gl.glEnable(GL2.GL_TEXTURE_2D);
            heartTexture.bind(gl);
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
            gl.glDisable(GL2.GL_BLEND);
        } else {
            textRenderer.beginRendering(screenWidth, screenHeight);
            textRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            textRenderer.draw("Health: " + health, 20, screenHeight - 40);
            textRenderer.endRendering();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (backgroundTexture != null)
            backgroundTexture.destroy(drawable.getGL().getGL2());
        if (heartTexture != null)
            heartTexture.destroy(drawable.getGL().getGL2());
        if (wallTexture != null)
            wallTexture.destroy(drawable.getGL().getGL2());
        if (robotTexture != null)
            robotTexture.destroy(drawable.getGL().getGL2());
        if (textRenderer != null)
            textRenderer.dispose();

            if (gameMusic != null && gameMusic.isRunning()) {
                gameMusic.stop();
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

        // Remove resetPosition() to prevent game reset on resize
        // resetPosition();

        for (Item item : items) {
            item.updatePositions(mazeOffsetX, mazeOffsetY, this.width, this.height, maze);
        }

        // Update button positions
        mainPageButtonRect.setRect(screenWidth - 60, screenHeight - 40, 50, 30);
        pauseButtonRect.setRect(screenWidth - 120, screenHeight - 40, 50, 30);
        restartButtonRect.setRect(screenWidth - 180, screenHeight - 40, 50, 30);
        levelSelectionButtonRect.setRect(screenWidth - 240, screenHeight - 40, 50, 30);
    }

    private void initializeItems() {
        items.clear();
        int itemCount = Item.getItemCountForLevel(level);
        for (int i = 0; i < itemCount; i++) {
            Item item = new Item(level, i);
            item.init(mazeOffsetX, mazeOffsetY, width, height, maze);
            items.add(item);
        }
    }
}