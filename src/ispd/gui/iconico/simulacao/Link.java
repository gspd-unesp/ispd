package ispd.gui.iconico.simulacao;

import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.motor.filas.servidores.implementacao.CS_Link;

import java.awt.Color;
import java.awt.Graphics;

public class Link extends Edge {

    public CS_Link link;
    private DesenhoSimulacao aDesenho;

    public Link(Vertex origem, Vertex destino, CS_Link link, DesenhoSimulacao aDesenho) {
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
        g.drawLine(getSource().getX(), getSource().getY(), getDestination().getX(), getDestination().getY());
        //Desenha tarefas
        if (link != null) {
            int num = link.getCargaTarefas();
            if (num > 0) {
                String seta;
                if (getSource().getX() < getDestination().getX()) {
                    seta = "▶";
                } else if (getSource().getX() > getDestination().getX()) {
                    seta = "◀";
                } else if (getSource().getY() < getDestination().getY()) {
                    seta = "▼";
                } else {
                    seta = "▲";
                }
                //Movimento
                int incX = (getDestination().getX() - getSource().getX()) / aDesenho.getSetas();
                int incY = (getDestination().getY() - getSource().getY()) / aDesenho.getSetas();
                g.drawString(seta, getSource().getX() + incX, getSource().getY() + incY    );
                g.drawString(seta, getSource().getX() + incX * 2, getSource().getY() + incY * 2);
                g.drawString(seta, getSource().getX() + incX * 3, getSource().getY() + incY * 3);
                g.drawString(seta, getSource().getX() + incX * 4, getSource().getY() + incY * 4);
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