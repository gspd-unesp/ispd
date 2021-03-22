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
 * AreaDesenho.java
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
package ispd.gui.iconico;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 *
 * @author denison
 */
public abstract class AreaDesenho extends JPanel implements MouseListener, MouseMotionListener {

    //Objetos para apresentar opções
    private boolean popupOn;
    protected JPopupMenu popupVertice;
    protected JPopupMenu popupAresta;
    protected JPopupMenu popupGeral;
    private JMenuItem jMenuIcone1A;
    private JMenuItem jMenuIcone1V;
    /**
     * Menu Item exclusivo de cada vertice
     */
    private JMenuItem jMenuVertice0;
    /**
     * Menu Item compartilhado entre os Vertices
     */
    private JMenuItem jMenuVertice1;
    private JMenuItem jMenuAresta1;
    private JMenuItem jMenuPainel1;
    //Objetos usados para desenhar a regua e as grades
    private int INCH;
    private int units;
    private boolean gridOn;
    private Rule columnView;
    private Rule rowView;
    private JPanel corner;
    private boolean metric;
    //Objetos para desenhar Retângulo de seleção
    private boolean rectOn;
    private boolean rect = false;
    private int retanguloX;
    private int retanguloY;
    private int retanguloLag;
    private int retanguloAlt;
    //Objetos para posição
    private boolean posicaoFixa;
    //Lista de Objetos
    protected Set<Icone> selecionados;
    /**
     * Lista com os vertices presentes na area de desenho
     */
    protected Set<Vertice> vertices;
    protected Set<Aresta> arestas;
    //controle para adicionar icones
    private boolean addVertice;
    private boolean addAresta;
    private Vertice origemAresta;
    private int posicaoMouseX;
    private int posicaoMouseY;
    private String erroMensagem;
    private String erroTitulo;

