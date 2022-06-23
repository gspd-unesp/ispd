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
 * Internet.java
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

import ispd.gui.iconico.Vertex;
import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author denison
 */
public class Internet extends Vertex implements GridItem {
    
    private GridItemIdentifier id;
    private HashSet<GridItem> conexoesEntrada;
    private HashSet<GridItem> conexoesSaida;
    private double banda;
    private double ocupacao;
    private double latencia;
    private boolean configurado;

    public Internet(int x, int y, int idLocal, int idGlobal) {
        super(x, y);
        this.id = new GridItemIdentifier(idLocal, idGlobal, "net" + idGlobal);
        this.conexoesEntrada = new HashSet<GridItem>();
        this.conexoesSaida = new HashSet<GridItem>();
    }

    @Override
    public GridItemIdentifier getId() {
        return this.id;
    }
    
    @Override
    public Set<GridItem> getInboundConnections() {
        return conexoesEntrada;
    }

    @Override
    public Set<GridItem> getOutboundConnections() {
        return conexoesSaida;
    }
    
    @Override
    public String getAttributes(ResourceBundle palavras) {
        String texto = palavras.getString("Local ID:") + " " + id.getLocalId()
         + "<br>" + palavras.getString("Global ID:") + " " + id.getGlobalId()
         + "<br>" + palavras.getString("Label") + ": " + id.getName()
         + "<br>" + palavras.getString("X-coordinate:") + " " + getX()
         + "<br>" + palavras.getString("Y-coordinate:") + " " + getY()
         + "<br>" + palavras.getString("Bandwidth") + ": " + getBanda()
         + "<br>" + palavras.getString("Latency") + ": " + getLatencia()
         + "<br>" + palavras.getString("Load Factor") + ": " + getTaxaOcupacao();
         return texto;
    }
    
    /**
     *
     * @param mousePosX the value of posicaoMouseX
     * @param mousePosY the value of posicaoMouseY
     * @param globalId the value of idGlobal
     * @param localId the value of idLocal
     */
    @Override
    public Internet makeCopy(int mousePosX, int mousePosY, int globalId, int localId) {
        Internet temp = new Internet(mousePosX, mousePosY, globalId, localId);
        temp.banda = this.banda;
        temp.ocupacao = this.ocupacao;
        temp.latencia = this.latencia;
        temp.verificaConfiguracao();
        return temp;
    }
    
    @Override
    public boolean isConfigured() {
        return configurado;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(DesenhoGrade.IINTERNET, getX() - 15, getY() - 15, null);
        if (isConfigured()) {
            g.drawImage(DesenhoGrade.IVERDE, getX() + 15, getY() + 15, null);
        } else {
            g.drawImage(DesenhoGrade.IVERMELHO, getX() + 15, getY() + 15, null);
        }

        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(getId().getGlobalId()), getX(), getY() + 30);
        // Se o icone estiver ativo, desenhamos uma margem nele.
        if (isSelected()) {
            g.setColor(Color.RED);
            g.drawRect(getX() - 19, getY() - 17, 37, 34);
        }
    }

    @Override
    public boolean contains(int x, int y) {
        if (x < getX() + 17 && x > getX() - 17) {
            if (y < getY() + 17 && y > getY() - 17) {
                return true;
            }
        }
        return false;
    }

    public double getBanda() {
        return banda;
    }

    public void setBanda(double banda) {
        this.banda = banda;
        verificaConfiguracao();
    }

    public double getTaxaOcupacao() {
        return ocupacao;
    }

    public void setTaxaOcupacao(double ocupacao) {
        this.ocupacao = ocupacao;
    }

    public double getLatencia() {
        return latencia;
    }

    public void setLatencia(double latencia) {
        this.latencia = latencia;
        verificaConfiguracao();
    }
    
    private void verificaConfiguracao() {
        if(banda > 0 && latencia > 0){
            configurado = true;
        } else {
            configurado = false;
        }
    }
}
