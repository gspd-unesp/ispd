package ispd.gui.iconico.grade;

import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ResourceBundle;
import java.util.Set;

public class Link extends Edge implements GridItem {

    private GridItemIdentifier id;
    private boolean selected;
    private Polygon areaSeta;
    private static Color DARK_GREEN = new Color(0, 130, 0);
    private double banda;
    private double ocupacao;
    private double latencia;
    private boolean configurado;

    public Link(Vertex origem, Vertex destino, int idLocal, int idGlobal) {
        super(origem, destino);
        this.selected = true;
        this.areaSeta = new Polygon();
        this.id = new GridItemIdentifier(idLocal, idGlobal, "link" + idGlobal);
    }

    @Override
    public GridItemIdentifier getId() {
        return this.id;
    }

    @Override
    public Set<GridItem> getInboundConnections() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<GridItem> getOutboundConnections() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAttributes(ResourceBundle resourceBundle) {
        String texto = resourceBundle.getString("Local ID:") + " " + getId().getLocalId()
                + "<br>" + resourceBundle.getString("Global ID:") + " " + getId().getGlobalId()
                + "<br>" + resourceBundle.getString("Label") + ": " + getId().getName()
                + "<br>" + resourceBundle.getString("X1-coordinate:") + " " + getSource().getX()
                + "<br>" + resourceBundle.getString("Y1-coordinate:") + " " + getSource().getY()
                + "<br>" + resourceBundle.getString("X2-coordinate:") + " " + getDestination().getY()
                + "<br>" + resourceBundle.getString("Y2-coordinate:") + " " + getDestination().getX()
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
    public Link makeCopy(int posicaoMouseX, int posicaoMouseY, int idGlobal, int idLocal) {
        Link temp = new Link(null, null, idGlobal, idLocal);
        temp.banda = this.banda;
        temp.latencia = this.latencia;
        temp.ocupacao = this.ocupacao;
        temp.verificaConfiguracao();
        return temp;
    }

    @Override
    public boolean isConfigured() {
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
        vecLine[0] = (double) xPoints[ 0] - getSource().getX();
        vecLine[1] = (double) yPoints[ 0] - getSource().getY();

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
        } else if (isConfigured()) {
            g.setColor(DARK_GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.drawLine(getSource().getX(), getSource().getY(), getDestination().getX(), getDestination().getY());
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
        return (((((getSource().getX() + getDestination().getX()) / 2) + getDestination().getX()) / 2) + getDestination().getX()) / 2;
    }

    @Override
    public Integer getY() {
        return (((((getSource().getY() + getDestination().getY()) / 2) + getDestination().getY()) / 2) + getDestination().getY()) / 2;
    }

    private void verificaConfiguracao() {
        if(banda > 0 && latencia > 0){
            configurado = true;
        } else {
            configurado = false;
        }
    }
}
