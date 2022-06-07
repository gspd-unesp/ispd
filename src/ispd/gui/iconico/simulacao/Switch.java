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
 * Switch.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 14-Out-2014 : Version 2.0.1;
 *
 */
package ispd.gui.iconico.simulacao;

import ispd.gui.iconico.Vertice;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author denison
 */
public class Switch extends Vertice {

    private final int TAMANHO = 50;
    private CS_Switch icone;
    
    public Switch(int coluna, int linha, CS_Switch icone) {
        super(linha, coluna);
        this.icone = icone;
    }
    
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        int[] swt_x = {getX() - 25, getX() - 14, getX() + 25, getX() + 25, getX() + 15};
        int[] swt_y = {getY(), getY() - 25, getY() - 25, getY(), getY() + 25};
        g.setColor(Color.BLUE);
        g.fillPolygon(swt_x, swt_y, 5);
        g.fillRect(getX() - 25, getY(), TAMANHO - 10, TAMANHO / 2);
        g.setColor(Color.BLACK);
        g.drawRect(getX() - 25, getY(), TAMANHO - 10, TAMANHO / 2);
        g.setColor(Color.GREEN);
        g.drawString("→", getX() - 1, getY() - 13);
        g.drawString("←", getX() - 7, getY() - 7);
        Integer num = icone.getCargaTarefas();
        if (num > 0) {
            g.drawString(num.toString(), getX() - 21, getY() + 12);
        } else {
            g.drawString("▪▪▪▪", getX() - 21, getY() + 12);
        }
        g.drawString("▪▪▪▪", getX() - 21, getY() + 22);
    }

    @Override
    public boolean contains(int x, int y) {
        if (x < getX() + 25 && x > getX() - 25) {
            if (y < getY() + 25 && y > getY() - 25) {
                return true;
            }
        }
        return false;
    }
}
