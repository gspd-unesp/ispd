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

public abstract class AreaDesenho extends JPanel implements MouseListener,
        MouseMotionListener {

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
    private boolean gridOn;

    private Ruler.RulerUnit unit;
    private Ruler columnView;
    private Ruler rowView;
    private JPanel corner;
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
    protected Set<Icon> selecionados;
    /**
     * Lista com os vertices presentes na area de desenho
     */
    protected Set<Vertex> vertices;
    protected Set<Edge> arestas;
    //controle para adicionar icones
    private boolean addVertice;
    private boolean addAresta;
    private Vertex origemAresta;
    private int posicaoMouseX;
    private int posicaoMouseY;
    private String erroMensagem;
    private String erroTitulo;

    public AreaDesenho(boolean popupOn, boolean gridOn, boolean rectOn,
                       boolean posicaoFixa) {
        vertices = new HashSet<Vertex>();
        arestas = new HashSet<Edge>();
        selecionados = new HashSet<Icon>();
        this.popupOn = popupOn;
        this.gridOn = gridOn;
        this.rectOn = rectOn;
        this.posicaoFixa = posicaoFixa;
        this.INCH = Toolkit.getDefaultToolkit().getScreenResolution();
        this.origemAresta = null;
        this.initRuler();
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

    public Ruler getColumnView() {
        return columnView;
    }

    public Ruler getRowView() {
        return rowView;
    }

    public JPanel getCorner() {
        return corner;
    }

    public void setGridOn(boolean gridOn) {
        this.gridOn = gridOn;
        repaint();
    }

    /**
     * It updates the grid unit.
     *
     * @param unit the unit to be updated to
     */
    public void updateUnitTo(final Ruler.RulerUnit unit) {
        this.unit = unit;
        if (posicaoFixa && vertices != null) {
            for (Vertex icone : vertices) {
                ((Vertex) icone).setPosition(getPosFixaX(icone.getX()),
                        getPosFixaY(icone.getY()));
            }
        }
    }

    public Ruler.RulerUnit getUnit() {
        return this.unit;
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

    @Override
    public void mouseClicked(MouseEvent me) {
        if (addAresta) {
            if (origemAresta != null) {
                Icon destinoAresta = getSelecionado(me.getX(), me.getY());
                if (destinoAresta != null && destinoAresta instanceof Vertex && !origemAresta.equals(destinoAresta)) {
                    adicionarAresta(origemAresta, (Vertex) destinoAresta);
                    origemAresta = null;
                } else {
                    JOptionPane.showMessageDialog(null, erroMensagem,
                            erroTitulo, JOptionPane.WARNING_MESSAGE);
                }
            } else {
                Icon icon = getSelecionado(me.getX(), me.getY());
                if (icon != null && icon instanceof Vertex) {
                    origemAresta = (Vertex) icon;
                } else {
                    JOptionPane.showMessageDialog(null, erroMensagem,
                            erroTitulo, JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (!selecionados.isEmpty()) {
            Icon icon = getSelecionado(me.getX(), me.getY());
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
                adicionarVertice(getPosFixaX(me.getX()),
                        getPosFixaY(me.getY()));
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
        Icon icon = getSelecionado(me.getX(), me.getY());
        if (icon != null) {
            if (icon instanceof Vertex) {
                ((Vertex) icon).setBase(0, 0);
            }
            if (!selecionados.contains(icon)) {
                if (me.getButton() != MouseEvent.BUTTON2 && selecionados.size() >= 1) {
                    for (Icon icone : selecionados) {
                        icone.setSelected(false);
                    }
                    selecionados.clear();
                }
                icon.setSelected(true);
                selecionados.add(icon);
            }
            if (selecionados.size() > 1) {
                for (Icon icone : selecionados) {
                    if (icone instanceof Vertex) {
                        ((Vertex) icone).setBase(icone.getX() - me.getX(),
                                icone.getY() - me.getY());
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
            for (Icon icone : selecionados) {
                if (icone instanceof Vertex) {
                    ((Vertex) icone).setPosition(getPosFixaX(icone.getX()),
                            getPosFixaY(icone.getY()));
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
                for (Vertex icone : vertices) {
                    if (retanguloX < icone.getX()
                            && icone.getX() < (retanguloX + retanguloLag)
                            && retanguloY < icone.getY()
                            && icone.getY() < (retanguloY + retanguloAlt)) {
                        icone.setSelected(true);
                        selecionados.add(icone);
                    }
                }
                for (Edge icone : arestas) {
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
            for (Icon icone : selecionados) {
                if (icone instanceof Vertex) {
                    int posX = me.getX();
                    int posY = me.getY();
                    ((Vertex) icone).setPosition(posX + ((Vertex) icone).getBaseX(), posY + ((Vertex) icone).getBaseY());
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
            for (Icon icone : vertices) {
                if (retX < icone.getX()
                        && icone.getX() < (retX + retLag)
                        && retY < icone.getY()
                        && icone.getY() < (retY + retAlt)) {
                    icone.setSelected(true);
                } else {
                    icone.setSelected(false);
                }
            }
            for (Icon icone : arestas) {
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
            g.drawLine(origemAresta.getX(), origemAresta.getY(),
                    posicaoMouseX, posicaoMouseY);
        }
        drawRect(g);
        // Desenhamos todos os icones
        for (Icon icone : arestas) {
            icone.draw(g);
        }
        for (Icon icone : vertices) {
            icone.draw(g);
        }
    }

    /**
     * It initializes the horizontal and the vertical rulers,
     * as well as the unit button used to change the both
     * ruler's unit.
     */
    private void initRuler() {
        /* centimeters (cm) unit set as default */
        this.updateUnitTo(Ruler.RulerUnit.CENTIMETERS);

        /* Create the row and column rulers */
        this.columnView = new Ruler(Ruler.RulerOrientation.HORIZONTAL,
                this.unit);
        this.rowView = new Ruler(Ruler.RulerOrientation.VERTICAL,
                this.unit);

        this.columnView.setPreferredWidth(this.getWidth());
        this.rowView.setPreferredHeight(this.getHeight());

        final var unitButton = new JButton(this.unit.getSymbol());

        /* Create the corner and add the unit button */
        this.corner = new JPanel();
        this.corner.add(unitButton);

        /* Add an action that when the unit button is clicked */
        /* the grid is changed to the next one */
        unitButton.addActionListener(evt -> {
            this.updateUnitTo(this.unit.nextUnit());
            this.rowView.updateUnitTo(this.unit);
            this.columnView.updateUnitTo(this.unit);

            ((JButton) evt.getSource())
                    .setText(this.unit.getSymbol());

            if (isGridOn()) {
                repaint();
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
    }

    public void setErrorText(String mensagem, String titulo) {
        this.erroMensagem = mensagem;
        this.erroTitulo = titulo;
    }

    public void setPopupButtonText(String icone, String vertice,
                                   String aresta, String painel) {
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
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    private void drawRect(Graphics g) {
        // Desenha retangulo
        if (rectOn && rect) {
            g.setColor(Color.BLACK);
            if (retanguloLag < 0 && retanguloAlt < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX + retanguloLag,
                        retanguloY + retanguloAlt, retanguloLag * -1,
                        retanguloAlt * -1);
                g.setColor(new Color((float) 0, (float) 0, (float) 1,
                        (float) 0.2));
                g.fillRect(retanguloX + retanguloLag,
                        retanguloY + retanguloAlt, retanguloLag * -1,
                        retanguloAlt * -1);
            } else if (retanguloLag < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX + retanguloLag, retanguloY,
                        retanguloLag * -1, retanguloAlt);
                g.setColor(new Color((float) 0, (float) 0, (float) 1,
                        (float) 0.2));
                g.fillRect(retanguloX + retanguloLag, retanguloY,
                        retanguloLag * -1, retanguloAlt);
            } else if (retanguloAlt < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX, retanguloY + retanguloAlt,
                        retanguloLag, retanguloAlt * -1);
                g.setColor(new Color((float) 0, (float) 0, (float) 1,
                        (float) 0.2));
                g.fillRect(retanguloX, retanguloY + retanguloAlt,
                        retanguloLag, retanguloAlt * -1);
            } else {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX, retanguloY, retanguloLag, retanguloAlt);
                g.setColor(new Color((float) 0, (float) 0, (float) 1,
                        (float) 0.2));
                g.fillRect(retanguloX, retanguloY, retanguloLag, retanguloAlt);
            }
        }
    }

    private void drawGrid(Graphics g) {
        if (isGridOn()) {
            g.setColor(Color.LIGHT_GRAY);
            final var increment = this.unit.getIncrement();
            for (int _w = 0; _w <= this.getWidth(); _w += increment) {
                g.drawLine(_w, 0, _w, this.getHeight());
            }
            for (int _h = 0; _h <= this.getHeight(); _h += increment) {
                g.drawLine(0, _h, this.getWidth(), _h);
            }
        }
    }

    private void drawPoints(Graphics g) {
        if (isPosicaoFixa()) {
            g.setColor(Color.GRAY);
            final var increment = this.unit.getIncrement();
            for (int i = increment; i <= this.getWidth(); i += increment) {
                for (int j = increment; j <= this.getHeight(); j += increment) {
                    g.fillRect(i - 1, j - 1, 3, 3);
                }
            }
        }
    }

    protected Icon getSelecionado(int x, int y) {
        for (Vertex vertice : vertices) {
            if (vertice.contains(x, y)) {
                return vertice;
            }
        }
        for (Edge aresta : arestas) {
            if (aresta.contains(x, y)) {
                return aresta;
            }
        }
        if (!selecionados.isEmpty()) {
            for (Icon icone : selecionados) {
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
     * @param Origem  Vertice de origem da aresta
     * @param Destino Vertice de destino da aresta
     */
    public abstract void adicionarAresta(Vertex Origem, Vertex Destino);

    public abstract void showActionIcon(MouseEvent me, Icon icon);

    public abstract void showSelectionIcon(MouseEvent me, Icon icon);

    public abstract void botaoPainelActionPerformed(ActionEvent evt);

    public abstract void botaoVerticeActionPerformed(ActionEvent evt);

    public abstract void botaoArestaActionPerformed(ActionEvent evt);

    public abstract void botaoIconeActionPerformed(ActionEvent evt);

    public void showPopupIcon(MouseEvent me, Icon icon) {
        if (icon instanceof Vertex) {
            if (jMenuVertice1.isEnabled()) {
                jMenuVertice1.setVisible(true);
            }
            popupVertice.show(me.getComponent(), me.getX(), me.getY());
        } else if (icon instanceof Edge) {
            if (jMenuAresta1.isEnabled()) {
                jMenuAresta1.setVisible(true);
            }
            popupAresta.show(me.getComponent(), me.getX(), me.getY());
        }
    }

    private int getPosFixaX(int x) {
        final var increment = this.unit.getIncrement();
        int novaPosicao = x;
        int diferenca = (x % increment);
        int indexPosicao = (x / increment);
        //Verifica se está na posição correta
        if (diferenca != 0) {
            //verifica para qual ponto deve deslocar a posição
            if (diferenca < increment / 2) {
                novaPosicao = indexPosicao * increment;
            } else {
                novaPosicao = indexPosicao * increment + increment;
            }
        }
        //Caso ícone saia da tela volta uma posição
        if (novaPosicao >= this.getWidth()) {
            indexPosicao = this.getWidth() / increment;
            novaPosicao = indexPosicao * increment;
        } else if (novaPosicao <= 0) {
            novaPosicao = increment;
        }
        return novaPosicao;
    }

    private int getPosFixaY(int y) {
        final var increment = this.unit.getIncrement();
        int novaPosicao = y;
        int diferenca = (y % increment);
        int indexPosicao = (y / increment);
        //Verifica se está na posição correta
        if (diferenca != 0) {
            //verifica para qual ponto deve deslocar a posição
            if (diferenca < increment / 2) {
                novaPosicao = indexPosicao * increment;
            } else {
                novaPosicao = indexPosicao * increment + increment;
            }
        }
        //Caso ícone saia da tela volta uma posição
        if (novaPosicao >= this.getHeight()) {
            indexPosicao = this.getHeight() / increment;
            novaPosicao = indexPosicao * increment;
        } else if (novaPosicao <= 0) {
            novaPosicao = increment;
        }
        return novaPosicao;
    }
}
