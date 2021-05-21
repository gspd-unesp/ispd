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
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.gui.iconico.grade;

import ispd.gui.iconico.Aresta;
import ispd.gui.iconico.Vertice;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author denison
 */
public class Link extends Aresta implements ItemGrade {

    private IdentificadorItemGrade id;
    private boolean selected;
    private Polygon areaSeta;
    private static Color DARK_GREEN = new Color(0, 130, 0);
    private double banda;
    private double ocupacao;
    private double latencia;
    private boolean configurado;

    public Link(Vertice origem, Vertice destino, int idLocal, int idGlobal) {
        super(origem, destino);
        this.selected = true;
        this.areaSeta = new Polygon();
        this.id = new IdentificadorItemGrade(idLocal, idGlobal, "link" + idGlobal);
    }

    @Override
    public IdentificadorItemGrade getId() {
        return this.id;
    }

    @Override
    public Set<ItemGrade> getConexoesEntrada() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<ItemGrade> getConexoesSaida() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAtributos(ResourceBundle palavras) {
        String texto = palavras.getString("Local ID:") + " " + getId().getIdLocal()
                + "<br>" + palavras.getString("Global ID:") + " " + getId().getIdGlobal()
                + "<br>" + palavras.getString("Label") + ": " + getId().getNome()
                + "<br>" + palavras.getString("X1-coordinate:") + " " + getOrigem().getX()
                + "<br>" + palavras.getString("Y1-coordinate:") + " " + getOrigem().getY()
                + "<br>" + palavras.getString("X2-coordinate:") + " " + getDestino().getY()
                + "<br>" + palavras.getString("Y2-coordinate:") + " " + getDestino().getX()
                + "<br>" + palavras.getString("Bandwidth") + ": " + getBanda()
                + "<br>" + palavras.getString("Latency") + ": " + getLatencia()
                + "<br>" + palavras.getString("Load Factor") + ": " + getTaxaOcupacao();
        return texto;
    }

    /**
     *
     * @param posicaoMouseX the value of posicaoMouseX
     * @param posicaoMouseY the value of posicaoMouseY
     * @param idGlobal the value of idGlobal
     * @param idLocal the value of idLocal
     */
    @Override
    public Link criarCopia(int posicaoMouseX, int posicaoMouseY, int idGlobal, int idLocal) {
        Link temp = new Link(null, null, idGlobal, idLocal);
        temp.banda = this.banda;
        temp.latencia = this.latencia;
        temp.ocupacao = this.ocupacao;
        temp.verificaConfiguracao();
        return temp;
    }

    @Override
    public boolean isConfigurado() {
        return configurado;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void draw(Graphics g) {
        double arrowWidth = 11.0f;
        double theta = 0.423f;
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        double[] vecLine = new double[2];
        double[] vecLeft = new double[2];
        double fLength;
        double th;
        double ta;
        double baseX, baseY;

        xPoints[0] = (int) getX();
        yPoints[0] = (int) getY();

        // build the line vector
        vecLine[0] = (double) xPoints[ 0] - getOrigem().getX();
        vecLine[1] = (double) yPoints[ 0] - getOrigem().getY();

        // build the arrow base vector - normal to the line
        vecLeft[0] = -vecLine[1];
        vecLeft[1] = vecLine[0];

        // setup length parameters
        fLength = (double) Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
        th = arrowWidth / (2.0f * fLength);
        ta = arrowWidth / (2.0f * ((double) Math.tan(theta) / 2.0f) * fLength);

        // find the base of the arrow
        baseX = ((double) xPoints[ 0] - ta * vecLine[0]);
        baseY = ((double) yPoints[ 0] - ta * vecLine[1]);

        // build the points on the sides of the arrow
        xPoints[1] = (int) (baseX + th * vecLeft[0]);
        yPoints[1] = (int) (baseY + th * vecLeft[1]);
        xPoints[2] = (int) (baseX - th * vecLeft[0]);
        yPoints[2] = (int) (baseY - th * vecLeft[1]);

        areaSeta.reset();
        areaSeta.addPoint(xPoints[0], yPoints[0]);
        areaSeta.addPoint(xPoints[1], yPoints[1]);
        areaSeta.addPoint(xPoints[2], yPoints[2]);

        if (isSelected()) {
            g.setColor(Color.BLACK);
        } else if (isConfigurado()) {
            g.setColor(DARK_GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.drawLine(getOrigem().getX(), getOrigem().getY(), getDestino().getX(), getDestino().getY());
        g.fillPolygon(areaSeta);
    }

    @Override
    public boolean contains(int x, int y) {
        return areaSeta.contains(x, y);
    }

    public double getBanda() {
        return banda;
    }

    public double getTaxaOcupacao() {
        return ocupacao;
    }

    public double getLatencia() {
        return latencia;
    }

    public void setBanda(double banda) {
        this.banda = banda;
        verificaConfiguracao();
    }

    public void setTaxaOcupacao(double taxa) {
        this.ocupacao = taxa;
    }

    public void setLatencia(double latencia) {
        this.latencia = latencia;
        verificaConfiguracao();
    }

    @Override
    public Integer getX() {
        return (((((getOrigem().getX() + getDestino().getX()) / 2) + getDestino().getX()) / 2) + getDestino().getX()) / 2;
    }

    @Override
    public Integer getY() {
        return (((((getOrigem().getY() + getDestino().getY()) / 2) + getDestino().getY()) / 2) + getDestino().getY()) / 2;
    }

    private void verificaConfiguracao() {
        if(banda > 0 && latencia > 0){
            configurado = true;
        } else {
            configurado = false;
        }
    }
}
