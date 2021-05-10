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
 * Maquina.java
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
import ispd.motor.filas.servidores.CS_Processamento;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author denison
 */
public class Maquina extends Vertice {

    private final int TAMANHO = 50;
    private CS_Processamento maq;

    public Maquina(int coluna, int linha, CS_Processamento icone) {
        super(linha, coluna);
        maq = icone;
    }

    @Override
    public void draw(Graphics g) {
        String name = maq.getId();
        if (maq.getnumeroMaquina() > 0) {
            name += "-" + maq.getnumeroMaquina();
        }
        int[] pc_x = {getX() - 23, getX() + 23, getX() + 23, getX() + 19, getX() + 19, getX() + 23, getX() - 23, getX() - 19, getX() - 19, getX() - 23};
        int[] pc_y = {getY() - 23, getY() - 23, getY() + 15, getY() + 15, getY() + 17, getY() + 23, getY() + 23, getY() + 17, getY() + 15, getY() + 15};
        g.setColor(Color.GRAY);
        g.fillPolygon(pc_x, pc_y, 10);
        //Desenha tarefas
        Integer num = maq.getCargaTarefas();
        if (num > 0) {
            g.setColor(Color.BLUE);
            g.fillRect(getX() - 17, getY() - 17, TAMANHO - 16, TAMANHO / 2);
            g.setColor(Color.WHITE);
            g.drawString(num.toString(), getX() - 17, getY() - 07);
        } else if(num < 0){
            g.setColor(Color.RED);
            g.fillRect(getX() - 17, getY() - 17, TAMANHO - 16, TAMANHO / 2);
            g.setColor(Color.WHITE);
            g.drawString("Erro!", getX() - 17, getY() - 07);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(getX() - 17, getY() - 17, TAMANHO - 16, TAMANHO / 2);
        }
        g.setColor(Color.BLACK);
        g.drawString(name, getX() - 25, getY() - 35);
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
