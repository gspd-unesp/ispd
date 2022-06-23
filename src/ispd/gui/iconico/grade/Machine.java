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
 * Machine.java
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author denison
 */
public class Machine extends Vertex implements GridItem {

    private GridItemIdentifier id;
    private HashSet<GridItem> conexoesEntrada;
    private HashSet<GridItem> conexoesSaida;
    private String algoritmo;
    private Double poderComputacional;
    private Integer nucleosProcessador;
    private Double ocupacao;
    private Boolean mestre;
    private Double memoriaRAM;
    private Double discoRigido;
    private boolean configurado;
    private String proprietario;
    private List<GridItem> escravos;
    private Double costperprocessing;
    private Double costpermemory; 
    private Double costperdisk;
    private String VMMallocpolicy;

    

    

    


    
    private Double consumoEnergia;

    public Machine(int x, int y, int idLocal, int idGlobal, Double energia) {
        super(x, y);
        this.id = new GridItemIdentifier(idLocal, idGlobal, "mac" + idGlobal);
        this.escravos = new ArrayList<GridItem>();
        this.proprietario = "user1";
        this.algoritmo = "---";
        this.poderComputacional = 0.0;
        this.nucleosProcessador = 1;
        this.ocupacao = 0.0;
        this.memoriaRAM = 0.0;
        this.discoRigido = 0.0;
        this.mestre = false;
        this.costperprocessing=0.0;
        this.costpermemory = 0.0;
        this.costperdisk = 0.0;
        this.VMMallocpolicy = "---";
        conexoesEntrada = new HashSet<GridItem>();
        conexoesSaida = new HashSet<GridItem>();
        this.consumoEnergia = energia;
    }

    @Override
    public GridItemIdentifier getId() {
        return this.id;
    }
    
    public Double getConsumoEnergia(){
        return this.consumoEnergia;
    }
    
