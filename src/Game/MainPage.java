package Game;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.GL2; 
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class MainPage implements GLEventListener {
    
    private TextRenderer textRenderer;
    public Rectangle2D startButtonRect;
    public boolean isHovering = false;
    
    private Rectangle2D exitButtonRect;
    private boolean isExitHovering = false;
    
    // สำหรับสีปุ่ม
    private float[] buttonColor = {0.3f, 0.5f, 0.8f};
    private float[] hoverColor = {0.4f, 0.6f, 0.9f};
    
    // เพิ่ม texture พื้นหลัง
    private Texture backgroundTexture;
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        
        // วาดพื้นหลัง
        drawBackground(gl, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        
        // วาดชื่อเกม
        textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        textRenderer.setColor(0.2f, 0.2f, 0.8f, 1.0f);
        
        String gameTitle = "Robot Simulation Game";
        Rectangle2D titleBounds = textRenderer.getBounds(gameTitle);
        int titleX = (int)((drawable.getSurfaceWidth() - titleBounds.getWidth()) / 2);
        int titleY = (int)(drawable.getSurfaceHeight() * 0.7);
        
        textRenderer.draw(gameTitle, titleX, titleY);
        textRenderer.endRendering();
        
        // วาดปุ่มเริ่มเกม
        float buttonWidth = 200;
        float buttonHeight = 50;
        float buttonX = (drawable.getSurfaceWidth() - buttonWidth) / 2;
        float buttonY = drawable.getSurfaceHeight() / 2 - buttonHeight / 2;
        
        startButtonRect = new Rectangle2D.Float(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // เลือกสีปุ่มตามสถานะ hover
        if (isHovering) {
            gl.glColor3f(hoverColor[0], hoverColor[1], hoverColor[2]);
        } else {
            gl.glColor3f(buttonColor[0], buttonColor[1], buttonColor[2]);
        }
        
        // วาดปุ่ม
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(buttonX, buttonY);
        gl.glVertex2f(buttonX + buttonWidth, buttonY);
        gl.glVertex2f(buttonX + buttonWidth, buttonY + buttonHeight);
        gl.glVertex2f(buttonX, buttonY + buttonHeight);
        gl.glEnd();
        
        // วาดข้อความบนปุ่ม
        Font thaiFont = new Font("TH Sarabun new", Font.PLAIN, 24);
        TextRenderer textRenderer = new TextRenderer(thaiFont, true, false);
        
        textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        String buttonText = "เริ่มเกม";
        
        Rectangle2D textBounds = textRenderer.getBounds(buttonText);
        int textX = (int)(buttonX + (buttonWidth - textBounds.getWidth()) / 2);
        int textY = (int)(buttonY + (buttonHeight - textBounds.getHeight()) / 2);
        
        textRenderer.draw(buttonText, textX, textY);
        textRenderer.endRendering();

                      
     // วาดปุ่มออกจากเกม
        float exitButtonWidth = 200;
        float exitButtonHeight = 50;
        float exitButtonX = (drawable.getSurfaceWidth() - exitButtonWidth) / 2;
        float exitButtonY = buttonY - 70;  // ให้ต่ำกว่าปุ่มเริ่มเกม

        exitButtonRect = new Rectangle2D.Float(exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight);

        // เลือกสีปุ่มออกจากเกม
        if (isExitHovering) {
            gl.glColor3f(0.8f, 0.3f, 0.3f);  // สีแดงเข้มเมื่อ hover
        } else {
            gl.glColor3f(0.9f, 0.4f, 0.4f);  // สีแดงปกติ
        }

        // วาดปุ่มออกจากเกม
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(exitButtonX, exitButtonY);
        gl.glVertex2f(exitButtonX + exitButtonWidth, exitButtonY);
        gl.glVertex2f(exitButtonX + exitButtonWidth, exitButtonY + exitButtonHeight);
        gl.glVertex2f(exitButtonX, exitButtonY + exitButtonHeight);
        gl.glEnd();

        // วาดข้อความบนปุ่มออกจากเกม
        textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f); 

        String exitText = "ออกจากเกม";
        Rectangle2D exitTextBounds = textRenderer.getBounds(exitText);
        int exitTextX = (int)(exitButtonX + (exitButtonWidth - exitTextBounds.getWidth()) / 2);
        int exitTextY = (int)(exitButtonY + (exitButtonHeight - exitTextBounds.getHeight()) / 2);

        textRenderer.draw(exitText, exitTextX, exitTextY);
        textRenderer.endRendering();
        
        
        // ทำให้แน่ใจว่าทุกคำสั่งถูกประมวลผล
        gl.glFlush();
    }

    private void drawBackground(GL2 gl, int width, int height) {
        if (backgroundTexture != null) {
            gl.glEnable(GL2.GL_TEXTURE_2D);
            backgroundTexture.bind(gl);
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, 0);
            gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(width, 0);
            gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(width, height);
            gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, height);
            gl.glEnd();
            
            gl.glDisable(GL2.GL_TEXTURE_2D);
        }
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        if (textRenderer != null) {
            textRenderer.dispose();
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));

        try {
            File backgroundFile = new File("D:\\Computer Graphics\\Project2D\\BG.jpg");
            if (backgroundFile.exists()) {
                backgroundTexture = TextureIO.newTexture(backgroundFile, true);
            } else {
                System.err.println("ไม่พบไฟล์รูปภาพพื้นหลัง: " + backgroundFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("ไม่สามารถโหลดรูปภาพพื้นหลังได้: " + e.getMessage());
            e.printStackTrace();
        }

        Randers.getWindow().addMouseListener(new MouseAdapter() {
        	@Override
        	public void mousePressed(MouseEvent e) {
        	    int mouseX = e.getX();
        	    int mouseY = e.getY();  // เปลี่ยนจาก Randers.getWindow().getHeight() - e.getY()



        	    if (startButtonRect != null && startButtonRect.contains(mouseX, mouseY)) {
        	        System.out.println("กดปุ่มเริ่มเกม!");
        	        Randers.setGLEventListener(new Robot());
        	    }
        	    
        	    if (exitButtonRect != null && exitButtonRect.contains(mouseX, drawable.getSurfaceHeight() - mouseY)) {
        	        System.out.println("ออกจากเกม!");
        	        Randers.getWindow().destroy();  // ปิดหน้าต่างของ JOGL
        	        System.exit(0);  // ปิดโปรแกรม
        	    }

        	}

        });
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
