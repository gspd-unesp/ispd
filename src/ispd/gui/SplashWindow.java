package ispd.gui;

import ispd.Main;

import javax.swing.ImageIcon;
import javax.swing.JWindow;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.MissingResourceException;

public class SplashWindow extends JWindow {
    private final Point textPosition;
    private final BufferedImage splash;
    private final ImageIcon image = SplashWindow.getImage();
    private final int width = this.image.getIconWidth() + Image.WIDTH * 2;
    private final int height = this.image.getIconHeight() + Image.HEIGHT * 2;

    public SplashWindow() {
        this.textPosition = new Point(
                Text.X_OFFSET, this.width - Text.Y_OFFSET);
        this.splash = new BufferedImage(
                this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        this.setLocationRelativeTo(null);
        this.setSize(new Dimension(this.width, this.height));
        this.drawSomeThings();
        this.setVisible(true);
    }

    private void drawSomeThings() {
        try {
            final var g = (Graphics2D) this.splash.getGraphics();
            this.captureAndDrawDesktopBackground(g);
            SplashWindow.drawOverlay(g, this.width, this.height);
            g.dispose();
        } catch (final AWTException ignored) {
        }
    }

    private void captureAndDrawDesktopBackground(final Graphics g) throws AWTException {
        final var robot =
                new Robot(this.getGraphicsConfiguration().getDevice());
        final var area = this.getBounds();
        final BufferedImage capture = robot.createScreenCapture(
                new Rectangle(area.x, area.y, area.width, area.height));
        
        g.drawImage(capture, 0, 0, null);
    }

    private static void drawOverlay(
            final Graphics g, final int width, final int height) {
        g.setColor(new Color(0, 0, 0, Overlay.ALPHA));
        g.fillRoundRect(
                Overlay.BORDER_SIZE,
                Overlay.BORDER_SIZE,
                width - Overlay.BORDER_SIZE,
                height - Overlay.BORDER_SIZE,
                
                Overlay.ARC_SIZE,
                Overlay.ARC_SIZE
        );
    }

    private static ImageIcon getImage() {
        final var imageUrl =
                Main.class.getResource(Image.PATH);

        if (imageUrl != null)
            return new ImageIcon(imageUrl);

        
        throw new MissingResourceException(
                "Missing .gif for splash window",
                URL.class.getName(),
                Image.PATH
        );
    }

    @Override
    public void paint(final Graphics g) {
        final var dim = this.getSize();
        // Create the offscreen buffer and associated Graphics
        final var offscreen = this.createImage(dim.width, dim.height);
        final var offscreenGraphics = offscreen.getGraphics();
        // Do normal redraw
        offscreenGraphics.drawImage(this.splash, 0, 0, null);
        this.image.paintIcon(this, offscreenGraphics,
                Image.WIDTH, Image.HEIGHT);
        offscreenGraphics.drawString(
                Text.CONTENT,
                this.textPosition.x,
                this.textPosition.y
        );
        // Transfer offscreen to window
        g.drawImage(offscreen, 0, 0, this);
    }

    private static class Image {
        private static final int WIDTH = 40;
        private static final int HEIGHT = 20;
        private static final String PATH = "gui/imagens/Splash.gif";
    }

    private static class Text {
        private static final int X_OFFSET = 40;
        private static final int Y_OFFSET = 50;
        
        private static final String CONTENT =
                "Copyright (c) 2010 - 2014 GSPD.  All rights reserved.";
    }

    private static class Overlay {
        private static final int ALPHA = 90;
        private static final int BORDER_SIZE = 6;
        private static final int ARC_SIZE = 12;
    }
}