    public void setConsumoEnergia( Double energia ){
        this.consumoEnergia = energia;
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
    public String toString() {
        return "id: " + getId().getGlobalId() + " " + getId().getName();
    }

    @Override
    public String getAttributes(ResourceBundle palavras) {
        String texto = palavras.getString("Local ID:") + " " + String.valueOf(getId().getLocalId())
                + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(getId().getGlobalId())
                + "<br>" + palavras.getString("Label") + ": " + getId().getName()
                + "<br>" + palavras.getString("X-coordinate:") + " " + String.valueOf(getX())
                + "<br>" + palavras.getString("Y-coordinate:") + " " + String.valueOf(getY())
                + "<br>" + palavras.getString("Computing power") + ": " + String.valueOf(getPoderComputacional())
                + "<br>" + palavras.getString("Load Factor") + ": " + String.valueOf(getTaxaOcupacao());
        if (isMestre()) {
            texto = texto
                    + "<br>" + palavras.getString("Master")
                    + "<br>" + palavras.getString("Scheduling algorithm") + ": " + getAlgoritmo();
        } else {
            texto = texto
                    + "<br>" + palavras.getString("Slave");
        }
        return texto;
    }

    @Override
    public Machine makeCopy(int mousePosX, int mousePosY, int globalId, int localId) {
        Machine temp = new Machine(mousePosX, mousePosY, globalId, localId, this.consumoEnergia);
        temp.algoritmo = this.algoritmo;
        //temp.VMMallocpolicy = this.VMMallocpolicy;
        temp.poderComputacional = this.poderComputacional;
        temp.ocupacao = this.ocupacao;
        temp.mestre = this.mestre;
        temp.proprietario = this.proprietario;
        temp.verificaConfiguracao();
        return temp;
    }

    @Override
    public boolean isConfigured() {
        return configurado;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(DesenhoGrade.IMACHINE, getX() - 15, getY() - 15, null);
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

    public void setMestre(Boolean mestre) {
        this.mestre = mestre;
        verificaConfiguracao();
    }

    public Boolean isMestre() {
        return mestre;
    }

    public List<GridItem> getEscravos() {
        return escravos;
    }

    public void setEscravos(List<GridItem> escravos) {
        this.escravos = escravos;
    }

    public List<GridItem> getNosEscalonaveis() {
        List<GridItem> escalonaveis = new ArrayList<GridItem>();
        Set internet = new HashSet();
        for (GridItem link : conexoesSaida) {
            GridItem itemGrade = (GridItem) ((Link) link).getDestination();
            if (itemGrade instanceof Cluster || itemGrade instanceof Machine) {
                if (!escalonaveis.contains(itemGrade)) {
                    escalonaveis.add(itemGrade);
                }
            } else if (itemGrade instanceof Internet) {
                internet.add(itemGrade);
                getIndiretosEscalonaveis(itemGrade, escalonaveis, internet);
            }
        }
        escalonaveis.remove(this);
        return escalonaveis;
    }

    private void getIndiretosEscalonaveis(GridItem itemGrade, List<GridItem> escalonaveis, Set internet) {
        for (GridItem link : itemGrade.getOutboundConnections()) {
            GridItem item = (GridItem) ((Link) link).getDestination();
            if (item instanceof Cluster || item instanceof Machine) {
                if (!escalonaveis.contains(item)) {
                    escalonaveis.add(item);
                }
            } else if (item instanceof Internet) {
                if (!internet.contains(item)) {
                    internet.add(item);
                    getIndiretosEscalonaveis(item, escalonaveis, internet);
                }
            }
        }
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public void setAlgoritmo(String algoritmo) {
        this.algoritmo = algoritmo;
        verificaConfiguracao();
    }

    public Double getPoderComputacional() {
        return poderComputacional;
    }

    public void setPoderComputacional(double poderComputacional) {
        this.poderComputacional = poderComputacional;
        verificaConfiguracao();
    }

    public String getProprietario() {
        return proprietario;
    }

    public void setProprietario(String proprietario) {
        this.proprietario = proprietario;
    }
    
    public Double getTaxaOcupacao() {
        return ocupacao;
    }

    public void setTaxaOcupacao(Double ocupacao) {
        this.ocupacao = ocupacao;
    }

    public Integer getNucleosProcessador() {
        return nucleosProcessador;
    }

    public void setNucleosProcessador(Integer nucleosProcessador) {
        this.nucleosProcessador = nucleosProcessador;
    }

    public Double getMemoriaRAM() {
        return memoriaRAM;
    }

    public void setMemoriaRAM(Double memoriaRAM) {
        this.memoriaRAM = memoriaRAM;
    }

    public Double getDiscoRigido() {
        return discoRigido;
    }

    public void setDiscoRigido(Double discoRigido) {
        this.discoRigido = discoRigido;
    }
    
    public Double getCostperprocessing() {
        return costperprocessing;
    }


    public void setCostperprocessing(Double costperprocessing) {
        this.costperprocessing = costperprocessing;
    }
    
    public Double getCostpermemory() {
        return costpermemory;
    }

    public void setCostpermemory(Double costpermemory) {
        this.costpermemory = costpermemory;
    }
    
    public Double getCostperdisk() {
        return costperdisk;
    }

    public void setCostperdisk(Double costperdisk) {
        this.costperdisk = costperdisk;
    }
    
    public String getVMMallocpolicy() {
        return VMMallocpolicy;
    }

    public void setVMMallocpolicy(String VMMallocpolicy) {
        this.VMMallocpolicy = VMMallocpolicy;
        verificaConfiguracao();
    }
    
   

    private void verificaConfiguracao() {
        if (poderComputacional > 0) {
            configurado = true;
            if (mestre && ("---".equals(algoritmo) || "---".equals(VMMallocpolicy))) {
                configurado = false;
            }
        } else {
            configurado = false;
        }
    }

    protected Set<GridItem> getNosIndiretosSaida() {
        Set<GridItem> indiretosSaida = new HashSet<GridItem>();
        for (GridItem link : conexoesSaida) {
            GridItem itemGrade = (GridItem) ((Link) link).getDestination();
            if (itemGrade instanceof Cluster || itemGrade instanceof Machine) {
                indiretosSaida.add(itemGrade);
            } else if (itemGrade instanceof Internet) {
                indiretosSaida.add(itemGrade);
                getIndiretosSaida(itemGrade, indiretosSaida);
            }
        }
        return indiretosSaida;
    }

    private void getIndiretosSaida(GridItem internet, Set<GridItem> indiretosSaida) {
        for (GridItem link : internet.getOutboundConnections()) {
            GridItem item = (GridItem) ((Link) link).getDestination();
            if (item instanceof Cluster || item instanceof Machine) {
                indiretosSaida.add(item);
            } else if (item instanceof Internet) {
                if (!indiretosSaida.contains(item)) {
                    indiretosSaida.add(item);
                    getIndiretosSaida(item, indiretosSaida);
                }
            }
        }
    }

    protected Set<GridItem> getNosIndiretosEntrada() {
        Set<GridItem> indiretosEntrada = new HashSet<GridItem>();
        for (GridItem link : conexoesEntrada) {
            GridItem itemGrade = (GridItem) ((Link) link).getSource();
            if (itemGrade instanceof Cluster || itemGrade instanceof Machine) {
                indiretosEntrada.add(itemGrade);
            } else if (itemGrade instanceof Internet) {
                indiretosEntrada.add(itemGrade);
                getIndiretosEntrada(itemGrade, indiretosEntrada);
            }
        }
        return indiretosEntrada;
    }

    private void getIndiretosEntrada(GridItem internet, Set<GridItem> indiretosEntrada) {
        for (GridItem link : internet.getInboundConnections()) {
            GridItem item = (GridItem) ((Link) link).getSource();
            if (item instanceof Cluster || item instanceof Machine) {
                indiretosEntrada.add(item);
            } else if (item instanceof Internet) {
                if (!indiretosEntrada.contains(item)) {
                    indiretosEntrada.add(item);
                    getIndiretosSaida(item, indiretosEntrada);
                }
            }
        }
    }
}
