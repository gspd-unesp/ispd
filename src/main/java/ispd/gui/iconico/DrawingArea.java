package ispd.gui.iconico;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class DrawingArea extends JPanel
        implements MouseListener, MouseMotionListener {
    private static final Color RECTANGLE_FILL_COLOR =
            new Color(0.0f, 0.0f, 1.0f, 0.2f);
    private static final RulerUnit DEFAULT_UNIT =
            RulerUnit.CENTIMETERS;
    protected final Set<Icon> selectedIcons = new HashSet<>(0);
    protected final Set<Vertex> vertices = new HashSet<>(0);
    protected final Set<Edge> edges = new HashSet<>(0);
    private final boolean isPopupOn;
    private final boolean isRectOn;
    private final boolean isPositionFixed;
    protected JPopupMenu generalPopup;
    private JPopupMenu vertexPopup;
    private JPopupMenu edgePopup;
    private JMenuItem jMenuIcon1A;
    private JMenuItem jMenuIcon1V;
    private JMenuItem jMenuVertex;
    private JMenuItem jMenuEdge;
    private JMenuItem jMenuPanel;
    private boolean isGridOn;
    private RulerUnit unit = null;
    private Ruler columnRuler;
    private Ruler rowRuler;
    private JPanel cornerUnitButton;
    private boolean shouldDrawRect = false;
    private int rectangleX = 0;
    private int rectangleY = 0;
    private int rectangleWidth = 0;
    private int rectangleHeight = 0;
    private boolean addVertex = false;
    private boolean isDrawingEdge = false;
    private Vertex edgeOrigin = null;
    private int mousePosX = 0;
    private int mousePosY = 0;
    private String errorMessage = null;
    private String errorTitle = null;

    protected DrawingArea(
            final boolean isPopupOn,
            final boolean isGridOn,
            final boolean isRectOn,
            final boolean isPositionFixed) {
        this.isPopupOn = isPopupOn;
        this.isGridOn = isGridOn;
        this.isRectOn = isRectOn;
        this.isPositionFixed = isPositionFixed;
        this.initRuler();
        this.initGeneralPopup();
        this.initIconPopup();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    private static JPanel panelWith(final Component component) {
        final JPanel corner = new JPanel();
        corner.add(component);
        return corner;
    }

    private static int clampPosition(
            final int position, final int range,
            final int increment) {
        if (position <= 0) {
            return increment;
        } else if (position >= range) {
            return (range / increment) * increment;
        }
        return position;
    }

    /**
     * It initializes the horizontal and the vertical rulers,
     * as well as the unit button used to change the both
     * ruler's unit.
     */
    private void initRuler() {
        this.updateUnitTo(DrawingArea.DEFAULT_UNIT);

        this.columnRuler = new Ruler(
                RulerOrientation.HORIZONTAL, this.unit);
        this.columnRuler.setPreferredWidth(this.getWidth());
        this.rowRuler = new Ruler(
                RulerOrientation.VERTICAL, this.unit);
        this.rowRuler.setPreferredHeight(this.getHeight());

        final var unitButton = new JButton(this.unit.getSymbol());
        unitButton.addActionListener(this::onUnitButtonClicked);
        this.cornerUnitButton = DrawingArea.panelWith(unitButton);
    }

    private void initGeneralPopup() {
        this.jMenuPanel = new JMenuItem("Paste");
        this.jMenuPanel.setEnabled(false);
        this.jMenuPanel.addActionListener(this::botaoPainelActionPerformed);
        this.generalPopup = new JPopupMenu();
        this.generalPopup.add(this.jMenuPanel);
    }

    private void initIconPopup() {
        this.vertexPopup = new JPopupMenu();
        this.edgePopup = new JPopupMenu();

        final JMenuItem jMenuVertice0 = new JMenuItem();
        jMenuVertice0.addActionListener(evt -> {
        });
        this.vertexPopup.add(jMenuVertice0);
        jMenuVertice0.setVisible(false);


        this.jMenuVertex = new JMenuItem("Copy");
        this.jMenuVertex.addActionListener(this::botaoVerticeActionPerformed);
        this.vertexPopup.add(this.jMenuVertex);

        this.jMenuEdge = new JMenuItem("Turn Over");
        this.jMenuEdge.addActionListener(this::botaoArestaActionPerformed);
        this.edgePopup.add(this.jMenuEdge);

        this.vertexPopup.add(new JSeparator());
        this.edgePopup.add(new JSeparator());

        this.jMenuIcon1V = new JMenuItem("Remove");
        this.jMenuIcon1V.addActionListener(this::botaoIconeActionPerformed);
        this.vertexPopup.add(this.jMenuIcon1V);

        this.jMenuIcon1A = new JMenuItem("Remove");
        this.jMenuIcon1A.addActionListener(this::botaoIconeActionPerformed);
        this.edgePopup.add(this.jMenuIcon1A);
    }

    /**
     * It updates the grid unit.
     *
     * @param newUnit the unit to be updated to
     */
    private void updateUnitTo(final RulerUnit newUnit) {
        this.unit = newUnit;

        if (!this.isPositionFixed) {
            return;
        }

        for (final var v : this.vertices) {
            v.setPosition(
                    this.getPosFixaX(v.getX()),
                    this.getPosFixaY(v.getY())
            );
        }
    }

    private void onUnitButtonClicked(final ActionEvent evt) {
        this.updateUnitTo(this.unit.nextUnit());

        this.rowRuler.updateUnitTo(this.unit);
        this.columnRuler.updateUnitTo(this.unit);

        ((AbstractButton) evt.getSource())
                .setText(this.unit.getSymbol());

        if (this.isGridOn) {
            this.repaint();
        }
    }

    public abstract void botaoPainelActionPerformed(ActionEvent evt);

    public abstract void botaoVerticeActionPerformed(ActionEvent evt);

    public abstract void botaoArestaActionPerformed(ActionEvent evt);

    public abstract void botaoIconeActionPerformed(ActionEvent evt);

    protected int getPosicaoMouseX() {
        return this.mousePosX;
    }

    protected int getPosicaoMouseY() {
        return this.mousePosY;
    }

    protected void setAddVertice(final boolean addVertice) {
        this.addVertex = addVertice;
    }

    public Ruler getColumnView() {
        return this.columnRuler;
    }

    public Ruler getRowView() {
        return this.rowRuler;
    }

    public JPanel getCorner() {
        return this.cornerUnitButton;
    }

    protected RulerUnit getUnit() {
        return this.unit;
    }

    @Override
    public void mouseClicked(final MouseEvent me) {
        if (this.isDrawingEdge) {
            if (this.edgeOrigin != null) {
                final Icon destinoAresta = this.getSelectedIcon(me.getX(),
                        me.getY());
                if (destinoAresta instanceof Vertex && !this.edgeOrigin.equals(destinoAresta)) {
                    this.adicionarAresta(this.edgeOrigin,
                            (Vertex) destinoAresta);
                    this.edgeOrigin = null;
                } else {
                    JOptionPane.showMessageDialog(null, this.errorMessage,
                            this.errorTitle, JOptionPane.WARNING_MESSAGE);
                }
            } else {
                final Icon icon = this.getSelectedIcon(me.getX(), me.getY());
                if (icon instanceof Vertex) {
                    this.edgeOrigin = (Vertex) icon;
                } else {
                    JOptionPane.showMessageDialog(null, this.errorMessage,
                            this.errorTitle, JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (!this.selectedIcons.isEmpty()) {
            final Icon icon = this.getSelectedIcon(me.getX(), me.getY());
            if (icon != null) {
                if (me.getButton() == MouseEvent.BUTTON3) {
                    if (this.isPopupOn) {
                        this.showPopupIcon(me, icon);
                    }
                } else if (me.getClickCount() == 2) {
                    this.showActionIcon(me, icon);
                } else if (me.getClickCount() == 1) {
                    this.showSelectionIcon(me, icon);
                }
            }
        } else if (this.addVertex) {
            if (this.isPositionFixed) {
                this.adicionarVertice(
                        this.getPosFixaX(me.getX()),
                        this.getPosFixaY(me.getY())
                );
            } else {
                this.adicionarVertice(me.getX(), me.getY());
            }
        } else if (this.isPopupOn) {
            if (me.getButton() == MouseEvent.BUTTON3) {
                this.generalPopup.show(me.getComponent(), me.getX(), me.getY());
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent me) {
        //Verifica se algum icone foi selecionado
        final Icon icon = this.getSelectedIcon(me.getX(), me.getY());
        if (icon != null) {
            if (icon instanceof Vertex) {
                ((Vertex) icon).setBase(0, 0);
            }
            if (!this.selectedIcons.contains(icon)) {
                if (me.getButton() != MouseEvent.BUTTON2 && this.selectedIcons.size() >= 1) {
                    for (final Icon icone : this.selectedIcons) {
                        icone.setSelected(false);
                    }
                    this.selectedIcons.clear();
                }
                icon.setSelected(true);
                this.selectedIcons.add(icon);
            }
            if (this.selectedIcons.size() > 1) {
                for (final Icon icone : this.selectedIcons) {
                    if (icone instanceof Vertex) {
                        ((Vertex) icone).setBase(icone.getX() - me.getX(),
                                icone.getY() - me.getY());
                    }
                }
            }
        }
        //Indica ponto inicial do retangulo
        if (this.isRectOn && this.selectedIcons.isEmpty()) {
            this.shouldDrawRect = true;
            this.rectangleX = me.getX();
            this.rectangleY = me.getY();
            this.rectangleWidth = 0;
            this.rectangleHeight = 0;
        }
        this.repaint();
    }

    @Override
    public void mouseReleased(final MouseEvent me) {
        //Arruma ícone na tela
        if (this.isPositionFixed && !this.selectedIcons.isEmpty()) {
            for (final var icon : this.selectedIcons) {
                if (icon instanceof Vertex) {
                    ((Vertex) icon).setPosition(
                            this.getPosFixaX(icon.getX()),
                            this.getPosFixaY(icon.getY())
                    );
                }
            }
        }

        if (!this.isRectOn) {
            this.repaint();
            return;
        }

        //Ajusta posição do retangulo
        if (this.rectangleWidth < 0) {
            this.rectangleX += this.rectangleWidth;
            this.rectangleWidth *= -1;
        }
        if (this.rectangleHeight < 0) {
            this.rectangleY += this.rectangleHeight;
            this.rectangleHeight *= -1;
        }
        //Adiciona icone na lista de selecionados
        if (this.selectedIcons.isEmpty()) {
            for (final Vertex icone : this.vertices) {
                if (this.isInSelectionRectangle(icone)) {
                    icone.setSelected(true);
                    this.selectedIcons.add(icone);
                }
            }
            for (final Edge icone : this.edges) {
                if (this.isInSelectionRectangle(icone)) {
                    icone.setSelected(true);
                    this.selectedIcons.add(icone);
                }
            }
        }
        this.shouldDrawRect = false;
        this.repaint();
    }

    private Icon getSelectedIcon(final int x, final int y) {
        for (final var v : this.vertices) {
            if (v.contains(x, y)) {
                return v;
            }
        }
        for (final var e : this.edges) {
            if (e.contains(x, y)) {
                return e;
            }
        }
        if (!this.selectedIcons.isEmpty()) {
            for (final var icon : this.selectedIcons) {
                icon.setSelected(false);
            }
            this.selectedIcons.clear();
        }
        return null;
    }

    private boolean isInSelectionRectangle(final Icon icon) {
        return DrawingArea.isInRange(
                icon.getX(), this.rectangleX, this.rectangleWidth)
               && DrawingArea.isInRange(
                icon.getY(), this.rectangleY, this.rectangleHeight);
    }

    /**
     * Realiza a adição de uma aresta à area de desenho. Este método é chamado
     * quando se realiza a conexão entre dois vertices com o addAresta ativo
     *
     * @param Origem  Vertice de origem da aresta
     * @param Destino Vertice de destino da aresta
     */
    public abstract void adicionarAresta(Vertex Origem, Vertex Destino);

    private void showPopupIcon(final MouseEvent me, final Icon icon) {
        if (icon instanceof Vertex) {
            if (this.jMenuVertex.isEnabled()) {
                this.jMenuVertex.setVisible(true);
            }
            this.vertexPopup.show(me.getComponent(), me.getX(), me.getY());
        } else if (icon instanceof Edge) {
            if (this.jMenuEdge.isEnabled()) {
                this.jMenuEdge.setVisible(true);
            }
            this.edgePopup.show(me.getComponent(), me.getX(), me.getY());
        }
    }

    public abstract void showActionIcon(MouseEvent me, Icon icon);

    public abstract void showSelectionIcon(MouseEvent me, Icon icon);

    /**
     * Realiza a adição de um vertice à area de desenho. Este método é chamado
     * quando o mouse é precionado com addVertice ativo
     *
     * @param x posição no eixo X
     * @param y posição no eixo Y
     */
    public abstract void adicionarVertice(int x, int y);

    private int getPosFixaX(final int x) {
        return this.convertToFixedPosition(x, this.getWidth());
    }

    private int getPosFixaY(final int y) {
        return this.convertToFixedPosition(y, this.getHeight());
    }

    private int convertToFixedPosition(final int pos, final int range) {
        final var increment = this.unit.getIncrement();
        final int offset = (pos % increment);
        final int positionIndex = (pos / increment);
        //Verifica se está na posição correta
        final int newPosition;
        if (offset == 0) {
            newPosition = pos;
        } else {
            //verifica para qual ponto deve deslocar a posição
            if (offset < increment / 2) {
                newPosition = positionIndex * increment;
            } else {
                newPosition = positionIndex * increment + increment;
            }
        }

        return DrawingArea.clampPosition(newPosition, range, increment);
    }

    @Override
    public void mouseDragged(final MouseEvent me) {
        this.updateIcons(me.getX(), me.getY());
        this.repaint();
    }

    private void updateIcons(final int x, final int y) {
        if (!this.selectedIcons.isEmpty()) {
            this.dragSelectedIcons(x, y);
            return;
        }

        if (!this.shouldDrawRect || !this.isRectOn) {
            return;
        }

        this.updateRectangleAndSelectIcons(x, y);
    }

    private void updateRectangleAndSelectIcons(final int x, final int y) {
        this.rectangleWidth = x - this.rectangleX;
        this.rectangleHeight = y - this.rectangleY;

        // TODO: This logic is duplicated in many places
        final int retX;
        final int retLag;
        if (this.rectangleWidth < 0) {
            retX = this.rectangleX + this.rectangleWidth;
            retLag = this.rectangleWidth * -1;
        } else {
            retX = this.rectangleX;
            retLag = this.rectangleWidth;
        }

        final int retY;
        final int retAlt;
        if (this.rectangleHeight < 0) {
            retY = this.rectangleY + this.rectangleHeight;
            retAlt = this.rectangleHeight * -1;
        } else {
            retY = this.rectangleY;
            retAlt = this.rectangleHeight;
        }

        Stream.concat(this.vertices.stream(), this.edges.stream())
                .filter(icon -> DrawingArea.isIconWithinRect(
                        icon, retX, retY, retLag, retAlt))
                .forEach(icon -> icon.setSelected(true));
    }

    private void dragSelectedIcons(final int x, final int y) {
        this.selectedIcons.stream()
                .filter(Vertex.class::isInstance)
                .map(Vertex.class::cast)
                .forEach(v -> v.setPosition(
                        x + v.getBaseX(),
                        y + v.getBaseY()
                ));
    }

    private static boolean isIconWithinRect(final Icon icon,
                                            final int x, final int y,
                                            final int w, final int h) {
        return DrawingArea.isInRange(icon.getX(), x, w)
               && DrawingArea.isInRange(icon.getY(), y, h);
    }

    private static boolean isInRange(
            final int pos, final int start, final int size) {
        return start <= pos && pos <= start + size;
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        this.mousePosX = e.getX();
        this.mousePosY = e.getY();
        if (this.isDrawingEdge) {
            this.repaint();
        }
    }

    protected void setIsDrawingEdge(final boolean isDrawingEdge) {
        this.isDrawingEdge = isDrawingEdge;
        this.edgeOrigin = null;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        this.drawBackground(g);
        this.drawGrid(g);
        this.drawPoints(g);
        //Desenha a linha da conexão de rede antes dela se estabelcer.
        if (this.edgeOrigin != null) {
            g.setColor(new Color(0, 0, 0));
            g.drawLine(this.edgeOrigin.getX(), this.edgeOrigin.getY(),
                    this.mousePosX, this.mousePosY);
        }
        this.drawRect(g);
        // Desenhamos todos os icones
        for (final Icon icone : this.edges) {
            icone.draw(g);
        }
        for (final Icon icone : this.vertices) {
            icone.draw(g);
        }
    }

    private void drawBackground(final Graphics g) {
        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    private void drawGrid(final Graphics g) {
        if (!this.isGridOn) {
            return;
        }

        g.setColor(Color.LIGHT_GRAY);
        final var increment = this.unit.getIncrement();
        for (int w = 0; w <= this.getWidth(); w += increment) {
            g.drawLine(w, 0, w, this.getHeight());
        }
        for (int h = 0; h <= this.getHeight(); h += increment) {
            g.drawLine(0, h, this.getWidth(), h);
        }
    }

    private void drawPoints(final Graphics g) {
        if (!this.isPositionFixed) {
            return;
        }

        g.setColor(Color.GRAY);
        final var increment = this.unit.getIncrement();
        for (int i = increment; i <= this.getWidth(); i += increment) {
            for (int j = increment; j <= this.getHeight(); j += increment) {
                g.fillRect(i - 1, j - 1, 3, 3);
            }
        }
    }

    private void drawRect(final Graphics g) {
        if (!this.isRectOn || !this.shouldDrawRect) {
            return;
        }

        final int x;
        final int w;

        if (this.rectangleWidth >= 0) {
            x = this.rectangleX;
            w = this.rectangleWidth;
        } else {
            x = this.rectangleX + this.rectangleWidth;
            w = this.rectangleWidth * -1;
        }

        final int y;
        final int h;

        if (this.rectangleHeight >= 0) {
            y = this.rectangleY;
            h = this.rectangleHeight;
        } else {
            y = this.rectangleY + this.rectangleHeight;
            h = this.rectangleHeight * -1;
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);
        g.setColor(DrawingArea.RECTANGLE_FILL_COLOR);
        g.fillRect(x, y, w, h);
    }

    protected boolean isGridOn() {
        return this.isGridOn;
    }

    public void setGridOn(final boolean gridOn) {
        this.isGridOn = gridOn;
        this.repaint();
    }

    protected void setErrorText(final String message, final String title) {
        this.errorMessage = message;
        this.errorTitle = title;
    }

    protected void setPopupButtonText(
            final String icon,
            final String vertex,
            final String edge,
            final String panel) {
        if (icon != null) {
            this.jMenuIcon1A.setText(icon);
            this.jMenuIcon1A.setVisible(true);
            this.jMenuIcon1V.setText(icon);
            this.jMenuIcon1V.setVisible(true);
        } else {
            this.jMenuIcon1A.setEnabled(false);
            this.jMenuIcon1A.setVisible(false);
            this.jMenuIcon1V.setEnabled(false);
            this.jMenuIcon1V.setVisible(false);
        }

        if (vertex != null) {
            this.jMenuVertex.setText(vertex);
            this.jMenuVertex.setVisible(true);
        } else {
            this.jMenuVertex.setEnabled(false);
            this.jMenuVertex.setVisible(false);
        }

        if (edge != null) {
            this.jMenuEdge.setText(edge);
            this.jMenuEdge.setVisible(true);
        } else {
            this.jMenuEdge.setEnabled(false);
            this.jMenuEdge.setVisible(false);
        }

        if (panel != null) {
            this.jMenuPanel.setText(panel);
            this.jMenuPanel.setVisible(true);
        } else {
            this.jMenuPanel.setEnabled(false);
            this.jMenuPanel.setVisible(false);
        }
    }
}