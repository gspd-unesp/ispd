/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * SplashWindow.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JWindow;

/**
 * @author denison
 */
public class SplashWindow extends JWindow
{
    private static final int TRANSPARENT_OVERLAY_ALPHA = 90;
    private static final int TRANSPARENT_OVERLAY_BORDER_SIZE = 6;
    private static final int TRANSPARENT_OVERLAY_CORNER_ROUNDNESS = 12;
    private static final int TEXT_OFFSET_X = 40;
    private static final int TEXT_OFFSET_Y = 50;
    private static final int IMAGE_WIDTH = 40;
    private static final int IMAGE_HEIGHT = 20;
    private final Point textPosition;
    private final BufferedImage splash;
    private final ImageIcon image;
    private String text; // TODO: Make this final

    public SplashWindow (final ImageIcon image)
    {
        // TODO: Fluent interface for drawing this window
        final int width = image.getIconWidth() + IMAGE_WIDTH * 2;
        final int height = image.getIconHeight() + IMAGE_HEIGHT * 2;
        this.textPosition = new Point(TEXT_OFFSET_X, width - TEXT_OFFSET_Y);
        this.text = "";
        this.setSize(new Dimension(width, height));

        this.setLocationRelativeTo(null);
        this.image = image;
        this.splash = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        try
        {
            final var g = (Graphics2D) this.splash.getGraphics();
            this.captureAndDrawDesktopBackground(g);
            this.drawTransparentOverlay(g, width, height);
            g.dispose();
        } catch (AWTException ignored)
        {
        }
    }

    private void captureAndDrawDesktopBackground (final Graphics2D g) throws AWTException
    {
        final var robot = new Robot(this.getGraphicsConfiguration().getDevice());
        final var area = this.getBounds();
        final BufferedImage capture = robot.createScreenCapture(
                new Rectangle(area.x, area.y, area.width, area.height)); // TODO: Possibly inline area
        g.drawImage(capture, 0, 0, null);
    }

    private void drawTransparentOverlay (final Graphics2D g, final int width, final int height)
    {
        g.setColor(new Color(0, 0, 0, TRANSPARENT_OVERLAY_ALPHA));
        g.fillRoundRect(
                TRANSPARENT_OVERLAY_BORDER_SIZE, TRANSPARENT_OVERLAY_BORDER_SIZE,
                width - TRANSPARENT_OVERLAY_BORDER_SIZE, height - TRANSPARENT_OVERLAY_BORDER_SIZE, // TODO: 2 * BORDER_SIZE ?
                TRANSPARENT_OVERLAY_CORNER_ROUNDNESS, TRANSPARENT_OVERLAY_CORNER_ROUNDNESS);
    }

    @Override
    public void paint (final Graphics g)
    {
        final var dim = this.getSize();
        // Create the offscreen buffer and associated Graphics
        final var offscreen = this.createImage(dim.width, dim.height);
        final var offscreenGraphics = offscreen.getGraphics();
        // Do normal redraw
        offscreenGraphics.drawImage(this.splash, 0, 0, null);
        this.image.paintIcon(this, offscreenGraphics, IMAGE_WIDTH, IMAGE_HEIGHT);
        offscreenGraphics.drawString(this.text, this.textPosition.x, this.textPosition.y);
        // Transfer offscreen to window
        g.drawImage(offscreen, 0, 0, this);
    }

    public void setText (final String text)
    {
        this.text = text;
    }
}