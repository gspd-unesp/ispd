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
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JWindow;

/**
 *
 * Janela de carregamento do iSPD, chamada durante a inicialização do programa
 *
 * @author denison
 */
public class SplashWindow extends JWindow {

    private static final int ALTURA = 20;
    private static final int LARGURA = 40;
    private Point posicaoTexto;
    private String text = "";
    private BufferedImage splash;
    private ImageIcon imagem;

    public SplashWindow(ImageIcon image) {
        int width = image.getIconWidth() + LARGURA * 2;
        int height = image.getIconHeight() + ALTURA * 2;
        posicaoTexto = new Point(40, width - 50);
        text = "";
        setSize(new Dimension(width, height));

        setLocationRelativeTo(null);
        Rectangle windowRect = getBounds();
        imagem = image;
        splash = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        try {
            Graphics2D g2 = (Graphics2D) splash.getGraphics();
            Robot robot = new Robot(
                    getGraphicsConfiguration().getDevice());
            //captura imagem do fundo
            BufferedImage capture = robot.createScreenCapture(
                    new Rectangle(windowRect.x, windowRect.y,
                    windowRect.width,
                    windowRect.height));
            g2.drawImage(capture, 0, 0, null);
            //Desenha retangulo transparente
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(6, 6, width - 6, height - 6, 12, 12);
            g2.dispose();
        } catch (AWTException ex) {
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics offgc;
        Dimension d = getSize();
        // create the offscreen buffer and associated Graphics
        Image offscreen = createImage(d.width, d.height);
        offgc = offscreen.getGraphics();
        // do normal redraw
        offgc.drawImage(splash, 0, 0, null);
        imagem.paintIcon(this, offgc, LARGURA, ALTURA);
        offgc.drawString(text, posicaoTexto.x, posicaoTexto.y);
        // transfer offscreen to window
        g.drawImage(offscreen, 0, 0, this);
    }

    public void setText(String text) {
        this.text = text;
    }
}