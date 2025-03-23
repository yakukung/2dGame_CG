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
        for (int i = 0; i < 5; i++) {
            levelButtons.add(new Rectangle2D.Float(100, 400 - i * 60, 200, 50));
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
                        if (selectedLevel == 1) {
                            Randers.setGLEventListener(new GameScreen()); // Ensure this line is executed
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
        textRenderer.draw("Select a Level", 100, drawable.getSurfaceHeight() - 50);

        for (int i = 0; i < levelButtons.size(); i++) {
            Rectangle2D button = levelButtons.get(i);
            textRenderer.draw("" + (i + 1), (int) button.getX() + 10, (int) button.getY() + 30);
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
    }
}