package com.zutubi.pulse.charting.demo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ScrollPaneWatermark extends JViewport
{
    BufferedImage fgimage, bgimage;

    TexturePaint texture;

    public ScrollPaneWatermark()
    {
        super();
        // setOpaque(false);
    }

    public void setBackgroundTexture(URL url) throws IOException
    {
        bgimage = ImageIO.read(url);
        Rectangle rect = new Rectangle(0, 0, bgimage.getWidth(null), bgimage.getHeight(null));
        texture = new TexturePaint(bgimage, rect);
    }

    public void setForegroundBadge(URL url) throws IOException
    {
        fgimage = ImageIO.read(url);
    }

    public void paintComponent(Graphics g)
    {
        // do the superclass behavior first
        super.paintComponent(g);

        // paint the texture
        if (texture != null)
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(texture);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void paintChildren(Graphics g)
    {
        super.paintChildren(g);
        if (fgimage != null)
        {
            g.drawImage(fgimage, getWidth() - fgimage.getWidth(null), 0, null);
        }
    }

    public void setView(JComponent view)
    {
        view.setOpaque(false);
        super.setView(view);
    }

    public static void main(String[] args) throws Exception
    {
        JFrame frame = new JFrame();

        JTextArea ta = new JTextArea();
        for (int i = 0; i < 1000; i++)
        {
            ta.append(Integer.toString(i) + "  ");
        }

        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        // ta.setOpaque(false);

        ScrollPaneWatermark watermark = new ScrollPaneWatermark();
        watermark.setBackgroundTexture(new File("C:\\projects\\pulse\\trunk\\master\\src\\www\\images\\sample.png").toURI().toURL());
        watermark.setView(ta);

        JScrollPane scroll = new JScrollPane();
        scroll.setViewport(watermark);

        frame.getContentPane().add(scroll);
        frame.pack();
        frame.setSize(600, 600);
        frame.setVisible(true);
    }

}
