package ispd.gui.iconico.simulacao;

import ispd.gui.iconico.Vertex;
import ispd.motor.filas.servidores.implementacao.CS_Switch;

import java.awt.Color;
import java.awt.Graphics;

public class Switch extends Vertex {

    private final int TAMANHO = 50;
    private CS_Switch icone;
    
    public Switch(int coluna, int linha, CS_Switch icone) {
        super(linha, coluna);
        this.icone = icone;
    }
    
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        int[] swt_x = {getX() - 25, getX() - 14, getX() + 25, getX() + 25, getX() + 15};
        int[] swt_y = {getY(), getY() - 25, getY() - 25, getY(), getY() + 25};
        g.setColor(Color.BLUE);
        g.fillPolygon(swt_x, swt_y, 5);
        g.fillRect(getX() - 25, getY(), TAMANHO - 10, TAMANHO / 2);
        g.setColor(Color.BLACK);
        g.drawRect(getX() - 25, getY(), TAMANHO - 10, TAMANHO / 2);
        g.setColor(Color.GREEN);
        g.drawString("→", getX() - 1, getY() - 13);
        g.drawString("←", getX() - 7, getY() - 7);
        Integer num = icone.getCargaTarefas();
        if (num > 0) {
            g.drawString(num.toString(), getX() - 21, getY() + 12);
        } else {
            g.drawString("▪▪▪▪", getX() - 21, getY() + 12);
        }
        g.drawString("▪▪▪▪", getX() - 21, getY() + 22);
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
