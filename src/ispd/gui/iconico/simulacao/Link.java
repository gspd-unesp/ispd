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
 * Link.java
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

import ispd.gui.iconico.Aresta;
import ispd.gui.iconico.Vertice;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author denison
 */
public class Link extends Aresta {

    public CS_Link link;
    private DesenhoSimulacao aDesenho;

    public Link(Vertice origem, Vertice destino, CS_Link link, DesenhoSimulacao aDesenho) {
        super(origem, destino);
        this.aDesenho = aDesenho;
        this.link = link;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void setSelected(boolean selected) {
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        //g.drawLine(getOrigem().getX() + TAMANHO / 2 + 2, getOrigem().getY() + TAMANHO / 2 + 2, getDestino().getX() + TAMANHO / 2 - 2, getDestino().getY() + TAMANHO / 2 - 2);
        g.drawLine(getOrigem().getX(), getOrigem().getY(), getDestino().getX(), getDestino().getY());
        //Desenha tarefas
        if (link != null) {
            int num = link.getCargaTarefas();
            if (num > 0) {
                String seta;
                if (getOrigem().getX() < getDestino().getX()) {
                    seta = "▶";
                } else if (getOrigem().getX() > getDestino().getX()) {
                    seta = "◀";
                } else if (getOrigem().getY() < getDestino().getY()) {
                    seta = "▼";
                } else {
                    seta = "▲";
                }
                //Movimento
                int incX = (getDestino().getX() - getOrigem().getX()) / aDesenho.getSetas();
                int incY = (getDestino().getY() - getOrigem().getY()) / aDesenho.getSetas();
                g.drawString(seta, getOrigem().getX() + incX, getOrigem().getY() + incY    );
                g.drawString(seta, getOrigem().getX() + incX * 2, getOrigem().getY() + incY * 2);
                g.drawString(seta, getOrigem().getX() + incX * 3, getOrigem().getY() + incY * 3);
                g.drawString(seta, getOrigem().getX() + incX * 4, getOrigem().getY() + incY * 4);
                //Tarefas
                g.setColor(Color.YELLOW);
                g.fillRect(getX(), getY(), 30, 30);
                g.setColor(Color.BLACK);
                g.drawString(num + seta, getX(), getY() + 20);
            }
        }
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }
}