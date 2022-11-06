package ispd.gui.iconico.grade;

import ispd.arquivo.xml.IconicoXML;
import ispd.gui.MainWindow;
import ispd.gui.PickModelTypeDialog;
import ispd.gui.iconico.DrawingArea;
import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Icon;
import ispd.gui.iconico.Vertex;
import ispd.motor.workload.impl.PerNodeWorkloadGenerator;
import ispd.motor.workload.impl.CollectionWorkloadGenerator;
import ispd.motor.workload.impl.GlobalWorkloadGenerator;
import ispd.motor.workload.impl.TraceFileWorkloadGenerator;
import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.workload.WorkloadGeneratorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class DesenhoGrade extends DrawingArea {
    public static final int MACHINE = 1;
    public static final int NETWORK = 2;
    public static final int CLUSTER = 3;
    public static final int INTERNET = 4;
    /* package-private */ static final Image machineIcon =
            DesenhoGrade.getImage("imagens/botao_no.gif");
    /* package-private */ static final Image clusterIcon =
            DesenhoGrade.getImage("imagens/botao_cluster.gif");
    /* package-private */ static final Image internetIcon =
            DesenhoGrade.getImage("imagens/botao_internet.gif");
    /* package-private */ static final Image greenIcon =
            DesenhoGrade.getImage("imagens/verde.png");
    /* package-private */ static final Image redIcon =
            DesenhoGrade.getImage("imagens/vermelho.png");
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color ALMOST_WHITE = new Color(220, 220, 220);
    private static final int SOME_OFFSET = 50;
    private static final double FULL_CAPACITY = 100.0;
    //Objetos do cursor
    private final Cursor crossHairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    private final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    // (GRID, IAAS ou PAAS)
    private ResourceBundle translator =
            ResourceBundle.getBundle("ispd.idioma.Idioma", Locale.getDefault());
    private int modelType = PickModelTypeDialog.GRID;
    private HashSet<String> users;
    private HashMap<String, Double> profiles;
    private WorkloadGenerator loadConfiguration = null;
    private int edgeCount = 0;
    private int vertexCount = 0;
    private int iconCount = 0;
    private MainWindow mainWindow = null; 
    private boolean shouldPrintDirectConnections = false;
    private boolean shouldPrintIndirectConnections = false;
    private boolean shouldPrintSchedulableNodes = true;
    private Vertex copiedIcon = null;
    private int vertexType = -1;
    private HashSet<VirtualMachine> virtualMachines = null;

    public DesenhoGrade(final int w, final int h) {
        super(true, true, true, false);
        this.setSize(w, h);

        this.users = new HashSet<>(0);
        this.users.add("user1");

        this.profiles = new HashMap<>(0);
        this.profiles.put("user1", DesenhoGrade.FULL_CAPACITY);

    }

    private static Image getImage(final String name) {
        return new ImageIcon(DesenhoGrade.getResource(name)).getImage();
    }

    private static URL getResource(final String name) {
        return MainWindow.class.getResource(name);
    }

    public int getModelType() {
        return this.modelType;
    }

    public void setModelType(final int modelType) {
        this.modelType = modelType;
    }

    public void setMainWindow(final MainWindow janelaPrincipal) {
        this.mainWindow = janelaPrincipal;
        this.initTexts();
    }

    //utilizado para inserir novo valor nas Strings dos componentes
    private void initTexts() {
        this.setPopupButtonText(
                this.translate("Remove"),
                this.translate("Copy"),
                this.translate("Turn Over"),
                this.translate("Paste"));
        this.setErrorText(
                this.translate("You must click an icon."),
                this.translate("WARNING"));
    }

    private String translate(final String text) {
        return this.translator.getString(text);
    }

    public HashSet<VirtualMachine> getVirtualMachines() {
        return this.virtualMachines;
    }

    public void setVirtualMachines(final HashSet<VirtualMachine> virtualMachines) {
        this.virtualMachines = virtualMachines;
    }

    public void setShouldPrintDirectConnections(final boolean should) {
        this.shouldPrintDirectConnections = should;
    }

    public void setShouldPrintIndirectConnections(final boolean should) {
        this.shouldPrintIndirectConnections = should;
    }

    public void setShouldPrintSchedulableNodes(final boolean should) {
        this.shouldPrintSchedulableNodes = should;
    }

    public HashMap<String, Double> getProfiles() {
        return this.profiles;
    }

    public void setProfiles(final HashMap<String, Double> profiles) {
        this.profiles = profiles;
    }

    public HashSet<String> getUsuarios() {
        return this.users;
    }

    public void setUsers(final HashSet<String> users) {
        this.users = users;
    }

    public WorkloadGenerator getLoadConfiguration() {
        return this.loadConfiguration;
    }

    public void setLoadConfiguration(final WorkloadGenerator loadConfiguration) {
        this.loadConfiguration = loadConfiguration;
    }

    @Override
    public void mouseEntered(final MouseEvent me) {
        this.repaint();
    }

    @Override
    public void mouseExited(final MouseEvent me) {
        this.repaint();
    }

    @Override
    public void botaoPainelActionPerformed(final ActionEvent evt) {
        if (this.copiedIcon == null) {
            return;
        }

        final var copy =
                ((GridItem) this.copiedIcon).makeCopy(this.getPosicaoMouseX(),
                        this.getPosicaoMouseY(), this.iconCount,
                        this.vertexCount);
        this.vertices.add((Vertex) copy);
        copy.getId();
        this.iconCount++;
        this.vertexCount++;
        this.selectedIcons.add((Icon) copy);
        this.mainWindow.modificar();
        this.setLabelAtributos(copy);
        this.repaint();
    }

    @Override
    public void botaoVerticeActionPerformed(final ActionEvent evt) {
        //Não copia conexão de rede
        if (this.selectedIcons.isEmpty()) {
            final String text = "WARNING";
            JOptionPane.showMessageDialog(null, this.translate(
                            "No icon selected."),
                    this.translate(text),
                    JOptionPane.WARNING_MESSAGE);
        } else if (this.selectedIcons.size() == 1) {
            final Icon item = this.selectedIcons.iterator().next();
            if (item instanceof Vertex) {
                this.copiedIcon = (Vertex) item;
                this.generalPopup.getComponent(0).setEnabled(true);
            } else {
                this.copiedIcon = null;
            }
        }
        if (this.copiedIcon == null) {
            this.generalPopup.getComponent(0).setEnabled(false);
        }
    }

    @Override
    public void botaoArestaActionPerformed(final ActionEvent evt) {
        if (this.selectedIcons.size() == 1) {
            final Link link = (Link) this.selectedIcons.iterator().next();
            this.selectedIcons.remove(link);
            link.setSelected(false);
            final Link temp = link.makeCopy(0, 0, this.iconCount,
                    this.edgeCount);
            this.edgeCount++;
            this.iconCount++;
            temp.setPosition(link.getDestination(), link.getSource());
            ((GridItem) temp.getSource()).getOutboundConnections().add(temp);
            ((GridItem) temp.getDestination()).getOutboundConnections().add(temp);
            this.selectedIcons.add(temp);
            this.edges.add(temp);
            this.mainWindow.appendNotificacao(this.translate("Network " +
                                                             "connection " +
                                                             "added."));
            this.mainWindow.modificar();
            this.setLabelAtributos(temp);
        }
    }

    @Override
    public void botaoIconeActionPerformed(final ActionEvent evt) {
        if (this.selectedIcons.isEmpty()) {
            JOptionPane.showMessageDialog(null, this.translate("No icon " +
                                                               "selected" +
                                                               "."),
                    this.translate("WARNING"),
                    JOptionPane.WARNING_MESSAGE);
        } else {
            final int opcao = JOptionPane.showConfirmDialog(null,
                    this.translate("Remove this icon?"),
                    this.translate("Remove"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (opcao == JOptionPane.YES_OPTION) {
                for (final Icon iconeRemover : this.selectedIcons) {
                    if (iconeRemover instanceof Edge) {
                        final GridItem or =
                                (GridItem) ((Edge) iconeRemover).getSource();
                        or.getOutboundConnections().remove((GridItem) iconeRemover);
                        final GridItem de =
                                (GridItem) ((Edge) iconeRemover).getDestination();
                        de.getInboundConnections().remove((GridItem) iconeRemover);
                        ((GridItem) iconeRemover).getId();
                        this.edges.remove((Edge) iconeRemover);
                        this.mainWindow.modificar();
                    } else {
                        final int cont = 0;
                        //Remover dados das conexoes q entram
                        Set<GridItem> listanos =
                                ((GridItem) iconeRemover).getInboundConnections();
                        for (final GridItem I : listanos) {
                            this.edges.remove((Edge) I);
                            I.getId();
                        }
                        //Remover dados das conexoes q saem
                        listanos =
                                ((GridItem) iconeRemover).getOutboundConnections();
                        for (final GridItem I : listanos) {
                            this.edges.remove((Edge) I);
                            I.getId();
                        }
                        ((GridItem) iconeRemover).getId();
                        this.vertices.remove((Vertex) iconeRemover);
                        this.mainWindow.modificar();
                    }
                }
                this.repaint();
            }
        }
    }

    @Override
    public void adicionarAresta(final Vertex Origem, final Vertex Destino) {
        final Link link = new Link(Origem, Destino, this.edgeCount,
                this.iconCount);
        ((GridItem) Origem).getOutboundConnections().add(link);
        ((GridItem) Destino).getInboundConnections().add(link);
        this.edgeCount++;
        this.iconCount++;
        this.edges.add(link);
        for (final Icon icon : this.selectedIcons) {
            icon.setSelected(false);
        }
        this.selectedIcons.clear();
        this.selectedIcons.add(link);
        this.mainWindow.appendNotificacao(this.translate("Network " +
                                                         "connection" +
                                                         " " +
                                                         "added."));
        this.mainWindow.modificar();
        this.setLabelAtributos(link);
    }

    @Override
    public void showActionIcon(final MouseEvent me, final Icon icon) {
        this.mainWindow.modificar();
        if (icon instanceof Machine || icon instanceof Cluster) {
            this.mainWindow.getjPanelConfiguracao().setIcone((GridItem) icon
                    , this.users, this.modelType);
            JOptionPane.showMessageDialog(
                    this.mainWindow,
                    this.mainWindow.getjPanelConfiguracao(),
                    this.mainWindow.getjPanelConfiguracao().getTitle(),
                    JOptionPane.PLAIN_MESSAGE);
        } else {
            this.mainWindow.getjPanelConfiguracao().setIcone((GridItem) icon);
            JOptionPane.showMessageDialog(
                    this.mainWindow,
                    this.mainWindow.getjPanelConfiguracao(),
                    this.mainWindow.getjPanelConfiguracao().getTitle(),
                    JOptionPane.PLAIN_MESSAGE);
        }
        this.setLabelAtributos((GridItem) icon);
    }

    @Override
    public void showSelectionIcon(final MouseEvent me, final Icon icon) {
        this.setLabelAtributos((GridItem) icon);
    }

    @Override
    public void adicionarVertice(final int x, final int y) {
        GridItem vertice = null;
        switch (this.vertexType) {
            case DesenhoGrade.MACHINE -> {
                vertice = new Machine(x, y, this.vertexCount, this.iconCount,
                        0.0);
                vertice.getId();
                this.mainWindow.appendNotificacao(this.translate(
                        "Machine icon added."));
            }
            case DesenhoGrade.CLUSTER -> {
                vertice = new Cluster(x, y,
                        this.vertexCount, this.iconCount, 0.0);
                vertice.getId();
                this.mainWindow.appendNotificacao(this.translate(
                        "Cluster icon added."));
            }
            case DesenhoGrade.INTERNET -> {
                vertice = new Internet(x, y, this.vertexCount, this.iconCount);
                vertice.getId();
                this.mainWindow.appendNotificacao(this.translate(
                        "Internet icon added."));
            }
        }
        if (vertice != null) {
            this.vertices.add((Vertex) vertice);
            this.vertexCount++;
            this.iconCount++;
            this.selectedIcons.add((Icon) vertice);
            this.mainWindow.modificar();
            this.setLabelAtributos(vertice);
        }
    }

    private void setLabelAtributos(final GridItem icon) {
        final StringBuilder text = new StringBuilder("<html>");
        text.append(icon.makeDescription(this.translator));
        if (this.shouldPrintDirectConnections && icon instanceof Vertex) {
            text.append("<br>").append(this.translate("Output " +
                                                      "Connection:"));
            for (final GridItem i : icon.getOutboundConnections()) {
                final GridItem saida = (GridItem) ((Edge) i).getDestination();
                text.append("<br>").append(saida.getId().getName());
            }
            text.append("<br>").append(this.translate("Input " +
                                                      "Connection:"));
            for (final GridItem i : icon.getInboundConnections()) {
                final GridItem entrada = (GridItem) ((Edge) i).getSource();
                text.append("<br>").append(entrada.getId().getName());
            }
        }
        if (this.shouldPrintDirectConnections && icon instanceof Edge) {
            for (final GridItem i : icon.getInboundConnections()) {
                text.append("<br>").append(this.translate("Source " +
                                                          "Node:")).append(" "
                ).append(i.getInboundConnections());
            }
            for (final GridItem i : icon.getInboundConnections()) {
                text.append("<br>").append(this.translate("Destination" +
                                                          " " +
                                                          "Node:")).append(" ").append(i.getOutboundConnections());
            }
        }
        if (this.shouldPrintIndirectConnections && icon instanceof final Machine I) {
            final Set<GridItem> listaEntrada = I.connectedInboundNodes();
            final Set<GridItem> listaSaida = I.connectedOutboundNodes();
            text.append("<br>").append(this.translate("Output Nodes " +
                                                      "Indirectly " +
                                                      "Connected:"));
            for (final GridItem i : listaSaida) {
                text.append("<br>").append(i.getId().getGlobalId());
            }
            text.append("<br>").append(this.translate("Input Nodes " +
                                                      "Indirectly " +
                                                      "Connected:"));
            for (final GridItem i : listaEntrada) {
                text.append("<br>").append(i.getId().getGlobalId());
            }
        }
        if (this.shouldPrintSchedulableNodes && icon instanceof final Machine I) {
            text.append("<br>").append(this.translate("Schedulable " +
                                                      "Nodes:"));
            for (final GridItem i : I.connectedSchedulableNodes()) {
                text.append("<br>").append(i.getId().getGlobalId());
            }
            if (I.isMaster()) {
                final List<GridItem> escravos = ((Machine) icon).getSlaves();
                text.append("<br>").append(this.translate("Slave " +
                                                          "Nodes:"));
                for (final GridItem i : escravos) {
                    text.append("<br>").append(i.getId().getName());
                }
            }
        }
        text.append("</html>");
        this.mainWindow.setSelectedIcon(icon, text.toString());
    }

    public String makeDescriptiveModel() {
        
        final StringBuilder saida = new StringBuilder();
        for (final Icon icon : this.vertices) {
            if (icon instanceof final Machine I) {
                saida.append(String.format("MAQ %s %f %f ",
                        I.getId().getName(), I.getComputationalPower(),
                        I.getLoadFactor()));
                if (((Machine) icon).isMaster()) {
                    saida.append(String.format("MESTRE %s LMAQ".formatted(I.getSchedulingAlgorithm())));
                    final List<GridItem> lista =
                            ((Machine) icon).getSlaves();
                    for (final GridItem slv : lista) {
                        if (this.vertices.contains((Vertex) slv)) {
                            saida.append(" ").append(slv.getId().getName());
                        }
                    }
                } else {
                    saida.append("ESCRAVO");
                }
                saida.append("\n");
            }
        }
        for (final Icon icon : this.vertices) {
            if (icon instanceof final Cluster I) {
                saida.append(String.format("CLUSTER %s %d %f %f %f %s\n",
                        I.getId().getName(), I.getSlaveCount(),
                        I.getComputationalPower(), I.getBandwidth(),
                        I.getLatency(), I.getSchedulingAlgorithm()));
            }
        }
        for (final Icon icon : this.vertices) {
            if (icon instanceof final Internet I) {
                saida.append(String.format("INET %s %f %f %f\n",
                        I.getId().getName(), I.getBandwidth(), I.getLatency(),
                        I.getLoadFactor()));
            }
        }
        for (final Edge icon : this.edges) {
            final Link I = (Link) icon;
            saida.append(String.format("REDE %s %f %f %f CONECTA",
                    I.getId().getName(), I.getBandwidth(), I.getLatency(),
                    I.getLoadFactor()));
            saida.append(" ").append(((GridItem) icon.getSource()).getId().getName());
            saida.append(" ").append(((GridItem) icon.getDestination()).getId().getName());
            saida.append("\n");
        }
        saida.append("CARGA");
        if (this.loadConfiguration != null) {
            switch (this.loadConfiguration.getType()) {
                case RANDOM ->
                        saida.append(" RANDOM\n").append(this.loadConfiguration.formatForIconicModel()).append("\n");
                case PER_NODE ->
                        saida.append(" MAQUINA\n").append(this.loadConfiguration.formatForIconicModel()).append("\n");
                case TRACE ->
                        saida.append(" TRACE\n").append(this.loadConfiguration.formatForIconicModel()).append("\n");
            }
        }
        return saida.toString();
    }

    /**
     * Transforma os icones da area de desenho em um Document xml dom
     */
    public Document getGrade() {
        final var xml = new IconicoXML(this.modelType);
        xml.addUsers(this.users, this.profiles);

        for (final Vertex vertice : this.vertices) {
            if (vertice instanceof final Machine I) {
                final var slaves =
                        new ArrayList<Integer>(I.getSlaves().size());
                for (final GridItem slv : I.getSlaves()) {
                    if (this.vertices.contains((Vertex) slv)) {
                        slaves.add(slv.getId().getGlobalId());
                    }
                }
                if (this.modelType == PickModelTypeDialog.GRID) {
                    xml.addMachine(I.getX(), I.getY(),
                            I.getId().getLocalId(), I.getId().getGlobalId(),
                            I.getId().getName(),
                            I.getComputationalPower(), I.getLoadFactor(),
                            I.getSchedulingAlgorithm(), I.getOwner(),
                            I.getCoreCount(), I.getRam(),
                            I.getHardDisk(),
                            I.isMaster(),
                            slaves,
                            I.getEnergyConsumption());
                } else if (this.modelType == PickModelTypeDialog.IAAS) {
                    xml.addMachineIaaS(I.getX(), I.getY(),
                            I.getId().getLocalId(), I.getId().getGlobalId(),
                            I.getId().getName(),
                            I.getComputationalPower(), I.getLoadFactor(),
                            I.getVmmAllocationPolicy(), I.getSchedulingAlgorithm(),
                            I.getOwner(), I.getCoreCount(),
                            I.getRam(),
                            I.getHardDisk(), I.getCostPerProcessing(),
                            I.getCostPerMemory(), I.getCostPerDisk(),
                            I.isMaster(), slaves);
                }
            } else if (vertice instanceof Cluster) {
                if (this.modelType == PickModelTypeDialog.GRID) {
                    final Cluster I = (Cluster) vertice;
                    xml.addCluster(I.getX(), I.getY(),
                            I.getId().getLocalId(), I.getId().getGlobalId(),
                            I.getId().getName(),
                            I.getSlaveCount(), I.getComputationalPower(),
                            I.getCoreCount(),
                            I.getRam(), I.getHardDisk(),
                            I.getBandwidth(), I.getLatency(),
                            I.getSchedulingAlgorithm(), I.getOwner(),
                            I.isMaster(),
                            I.getEnergyConsumption());
                } else if (this.modelType == PickModelTypeDialog.IAAS) {
                    final Cluster I = (Cluster) vertice;
                    xml.addClusterIaaS(I.getX(), I.getY(),
                            I.getId().getLocalId(), I.getId().getGlobalId(),
                            I.getId().getName(), I.getSlaveCount(),
                            I.getComputationalPower(), I.getCoreCount(),
                            I.getRam(), I.getHardDisk(),
                            I.getBandwidth(), I.getLatency(),
                            I.getSchedulingAlgorithm(), I.getVmmAllocationPolicy(),
                            I.getCostPerProcessing(),
                            I.getCostPerMemory(), I.getCostPerDisk(),
                            I.getOwner(), I.isMaster());
                }
            } else if (vertice instanceof final Internet I) {
                xml.addInternet(
                        I.getX(), I.getY(),
                        I.getId().getLocalId(), I.getId().getGlobalId(),
                        I.getId().getName(),
                        I.getBandwidth(), I.getLoadFactor(), I.getLatency());
            }
        }
        for (final Edge link : this.edges) {
            final Link l = (Link) link;
            xml.addLink(l.getSource().getX(), l.getSource().getY(),
                    l.getDestination().getX(), l.getDestination().getY(),
                    l.getId().getLocalId(), l.getId().getGlobalId(),
                    l.getId().getName(),
                    l.getBandwidth(), l.getLoadFactor(), l.getLatency(),
                    ((GridItem) l.getSource()).getId().getGlobalId(),
                    ((GridItem) l.getDestination()).getId().getGlobalId());
        }
        //trecho de escrita das máquinas virtuais
        if (this.virtualMachines != null) {
            for (final VirtualMachine vm : this.virtualMachines) {
                xml.addVirtualMachines(vm.getName(), vm.getOwner(),
                        vm.getVMM(), vm.getCoreCount(),
                        vm.getAllocatedMemory(), vm.getAllocatedDisk(),
                        vm.getOperatingSystem());
            }
        }

        //configurar carga
        if (this.loadConfiguration != null) {
            if (this.loadConfiguration instanceof final GlobalWorkloadGenerator cr) {
                xml.setLoadRandom(cr.getTaskCount(), cr.getTaskCreationTime(),
                        cr.getComputationMaximum(), cr.getComputationAverage(),
                        cr.getComputationMinimum(), cr.getComputationProbability(),
                        cr.getCommunicationMaximum(), cr.getCommunicationAverage(),
                        cr.getCommunicationMinimum(), cr.getCommunicationProbability());
            } else if (this.loadConfiguration.getType() == WorkloadGeneratorType.PER_NODE) {
                for (final WorkloadGenerator node :
                        ((CollectionWorkloadGenerator) this.loadConfiguration).getList()) {
                    final PerNodeWorkloadGenerator no = (PerNodeWorkloadGenerator) node;
                    xml.addLoadNo(no.getApplication(), no.getOwner(),
                            no.getSchedulerId(), no.getTaskCount(),
                            no.getComputationMaximum(), no.getComputationMinimum(),
                            no.getCommunicationMaximum(), no.getCommunicationMinimum());
                }
            } else if (this.loadConfiguration.getType() == WorkloadGeneratorType.TRACE) {
                final TraceFileWorkloadGenerator trace = (TraceFileWorkloadGenerator) this.loadConfiguration;
                xml.setLoadTrace(trace.getTraceFile().toString(),
                        trace.getTaskCount(),
                        trace.getTraceType());
            }
        }

        return xml.getDescricao();
    }

    public void setGrid(final Document document) {
        //Realiza leitura dos usuários/proprietários do modelo
        this.users = IconicoXML.newSetUsers(document);
        this.virtualMachines = IconicoXML.newListVirtualMachines(document);
        this.modelType = this.getModelType(document);
        this.profiles = IconicoXML.newListPerfil(document);

        //Realiza leitura dos icones
        IconicoXML.newGrade(document, this.vertices, this.edges);
        //Realiza leitura da configuração de carga do modelo
        this.loadConfiguration = IconicoXML.newGerarCarga(document);

        this.updateVertexAndEdgeCount();
        this.repaint();
    }

    private int getModelType(final Document doc) {
        final var sys = (Element) doc.getElementsByTagName("system").item(0);
        return switch (sys.getAttribute("version")) {
            case "2.1" -> PickModelTypeDialog.GRID;
            case "2.2" -> PickModelTypeDialog.IAAS;
            case "2.3" -> PickModelTypeDialog.PAAS;
            default -> this.modelType;
        };
    }

    private void updateVertexAndEdgeCount() {

        for (final var icon : this.edges) {
            final var i = (GridItem) icon;
            if (this.edgeCount < i.getId().getLocalId()) {
                this.edgeCount = i.getId().getLocalId();
            }
            if (this.iconCount < i.getId().getGlobalId()) {
                this.iconCount = i.getId().getGlobalId();
            }
        }

        for (final var icon : this.vertices) {
            final var i = (GridItem) icon;
            if (this.vertexCount < i.getId().getLocalId()) {
                this.vertexCount = i.getId().getLocalId();
            }
            if (this.iconCount < i.getId().getGlobalId()) {
                this.iconCount = i.getId().getGlobalId();
            }
        }

        this.iconCount++;
        this.vertexCount++;
        this.edgeCount++;
    }

    public BufferedImage createImage() {
        final int greatestX = this.findGreatestX();
        final int greatestY = this.findGreatestY();

        final var image = new BufferedImage(
                greatestX + DesenhoGrade.SOME_OFFSET,
                greatestY + DesenhoGrade.SOME_OFFSET,
                BufferedImage.TYPE_INT_RGB
        );

        final var g = (Graphics2D) image.getGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setColor(DesenhoGrade.WHITE);
        g.fillRect(0, 0, greatestX + DesenhoGrade.SOME_OFFSET,
                greatestY + DesenhoGrade.SOME_OFFSET);

        g.setColor(DesenhoGrade.ALMOST_WHITE);
        final var increment = this.getUnit().getIncrement();
        if (this.isGridOn()) {
            for (int w = 0; w <= greatestX + DesenhoGrade.SOME_OFFSET;
                 w += increment) {
                g.drawLine(w, 0, w, greatestY + DesenhoGrade.SOME_OFFSET);
            }
            for (int h = 0; h <= greatestY + DesenhoGrade.SOME_OFFSET;
                 h += increment) {
                g.drawLine(0, h, greatestX + DesenhoGrade.SOME_OFFSET, h);
            }
        }

        this.allIcons().forEach(icon -> icon.draw(g));

        return image;
    }

    private int findGreatestX() {
        return this.findGreatestCoord(Icon::getX);
    }

    private int findGreatestY() {
        return this.findGreatestCoord(Icon::getY);
    }

    private Stream<Icon> allIcons() {
        return Stream.concat(this.edges.stream(), this.vertices.stream());
    }

    private int findGreatestCoord(final Function<? super Icon, Integer> getCoord) {
        return this.vertices.stream()
                .mapToInt(getCoord::apply)
                .max()
                .orElse(0);
    }

    /**
     * Metodo publico para efetuar a copia dos valores de uma conexão de rede
     * especifica informada pelo usuário para as demais conexões de rede.
     */
    public void matchNetwork() {
        if (this.selectedIcons.size() != 1) {
            JOptionPane.showMessageDialog(null,
                    this.translate("Please select a network icon"),
                    this.translate("WARNING"),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        final Link link = (Link) this.selectedIcons.iterator().next();
        final double bandwidth = link.getBandwidth();
        final double occupationToll = link.getLoadFactor();
        final double latency = link.getLatency();

        for (final Edge e : this.edges) {
            final Link otherLink = (Link) e;
            otherLink.setBandwidth(bandwidth);
            otherLink.setLoadFactor(occupationToll);
            otherLink.setLatency(latency);
        }
    }

    /**
     * Organizes the icons of the DrawingArea in a rectangular grid-like way.
     */
    public void iconArrange() {
        //Distancia entre os icones
        final int size = 100;
        //posição inicial
        int linha = size, coluna = size;
        int columnPos = 0;
        final int totalVertice = this.vertices.size();
        //número de elementos por linha
        final int columnNum = ((int) Math.sqrt(totalVertice)) + 1;
        //Organiza os icones na tela
        for (final Vertex icone : this.vertices) {
            icone.setPosition(coluna, linha);
            //busca por arestas conectadas ao vertice
            coluna += size;
            columnPos++;
            if (columnPos == columnNum) {
                columnPos = 0;
                coluna = size;
                linha += size;
            }
        }
    }

    public void setTranslator(final ResourceBundle translator) {
        this.translator = translator;
        this.initTexts();
    }

    public List<String> getNosEscalonadores() {
        final List<String> machines = new ArrayList<>(0);
        for (final var icon : this.vertices) {
            if (icon instanceof Machine m && m.isMaster()) {
                machines.add(m.getId().getName());
            }
            if (icon instanceof Cluster c && c.isMaster()) {
                machines.add(c.getId().getName());
            }
        }
        return machines;
    }

    public void setIconeSelecionado(final Integer object) {
        if (object == null) {
            this.setIsDrawingEdge(false);
            this.setAddVertice(false);
            this.setCursor(this.normalCursor);
            return;
        }

        if (object == DesenhoGrade.NETWORK) {
            this.setIsDrawingEdge(true);
            this.setAddVertice(false);
            this.setCursor(this.crossHairCursor);
        } else {
            this.vertexType = object;
            this.setIsDrawingEdge(false);
            this.setAddVertice(true);
            this.setCursor(this.crossHairCursor);
        }
    }


    @Override
    public Dimension getMaximumSize() {
        return this.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return this.getSize();
    }
}