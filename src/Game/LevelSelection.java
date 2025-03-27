package Game;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class LevelSelection implements GLEventListener {
    private List<Rectangle2D> levelButtons;
    private Rectangle2D mainPageButtonRect;
    private int selectedLevel = -1;

    // Textures for buttons
    private Texture level1Texture;
    private Texture level2Texture;
    private Texture level3Texture;
    private Texture mainPageTexture;
    private Texture backgroundTexture;
    private MainPage mainPage;

    public LevelSelection(MainPage mainPage) {
        this.mainPage = mainPage;
    }

    // Inner class for texture loading
    private static class TextureLoader {
        public static Texture loadTexture(GL2 gl, String filePath) {
            try {
                return TextureIO.newTexture(new File(filePath), true);
            } catch (IOException e) {
                System.err.println("Error loading texture: " + filePath);
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        
        // ไปแก้ Path อยู่ที่ MainPage ทำไว้แบบใช้ทุกคลาสแล้ว
        String imagePath = MainPage.filePath;
        level1Texture = TextureLoader.loadTexture(gl, imagePath + "level1.png");
        level2Texture = TextureLoader.loadTexture(gl, imagePath + "level2.png");
        level3Texture = TextureLoader.loadTexture(gl, imagePath + "level3.png");
        mainPageTexture = TextureLoader.loadTexture(gl, imagePath + "Back.png");
        backgroundTexture = TextureLoader.loadTexture(gl, imagePath + "bgSelect.jpg");

        levelButtons = new ArrayList<>();
        int buttonWidth = 100;
        int buttonHeight = 50; // Increased height for images
        int spacing = 50;
        int totalWidth = (buttonWidth + spacing) * 3 - spacing;
        int startX = (drawable.getSurfaceWidth() - totalWidth) / 2;
        int yPosition = drawable.getSurfaceHeight() / 2;

        for (int i = 0; i < 3; i++) {
            int xPosition = startX + i * (buttonWidth + spacing);
            levelButtons.add(new Rectangle2D.Float(xPosition, yPosition, buttonWidth, buttonHeight));
        }

        // MainPage button position
        int mainPageButtonWidth = 100;
        int mainPageButtonHeight = 60;
        int mainPageButtonX = (drawable.getSurfaceWidth() - mainPageButtonWidth) / 2;
        int mainPageButtonY = yPosition + buttonHeight + 60;
        mainPageButtonRect = new Rectangle2D.Float(mainPageButtonX, mainPageButtonY,
                mainPageButtonWidth, mainPageButtonHeight);

                

        // Mouse listener
        Randers.getWindow().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = drawable.getSurfaceHeight() - e.getY();

                for (int i = 0; i < levelButtons.size(); i++) {
                    if (levelButtons.get(i).contains(mouseX, mouseY)) {
                        selectedLevel = i + 1;
                        System.out.println("Selected Level: " + selectedLevel + " - " +
                                (selectedLevel) + " items to collect");
                        SwingUtilities.invokeLater(() -> {
                            switch (selectedLevel) {
                                case 1:
                                    mainPage.stopBackgroundMusic();
                                    Randers.setGLEventListener(new Robot(new MazeLV_1(), selectedLevel));
                                    break;
                                case 2:
                                    mainPage.stopBackgroundMusic();
                                    Randers.setGLEventListener(new Robot(new MazeLV_2(), selectedLevel));
                                    break;
                                case 3:
                                    mainPage.stopBackgroundMusic();
                                    Randers.setGLEventListener(new Robot(new MazeLV_3(), selectedLevel));
                                    break;
                            }
                        });
                        return;
                    }
                }

                if (mainPageButtonRect.contains(mouseX, mouseY)) {
                    SwingUtilities.invokeLater(() -> {
                        Randers.setGLEventListener(new MainPage());
                    });
                }
            }
        });
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Draw the background first
        if (backgroundTexture != null) {
            backgroundTexture.bind(gl);
            gl.glEnable(GL2.GL_TEXTURE_2D);
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0, 0);
            gl.glVertex2f(0, 0);
            gl.glTexCoord2f(1, 0);
            gl.glVertex2f(drawable.getSurfaceWidth(), 0);
            gl.glTexCoord2f(1, 1);
            gl.glVertex2f(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
            gl.glTexCoord2f(0, 1);
            gl.glVertex2f(0, drawable.getSurfaceHeight());
            gl.glEnd();
            gl.glDisable(GL2.GL_TEXTURE_2D);
        }

        // Enable blending for transparency
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // Draw level buttons with textures
        for (int i = 0; i < levelButtons.size(); i++) {
            Rectangle2D button = levelButtons.get(i);
            Texture texture = null;
            switch (i) {
                case 0:
                    texture = level1Texture;
                    break;
                case 1:
                    texture = level2Texture;
                    break;
                case 2:
                    texture = level3Texture;
                    break;
            }

            if (texture != null) {
                texture.bind(gl);
                gl.glEnable(GL2.GL_TEXTURE_2D);
                gl.glBegin(GL2.GL_QUADS);
                gl.glTexCoord2f(0, 0);
                gl.glVertex2f((float) button.getX(), (float) button.getY());
                gl.glTexCoord2f(1, 0);
                gl.glVertex2f((float) (button.getX() + button.getWidth()), (float) button.getY());
                gl.glTexCoord2f(1, 1);
                gl.glVertex2f((float) (button.getX() + button.getWidth()),
                        (float) (button.getY() + button.getHeight()));
                gl.glTexCoord2f(0, 1);
                gl.glVertex2f((float) button.getX(), (float) (button.getY() + button.getHeight()));
                gl.glEnd();
                gl.glDisable(GL2.GL_TEXTURE_2D);
            }
        }

        // Draw main page button with texture
        if (mainPageTexture != null) {
            mainPageTexture.bind(gl);
            gl.glEnable(GL2.GL_TEXTURE_2D);
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0, 0);
            gl.glVertex2f((float) mainPageButtonRect.getX(), (float) mainPageButtonRect.getY());
            gl.glTexCoord2f(1, 0);
            gl.glVertex2f((float) (mainPageButtonRect.getX() + mainPageButtonRect.getWidth()),
                    (float) mainPageButtonRect.getY());
            gl.glTexCoord2f(1, 1);
            gl.glVertex2f((float) (mainPageButtonRect.getX() + mainPageButtonRect.getWidth()),
                    (float) (mainPageButtonRect.getY() + mainPageButtonRect.getHeight()));
            gl.glTexCoord2f(0, 1);
            gl.glVertex2f((float) mainPageButtonRect.getX(),
                    (float) (mainPageButtonRect.getY() + mainPageButtonRect.getHeight()));
            gl.glEnd();
            gl.glDisable(GL2.GL_TEXTURE_2D);
        }

        gl.glDisable(GL2.GL_BLEND);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Release textures
        if (backgroundTexture != null)
            backgroundTexture.destroy(drawable.getGL());
        if (level1Texture != null)
            level1Texture.destroy(drawable.getGL());
        if (level2Texture != null)
            level2Texture.destroy(drawable.getGL());
        if (level3Texture != null)
            level3Texture.destroy(drawable.getGL());
        if (mainPageTexture != null)
            mainPageTexture.destroy(drawable.getGL());
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

        // Recalculate button positions
        int buttonWidth = 150;
        int buttonHeight = 50;
        int spacing = 20;
        int totalWidth = (buttonWidth + spacing) * 3 - spacing;
        int startX = (width - totalWidth) / 2;
        int yPosition = height / 2;

        levelButtons.clear();
        for (int i = 0; i < 3; i++) {
            int xPosition = startX + i * (buttonWidth + spacing);
            levelButtons.add(new Rectangle2D.Float(xPosition, yPosition, buttonWidth, buttonHeight));
        }

        // Update main page button
        int mainPageButtonWidth = 100;
        int mainPageButtonHeight = 60;
        int mainPageButtonX = (width - mainPageButtonWidth) / 2;
        int mainPageButtonY = yPosition + buttonHeight + 60;
        mainPageButtonRect.setRect(mainPageButtonX, mainPageButtonY, mainPageButtonWidth, mainPageButtonHeight);
    }
}