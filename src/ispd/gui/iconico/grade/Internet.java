package ispd.gui.iconico.grade;

import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

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
    public String getAttributes(ResourceBundle resourceBundle) {
        String texto = resourceBundle.getString("Local ID:") + " " + id.getLocalId()
         + "<br>" + resourceBundle.getString("Global ID:") + " " + id.getGlobalId()
         + "<br>" + resourceBundle.getString("Label") + ": " + id.getName()
         + "<br>" + resourceBundle.getString("X-coordinate:") + " " + getX()
         + "<br>" + resourceBundle.getString("Y-coordinate:") + " " + getY()
         + "<br>" + resourceBundle.getString("Bandwidth") + ": " + getBanda()
         + "<br>" + resourceBundle.getString("Latency") + ": " + getLatencia()
         + "<br>" + resourceBundle.getString("Load Factor") + ": " + getTaxaOcupacao();
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
    public Internet makeCopy(int posicaoMouseX, int posicaoMouseY, int idGlobal, int idLocal) {
        Internet temp = new Internet(posicaoMouseX, posicaoMouseY, idGlobal, idLocal);
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
