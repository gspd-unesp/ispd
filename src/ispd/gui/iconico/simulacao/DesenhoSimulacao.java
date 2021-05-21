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
 * DesenhoSimulacao.java
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

import ispd.gui.iconico.AreaDesenho;
import ispd.gui.iconico.Icone;
import ispd.gui.iconico.Vertice;
import ispd.motor.SimulacaoGrafica;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 *
 * @author denison
 */
public class DesenhoSimulacao extends AreaDesenho {

    private final int INCREMENTO = 100;
    private Font fonte;
    private SimulacaoGrafica sim;
    private int setas = 5;
    private int setaCont = 0;

    public DesenhoSimulacao(SimulacaoGrafica sim) {
        super(false, false, true, true);
        this.setUnits(INCREMENTO);
        this.sim = sim;
        this.fonte = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        HashMap<Object, Vertice> posicoes = new HashMap<Object, Vertice>();
        int coluna = INCREMENTO;
        int linha = INCREMENTO;
        int pos_coluna = 0;
        int num_coluna = ((int) Math.sqrt(
                sim.getRedeDeFilas().getMaquinas().size()
                + sim.getRedeDeFilas().getMestres().size()
                + sim.getRedeDeFilas().getInternets().size())) + 1;
        for (CS_Maquina icone : sim.getRedeDeFilas().getMaquinas()) {
            Maquina maq = new Maquina(coluna, linha, icone);
            vertices.add(maq);
            posicoes.put(icone, maq);
            coluna += INCREMENTO;
            pos_coluna++;
            if (pos_coluna == num_coluna) {
                pos_coluna = 0;
                coluna = INCREMENTO;
                linha += INCREMENTO;
            }
        }
        for (CS_Processamento icone : sim.getRedeDeFilas().getMestres()) {
            Maquina maq = new Maquina(coluna, linha, icone);
            vertices.add(maq);
            posicoes.put(icone, maq);
            coluna += INCREMENTO;
            pos_coluna++;
            if (pos_coluna == num_coluna) {
                pos_coluna = 0;
                coluna = INCREMENTO;
                linha += INCREMENTO;
            }
        }
        for (CS_Internet icone : sim.getRedeDeFilas().getInternets()) {
            Roteador rot = new Roteador(coluna, linha, icone);
            vertices.add(rot);
            posicoes.put(icone, rot);
            coluna += INCREMENTO;
            pos_coluna++;
            if (pos_coluna == num_coluna) {
                pos_coluna = 0;
                coluna = INCREMENTO;
                linha += INCREMENTO;
            }
        }
        for (CS_Comunicacao icone : sim.getRedeDeFilas().getLinks()) {
            if (!(icone instanceof CS_Link)) {
                Switch sw = new Switch(coluna, linha, (CS_Switch) icone);
                vertices.add(sw);
                posicoes.put(icone, sw);
                coluna += INCREMENTO;
                pos_coluna++;
                if (pos_coluna == num_coluna) {
                    pos_coluna = 0;
                    coluna = INCREMENTO;
                    linha += INCREMENTO;
                }
            }
        }
        for (CS_Comunicacao cs_link : sim.getRedeDeFilas().getLinks()) {
            if (cs_link instanceof CS_Link) {
                CS_Link link = (CS_Link) cs_link;
                Link lk = new Link(posicoes.get(link.getConexoesEntrada()), posicoes.get(link.getConexoesSaida()), link, this);
                arestas.add(lk);
            } else {
                Vertice origm = posicoes.get(cs_link);
                for (CentroServico destino : ((CS_Switch) cs_link).getConexoesSaida()) {
                    Vertice destn = posicoes.get(destino);
                    if (destn != null) {
                        Link lk = new Link(origm, destn, null, this);
                        arestas.add(lk);
                    }
                }
            }
        }
        this.setPreferredSize(new Dimension(num_coluna * INCREMENTO + INCREMENTO, linha + INCREMENTO));
    }

    public Font getFonte() {
        return fonte;
    }

    @Override
    protected void paintComponent(Graphics g) {
        //Animação das setas transmitindo dados nas conexões
        if (setaCont == 100 && setas == 5) {
            setas = 8;
            setaCont = 0;
        } else if (setaCont == 100) {
            setas = 5;
            setaCont = 0;
        }
        setaCont++;
        g.setFont(fonte);
        super.paintComponent(g);
        if (!sim.isParar()) {
            this.repaint();
        }
    }

    public int getSetas() {
        return setas;
    }

    @Override
    public void adicionarVertice(int x, int y) {
    }

    @Override
    public void adicionarAresta(Vertice verticeOrigem, Vertice verticeDestino) {
    }

    @Override
    public void showActionIcon(MouseEvent me, Icone icon) {
    }

    @Override
    public void botaoPainelActionPerformed(ActionEvent evt) {
    }

    @Override
    public void botaoVerticeActionPerformed(ActionEvent evt) {
    }

    @Override
    public void botaoArestaActionPerformed(ActionEvent evt) {
    }

    @Override
    public void botaoIconeActionPerformed(ActionEvent evt) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent me) {
        repaint();
    }

    @Override
    public void showSelectionIcon(MouseEvent me, Icone icon) {}
}
