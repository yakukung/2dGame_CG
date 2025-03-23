package Game;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import Game.MazeLV_1;
import Game.MazeLV_2;
import Game.MazeLV_3;

public class LevelSelection implements GLEventListener {
    private TextRenderer textRenderer;
    private List<Rectangle2D> levelButtons;
    private int selectedLevel = -1;

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));

        levelButtons = new ArrayList<>();
        int buttonWidth = 200;
        int buttonHeight = 50;
        int spacing = 20;
        int totalWidth = (buttonWidth + spacing) * 3 - spacing;
        int startX = (drawable.getSurfaceWidth() - totalWidth) / 2;
        int yPosition = drawable.getSurfaceHeight() / 2;

        for (int i = 0; i < 3; i++) {
            int xPosition = startX + i * (buttonWidth + spacing);
            levelButtons.add(new Rectangle2D.Float(xPosition, yPosition, buttonWidth, buttonHeight));
        }

        Randers.getWindow().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = Randers.getWindow().getHeight() - e.getY();

                for (int i = 0; i < levelButtons.size(); i++) {
                    if (levelButtons.get(i).contains(mouseX, mouseY)) {
                        selectedLevel = i + 1;
                        System.out.println("Selected Level: " + selectedLevel);
                        switch (selectedLevel) {
                            case 1:
                                Randers.setGLEventListener(new Robot(new MazeLV_1()));
                                break;
                            case 2:
                                Randers.setGLEventListener(new Robot(new MazeLV_2()));
                                break;
                            case 3:
                                Randers.setGLEventListener(new Robot(new MazeLV_3()));
                                break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        for (int i = 0; i < levelButtons.size(); i++) {
            Rectangle2D button = levelButtons.get(i);
            String levelText = "Level " + (i + 1);
            Rectangle2D textBounds = textRenderer.getBounds(levelText);
            int textX = (int) (button.getX() + (button.getWidth() - textBounds.getWidth()) / 2);
            int textY = (int) (button.getY() + (button.getHeight() - textBounds.getHeight()) / 2);
            textRenderer.draw(levelText, textX, textY);
        }

        textRenderer.endRendering();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
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

        // Recalculate button positions to keep them centered
        int buttonWidth = 200;
        int buttonHeight = 50;
        int spacing = 20;
        int totalWidth = (buttonWidth + spacing) * 3 - spacing;
        int startX = (width - totalWidth) / 2;
        int yPosition = height / 2;

        for (int i = 0; i < levelButtons.size(); i++) {
            int xPosition = startX + i * (buttonWidth + spacing);
            levelButtons.set(i, new Rectangle2D.Float(xPosition, yPosition, buttonWidth, buttonHeight));
        }
    }
}