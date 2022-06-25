package ispd.gui.iconico.simulacao;

import ispd.gui.iconico.Vertex;
import ispd.motor.filas.servidores.CS_Processamento;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

public class Maquina extends Vertex {

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

    @Override
    public Image getImage() {
        return null;
    }
}