    public AreaDesenho(boolean popupOn, boolean gridOn, boolean rectOn, boolean posicaoFixa) {
        vertices = new HashSet<Vertice>();
        arestas = new HashSet<Aresta>();
        selecionados = new HashSet<Icone>();
        this.popupOn = popupOn;
        this.gridOn = gridOn;
        this.rectOn = rectOn;
        this.posicaoFixa = posicaoFixa;
        this.INCH = Toolkit.getDefaultToolkit().getScreenResolution();
        this.origemAresta = null;
        iniciarRegua();
        iniciarPopupGeral();
        iniciarPopupIcone();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public int getPosicaoMouseX() {
        return posicaoMouseX;
    }

    public int getPosicaoMouseY() {
        return posicaoMouseY;
    }

    public boolean isMetric() {
        return metric;
    }

    public boolean isGridOn() {
        return gridOn;
    }

    public boolean isPopupOn() {
        return popupOn;
    }

    public boolean isRectOn() {
        return rectOn;
    }

    public boolean isPosicaoFixa() {
        return posicaoFixa;
    }

    public boolean isAddVertice() {
        return addVertice;
    }

    public void setAddVertice(boolean addVertice) {
        this.addVertice = addVertice;
    }

    public boolean isAddAresta() {
        return addAresta;
    }

    public void setAddAresta(boolean addAresta) {
        this.addAresta = addAresta;
        this.origemAresta = null;
    }

    public Rule getColumnView() {
        return columnView;
    }

    public Rule getRowView() {
        return rowView;
    }

    public JPanel getCorner() {
        return corner;
    }

    public void setGridOn(boolean gridOn) {
        this.gridOn = gridOn;
        repaint();
    }

    public void setMetric(boolean metric) {
        this.metric = metric;
        setUnits((int) (this.isMetric() ? (INCH / 2.54) : (INCH / 2)));
    }

    public void setPopupOn(boolean popupOn) {
        this.popupOn = popupOn;
    }

    public void setRectOn(boolean rectOn) {
        this.rectOn = rectOn;
    }

    public void setPosicaoFixa(boolean posicaoFixa) {
        this.posicaoFixa = posicaoFixa;
    }

    public void setUnits(int units) {
        this.units = units;
        if (posicaoFixa && vertices != null) {
            for (Vertice icone : vertices) {
                ((Vertice) icone).setPosition(getPosFixaX(icone.getX()), getPosFixaY(icone.getY()));
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (addAresta) {
            if (origemAresta != null) {
                Icone destinoAresta = getSelecionado(me.getX(), me.getY());
                if (destinoAresta != null && destinoAresta instanceof Vertice && !origemAresta.equals(destinoAresta)) {
                    adicionarAresta(origemAresta, (Vertice) destinoAresta);
                    origemAresta = null;
                } else {
                    JOptionPane.showMessageDialog(null, erroMensagem, erroTitulo, JOptionPane.WARNING_MESSAGE);
                }
            } else {
                Icone icon = getSelecionado(me.getX(), me.getY());
                if (icon != null && icon instanceof Vertice) {
                    origemAresta = (Vertice) icon;
                } else {
                    JOptionPane.showMessageDialog(null, erroMensagem, erroTitulo, JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (!selecionados.isEmpty()) {
            Icone icon = getSelecionado(me.getX(), me.getY());
            if (icon != null) {
                if (me.getButton() == MouseEvent.BUTTON3) {
                    if (popupOn) {
                        showPopupIcon(me, icon);
                    }
                } else if (me.getClickCount() == 2) {
                    showActionIcon(me, icon);
                } else if (me.getClickCount() == 1) {
                    showSelectionIcon(me, icon);
                }
            }
        } else if (addVertice) {
            if (isPosicaoFixa()) {
                adicionarVertice(getPosFixaX(me.getX()), getPosFixaY(me.getY()));
            } else {
                adicionarVertice(me.getX(), me.getY());
            }
        } else if (popupOn) {
            if (me.getButton() == MouseEvent.BUTTON3) {
                popupGeral.show(me.getComponent(), me.getX(), me.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        //Verifica se algum icone foi selecionado
        Icone icon = getSelecionado(me.getX(), me.getY());
        if (icon != null) {
            if (icon instanceof Vertice) {
                ((Vertice) icon).setBase(0, 0);
            }
            if (!selecionados.contains(icon)) {
                if (me.getButton() != MouseEvent.BUTTON2 && selecionados.size() >= 1) {
                    for (Icone icone : selecionados) {
                        icone.setSelected(false);
                    }
                    selecionados.clear();
                }
                icon.setSelected(true);
                selecionados.add(icon);
            }
            if (selecionados.size() > 1) {
                for (Icone icone : selecionados) {
                    if (icone instanceof Vertice) {
                        ((Vertice) icone).setBase(icone.getX() - me.getX(), icone.getY() - me.getY());
                    }
                }
            }
        }
        //Indica ponto inicial do retangulo
        if (rectOn && selecionados.isEmpty()) {
            rect = true;
            retanguloX = me.getX();
            retanguloY = me.getY();
            retanguloLag = 0;
            retanguloAlt = 0;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        //Arruma ícone na tela
        if (posicaoFixa && !selecionados.isEmpty()) {
            for (Icone icone : selecionados) {
                if (icone instanceof Vertice) {
                    ((Vertice) icone).setPosition(getPosFixaX(icone.getX()), getPosFixaY(icone.getY()));
                }
            }
        }
        if (rectOn) {
            //Ajusta posição do retangulo
            if (retanguloLag < 0) {
                retanguloX += retanguloLag;
                retanguloLag *= -1;
            }
            if (retanguloAlt < 0) {
                retanguloY += retanguloAlt;
                retanguloAlt *= -1;
            }
            //Adiciona icone na lista de selecionados
            if (selecionados.isEmpty()) {
                for (Vertice icone : vertices) {
                    if (retanguloX < icone.getX()
                            && icone.getX() < (retanguloX + retanguloLag)
                            && retanguloY < icone.getY()
                            && icone.getY() < (retanguloY + retanguloAlt)) {
                        icone.setSelected(true);
                        selecionados.add(icone);
                    }
                }
                for (Aresta icone : arestas) {
                    if (retanguloX < icone.getX()
                            && icone.getX() < (retanguloX + retanguloLag)
                            && retanguloY < icone.getY()
                            && icone.getY() < (retanguloY + retanguloAlt)) {
                        icone.setSelected(true);
                        selecionados.add(icone);
                    }
                }
            }
            rect = false;
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        posicaoMouseX = e.getX();
        posicaoMouseY = e.getY();
        if (isAddAresta()) {
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        //Arrasta icones selecionados pelo retangulo
        if (!selecionados.isEmpty()) {
            for (Icone icone : selecionados) {
                if (icone instanceof Vertice) {
                    int posX = me.getX();
                    int posY = me.getY();
                    ((Vertice) icone).setPosition(posX + ((Vertice) icone).getBaseX(), posY + ((Vertice) icone).getBaseY());
                }
            }
        } else if (rect && rectOn) {
            //Redefine dimensões do retangulo
            retanguloLag = me.getX() - retanguloX;
            retanguloAlt = me.getY() - retanguloY;
            int retX, retY, retLag, retAlt;
            if (retanguloLag < 0) {
                retX = retanguloX + retanguloLag;
                retLag = retanguloLag * -1;
            } else {
                retX = retanguloX;
                retLag = retanguloLag;
            }
            if (retanguloAlt < 0) {
                retY = retanguloY + retanguloAlt;
                retAlt = retanguloAlt * -1;
            } else {
                retY = retanguloY;
                retAlt = retanguloAlt;
            }
            //Seleciona icones dentro do retangulo
            for (Icone icone : vertices) {
                if (retX < icone.getX()
                        && icone.getX() < (retX + retLag)
                        && retY < icone.getY()
                        && icone.getY() < (retY + retAlt)) {
                    icone.setSelected(true);
                } else {
                    icone.setSelected(false);
                }
            }
            for (Icone icone : arestas) {
                if (retX < icone.getX()
                        && icone.getX() < (retX + retLag)
                        && retY < icone.getY()
                        && icone.getY() < (retY + retAlt)) {
                    icone.setSelected(true);
                } else {
                    icone.setSelected(false);
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawGrid(g);
        drawPoints(g);
        //Desenha a linha da conexão de rede antes dela se estabelcer.
        if (origemAresta != null) {
            g.setColor(new Color(0, 0, 0));
            g.drawLine(origemAresta.getX(), origemAresta.getY(), posicaoMouseX, posicaoMouseY);
        }
        drawRect(g);
        // Desenhamos todos os icones
        for (Icone icone : arestas) {
            icone.draw(g);
        }
        for (Icone icone : vertices) {
            icone.draw(g);
        }
    }

    private void iniciarRegua() {
        //Create the row and column headers.
        columnView = new Rule(Rule.HORIZONTAL, true);
        rowView = new Rule(Rule.VERTICAL, true);

        columnView.setPreferredWidth(this.getWidth());
        rowView.setPreferredHeight(this.getHeight());

        //Create the corners.
        corner = new JPanel(); //use FlowLayout
        JButton isMetric = new JButton("cm");
        corner.add(isMetric);
        setMetric(true);
        isMetric.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setMetric(!isMetric());
                if (isMetric()) {
                    //Turn it to metric.
                    ((JButton) evt.getSource()).setText("cm");
                    rowView.setIsMetric(true);
                    columnView.setIsMetric(true);
                } else {
                    //Turn it to inches.
                    ((JButton) evt.getSource()).setText(" in ");
                    rowView.setIsMetric(false);
                    columnView.setIsMetric(false);
                }
                if (isGridOn()) {
                    repaint();
                }
            }
        });
    }

    private void iniciarPopupIcone() {
        popupVertice = new JPopupMenu();
        popupAresta = new JPopupMenu();
        JSeparator jSeparatorA = new JSeparator();
        JSeparator jSeparatorV = new JSeparator();

        jMenuVertice0 = new JMenuItem();
        jMenuVertice0.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoExclusiveVerticeActionPerformed(evt);
            }
        });
        popupVertice.add(jMenuVertice0);
        jMenuVertice0.setVisible(false);


        jMenuVertice1 = new JMenuItem("Copy");
        jMenuVertice1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoVerticeActionPerformed(evt);
            }
        });
        popupVertice.add(jMenuVertice1);

        jMenuAresta1 = new JMenuItem("Turn Over");
        jMenuAresta1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoArestaActionPerformed(evt);
            }
        });
        popupAresta.add(jMenuAresta1);

        popupVertice.add(jSeparatorV);
        popupAresta.add(jSeparatorA);

        jMenuIcone1V = new JMenuItem("Remove");
        jMenuIcone1V.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoIconeActionPerformed(evt);
            }
        });
        popupVertice.add(jMenuIcone1V);

        jMenuIcone1A = new JMenuItem("Remove");
        jMenuIcone1A.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoIconeActionPerformed(evt);
            }
        });
        popupAresta.add(jMenuIcone1A);
    }

    private void iniciarPopupGeral() {
        popupGeral = new JPopupMenu();
        jMenuPainel1 = new JMenuItem("Paste");
        jMenuPainel1.setEnabled(false);
        jMenuPainel1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoPainelActionPerformed(evt);
            }
        });
        popupGeral.add(jMenuPainel1);
    }

    private void botaoExclusiveVerticeActionPerformed(ActionEvent evt) {
        Vertice icon = (Vertice) getSelecionado(getPosicaoMouseX(), getPosicaoMouseY());
        if (icon != null) {
            icon.exclusiveItemActionPerformed(evt);
        }
    }

    public void setErrorText(String mensagem, String titulo) {
        this.erroMensagem = mensagem;
        this.erroTitulo = titulo;
    }

    public void setPopupButtonText(String icone, String vertice, String aresta, String painel) {
        if (icone != null) {
            jMenuIcone1A.setText(icone);
            jMenuIcone1A.setVisible(true);
            jMenuIcone1V.setText(icone);
            jMenuIcone1V.setVisible(true);
        } else {
            jMenuIcone1A.setEnabled(false);
            jMenuIcone1A.setVisible(false);
            jMenuIcone1V.setEnabled(false);
            jMenuIcone1V.setVisible(false);
        }
        if (vertice != null) {
            jMenuVertice1.setText(vertice);
            jMenuVertice1.setVisible(true);
        } else {
            jMenuVertice1.setEnabled(false);
            jMenuVertice1.setVisible(false);
        }
        if (aresta != null) {
            jMenuAresta1.setText(aresta);
            jMenuAresta1.setVisible(true);
        } else {
            jMenuAresta1.setEnabled(false);
            jMenuAresta1.setVisible(false);
        }
        if (painel != null) {
            jMenuPainel1.setText(painel);
            jMenuPainel1.setVisible(true);
        } else {
            jMenuPainel1.setEnabled(false);
            jMenuPainel1.setVisible(false);
        }
    }

    private void drawBackground(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    private void drawRect(Graphics g) {
        // Desenha retangulo
        if (rectOn && rect) {
            g.setColor(Color.BLACK);
            if (retanguloLag < 0 && retanguloAlt < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX + retanguloLag, retanguloY + retanguloAlt, retanguloLag * -1, retanguloAlt * -1);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX + retanguloLag, retanguloY + retanguloAlt, retanguloLag * -1, retanguloAlt * -1);
            } else if (retanguloLag < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX + retanguloLag, retanguloY, retanguloLag * -1, retanguloAlt);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX + retanguloLag, retanguloY, retanguloLag * -1, retanguloAlt);
            } else if (retanguloAlt < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX, retanguloY + retanguloAlt, retanguloLag, retanguloAlt * -1);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX, retanguloY + retanguloAlt, retanguloLag, retanguloAlt * -1);
            } else {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX, retanguloY, retanguloLag, retanguloAlt);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX, retanguloY, retanguloLag, retanguloAlt);
            }
        }
    }

    private void drawGrid(Graphics g) {
        if (isGridOn()) {
            g.setColor(Color.LIGHT_GRAY);
            //int units = (int) (this.isMetric() ? (INCH / 2.54) : (INCH / 2));
            for (int _w = 0; _w <= this.getWidth(); _w += units) {
                g.drawLine(_w, 0, _w, this.getHeight());
            }
            for (int _h = 0; _h <= this.getHeight(); _h += units) {
                g.drawLine(0, _h, this.getWidth(), _h);
            }
        }
    }

    private void drawPoints(Graphics g) {
        if (isPosicaoFixa()) {
            g.setColor(Color.GRAY);
            //int units = (int) (this.isMetric() ? (INCH / 2.54) : (INCH / 2));
            for (int i = units; i <= this.getWidth(); i += units) {
                for (int j = units; j <= this.getHeight(); j += units) {
                    g.fillRect(i - 1, j - 1, 3, 3);
                }
            }
        }
    }

    protected Icone getSelecionado(int x, int y) {
        for (Vertice vertice : vertices) {
            if (vertice.contains(x, y)) {
                return vertice;
            }
        }
        for (Aresta aresta : arestas) {
            if (aresta.contains(x, y)) {
                return aresta;
            }
        }
        if (!selecionados.isEmpty()) {
            for (Icone icone : selecionados) {
                icone.setSelected(false);
            }
            selecionados.clear();
        }
        return null;
    }

    /**
     * Realiza a adição de um vertice à area de desenho. Este método é chamado
     * quando o mouse é precionado com addVertice ativo
     *
     * @param x posição no eixo X
     * @param y posição no eixo Y
     */
    public abstract void adicionarVertice(int x, int y);

    /**
     * Realiza a adição de uma aresta à area de desenho. Este método é chamado
     * quando se realiza a conexão entre dois vertices com o addAresta ativo
     *
     * @param Origem Vertice de origem da aresta
     * @param Destino Vertice de destino da aresta
     */
    public abstract void adicionarAresta(Vertice Origem, Vertice Destino);

    public abstract void showActionIcon(MouseEvent me, Icone icon);

    public abstract void showSelectionIcon(MouseEvent me, Icone icon);

    public abstract void botaoPainelActionPerformed(ActionEvent evt);

    public abstract void botaoVerticeActionPerformed(ActionEvent evt);

    public abstract void botaoArestaActionPerformed(ActionEvent evt);

    public abstract void botaoIconeActionPerformed(ActionEvent evt);

    public void showPopupIcon(MouseEvent me, Icone icon) {
        if (icon instanceof Vertice) {
            if (jMenuVertice1.isEnabled()) {
                jMenuVertice1.setVisible(true);
            }
            if (((Vertice) icon).getExclusiveItem() != null) {
                jMenuVertice0.setText(((Vertice) icon).getExclusiveItem());
                jMenuVertice0.setVisible(true);
            } else {
                jMenuVertice0.setVisible(false);
            }
            popupVertice.show(me.getComponent(), me.getX(), me.getY());
        } else if (icon instanceof Aresta) {
            if (jMenuAresta1.isEnabled()) {
                jMenuAresta1.setVisible(true);
            }
            popupAresta.show(me.getComponent(), me.getX(), me.getY());
        }
    }

    private int getPosFixaX(int x) {
        int novaPosicao = x;
        int diferenca = (x % units);
        int indexPosicao = (x / units);
        //Verifica se está na posição correta
        if (diferenca != 0) {
            //verifica para qual ponto deve deslocar a posição
            if (diferenca < units / 2) {
                novaPosicao = indexPosicao * units;
            } else {
                novaPosicao = indexPosicao * units + units;
            }
        }
        //Caso ícone saia da tela volta uma posição
        if (novaPosicao >= this.getWidth()) {
            indexPosicao = this.getWidth() / units;
            novaPosicao = indexPosicao * units;
        } else if (novaPosicao <= 0) {
            novaPosicao = units;
        }
        return novaPosicao;
    }

    private int getPosFixaY(int y) {
        int novaPosicao = y;
        int diferenca = (y % units);
        int indexPosicao = (y / units);
        //Verifica se está na posição correta
        if (diferenca != 0) {
            //verifica para qual ponto deve deslocar a posição
            if (diferenca < units / 2) {
                novaPosicao = indexPosicao * units;
            } else {
                novaPosicao = indexPosicao * units + units;
            }
        }
        //Caso ícone saia da tela volta uma posição
        if (novaPosicao >= this.getHeight()) {
            indexPosicao = this.getHeight() / units;
            novaPosicao = indexPosicao * units;
        } else if (novaPosicao <= 0) {
            novaPosicao = units;
        }
        return novaPosicao;
    }
}