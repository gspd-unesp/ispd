/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertice;
import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author denison
 */
public class Internet extends Vertice implements ItemGrade {
    
    private IdentificadorItemGrade id;
    private HashSet<ItemGrade> conexoesEntrada;
    private HashSet<ItemGrade> conexoesSaida;
    private double banda;
    private double ocupacao;
    private double latencia;
    private boolean configurado;

    public Internet(int x, int y, int idLocal, int idGlobal) {
        super(x, y);
        this.id = new IdentificadorItemGrade(idLocal, idGlobal, "net" + idGlobal);
        this.conexoesEntrada = new HashSet<ItemGrade>();
        this.conexoesSaida = new HashSet<ItemGrade>();
    }

    @Override
    public IdentificadorItemGrade getId() {
        return this.id;
    }
    
    @Override
    public Set<ItemGrade> getConexoesEntrada() {
        return conexoesEntrada;
    }

    @Override
    public Set<ItemGrade> getConexoesSaida() {
        return conexoesSaida;
    }
    
    @Override
    public String getAtributos(ResourceBundle palavras) {
        String texto = palavras.getString("Local ID:") + " " + id.getIdLocal()
         + "<br>" + palavras.getString("Global ID:") + " " + id.getIdGlobal()
         + "<br>" + palavras.getString("Label") + ": " + id.getNome()
         + "<br>" + palavras.getString("X-coordinate:") + " " + getX()
         + "<br>" + palavras.getString("Y-coordinate:") + " " + getY()
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
    public Internet criarCopia(int posicaoMouseX, int posicaoMouseY, int idGlobal, int idLocal) {
        Internet temp = new Internet(posicaoMouseX, posicaoMouseY, idGlobal, idLocal);
        temp.banda = this.banda;
        temp.ocupacao = this.ocupacao;
        temp.latencia = this.latencia;
        temp.verificaConfiguracao();
        return temp;
    }
    
    @Override
    public boolean isConfigurado() {
        return configurado;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(DesenhoGrade.IINTERNET, getX() - 15, getY() - 15, null);
        if (isConfigurado()) {
            g.drawImage(DesenhoGrade.IVERDE, getX() + 15, getY() + 15, null);
        } else {
            g.drawImage(DesenhoGrade.IVERMELHO, getX() + 15, getY() + 15, null);
        }

        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(getId().getIdGlobal()), getX(), getY() + 30);
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
