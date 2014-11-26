package org.opencv.face.video.swing;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.opencv.core.Mat;
import org.opencv.face.video.ImageConverter;

/**
 *
 * @author spindizzy
 */
public class VideoPanel extends Canvas {

    private BufferedImage image;
    
    private Image offscreenImage;

    public VideoPanel() {
        setForeground(Color.white);
        setBackground(Color.black);
    }

    /**
     * Converts/writes a Mat into a BufferedImage.
     *
     * @param matrix Mat of type CV_8UC3 or CV_8UC1
     */
    public void updateImage(Mat matrix) {
        image = ImageConverter.toBufferedImage(matrix);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
       if (image == null) {
            g.drawString("Camera ...", 10, 10);
            return;
        }
       g.drawImage(this.image, 10, 10, this.image.getWidth(), this.image.getHeight(), null);
    }
    
    

    @Override
    public void update(Graphics g) {
         Rectangle box = g.getClipBounds();
        // create the offscreenImage buffer and associated Graphics
        if(offscreenImage == null){
            offscreenImage = createImage(box.width, box.height);
        }
        Graphics offscreenGraphics = offscreenImage.getGraphics();
        offscreenGraphics.setColor(getBackground());
        offscreenGraphics.fillRect(0, 0, box.width, box.height);
        offscreenGraphics.setColor(getForeground());
        offscreenGraphics.translate(-box.x, -box.y);
        // do normal redraw
        paint(offscreenGraphics);
        // transfer offscreenImage to window
        g.drawImage(offscreenImage, box.x, box.y, this);
    }


}
