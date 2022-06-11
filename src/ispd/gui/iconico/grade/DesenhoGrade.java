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
 * DesenhoGrade.java
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
package ispd.gui.iconico.grade;

import ispd.utils.ValidaValores;
import ispd.arquivo.xml.IconicoXML;
import ispd.gui.EscolherClasse;
import ispd.gui.JPrincipal;
import ispd.gui.iconico.AreaDesenho;
import ispd.gui.iconico.Aresta;
import ispd.gui.iconico.Icone;
import ispd.gui.iconico.Vertice;
import ispd.motor.carga.CargaList;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author denison
 */
public class DesenhoGrade extends AreaDesenho {

    protected static Image IMACHINE;
    protected static Image ICLUSTER;
    protected static Image IINTERNET;
    protected static Image IVERDE;
    protected static Image IVERMELHO;
    public static final int MACHINE = 1;
    public static final int NETWORK = 2;
    public static final int CLUSTER = 3;
    public static final int INTERNET = 4;
    private ResourceBundle palavras;
    private int tipoModelo; //tipo de serviço que o modelo vai simulate (GRID, IAAS ou PAAS)

    public int getTipoModelo() {
        return tipoModelo;
    }

    public void setTipoModelo(int tipoModelo) {
        this.tipoModelo = tipoModelo;
    }
    //Objetos principais da classe
    /**
     * Lista com os usuarios/proprietarios do modelo criado
     */
    private HashSet<String> usuarios;
    private HashMap<String,Double> perfis;
    /**
     * Objeto para Manipular as cargas
     */
    private GerarCarga cargasConfiguracao;
    /**
     * número de icones excluindo os links
     */
    private int numArestas;
    /**
     * número de links
     */
    private int numVertices;
    /**
     * número total de icones
     */
    private int numIcones;
    //Objetos advindo da classe JanelaPrincipal
    private JPrincipal janelaPrincipal;
    //Objetos do cursor
    private Cursor hourglassCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    private Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    //Objetos para Selecionar texto na Area Lateral
    private boolean imprimeNosConectados;
    private boolean imprimeNosIndiretos;
    private boolean imprimeNosEscalonaveis;
    //Obejtos para copiar um icone
    private Vertice iconeCopiado;
    private int tipoDeVertice;
    private HashSet<VirtualMachine> maquinasVirtuais;

    public DesenhoGrade(int w, int h) {
        super(true, true, true, false);
        //Utiliza o idioma do sistema como padrão
        criarImagens();
        Locale locale = Locale.getDefault();
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);
        this.setSize(w, h);
        this.numArestas = 0;
        this.numVertices = 0;
        this.numIcones = 0;
        this.tipoDeVertice = -1;
        usuarios = new HashSet<String>();
        usuarios.add("user1");
        perfis = new HashMap<String,Double>();
        perfis.put("user1",100.0);
        ValidaValores.removeTodosNomeIcone();
        cargasConfiguracao = null;
        imprimeNosConectados = false;
        imprimeNosIndiretos = false;
        imprimeNosEscalonaveis = true;
        maquinasVirtuais = null;
        this.tipoModelo = EscolherClasse.GRID;
    }

    public void setPaineis(JPrincipal janelaPrincipal) {
        this.janelaPrincipal = janelaPrincipal;
        this.initTexts();
    }

    public HashSet<VirtualMachine> getMaquinasVirtuais() {
        return maquinasVirtuais;
    }

    public void setMaquinasVirtuais(HashSet<VirtualMachine> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
    }

    //utilizado para inserir novo valor nas Strings dos componentes
    private void initTexts() {
        setPopupButtonText(
                palavras.getString("Remove"),
                palavras.getString("Copy"),
                palavras.getString("Turn Over"),
                palavras.getString("Paste"));
        setErrorText(
                palavras.getString("You must click an icon."),
                palavras.getString("WARNING"));
    }

    @Override
    public void adicionarAresta(Vertice origem, Vertice destino) {
        Link link = new Link(origem, destino, numArestas, numIcones);
        ((ItemGrade) origem).getConexoesSaida().add(link);
        ((ItemGrade) destino).getConexoesEntrada().add(link);
        numArestas++;
        numIcones++;
        ValidaValores.addNomeIcone(link.getId().getNome());
        arestas.add(link);
        for (Icone icon : selecionados) {
            icon.setSelected(false);
        }
        selecionados.clear();
        selecionados.add(link);
        this.janelaPrincipal.appendNotificacao(palavras.getString("Network connection added."));
        this.janelaPrincipal.modificar();
        this.setLabelAtributos(link);
    }

    @Override
    public void adicionarVertice(int x, int y) {
        ItemGrade vertice = null;
        switch (tipoDeVertice) {
            case MACHINE:
                vertice = new Machine(x, y, numVertices, numIcones, 0.0);
                ValidaValores.addNomeIcone(vertice.getId().getNome());
                this.janelaPrincipal.appendNotificacao(palavras.getString("Machine icon added."));
                break;
            case CLUSTER:
                vertice = new Cluster(x, y, numVertices, numIcones, 0.0);
                ValidaValores.addNomeIcone(vertice.getId().getNome());
                this.janelaPrincipal.appendNotificacao(palavras.getString("Cluster icon added."));
                break;
            case INTERNET:
                vertice = new Internet(x, y, numVertices, numIcones);
                ValidaValores.addNomeIcone(vertice.getId().getNome());
                this.janelaPrincipal.appendNotificacao(palavras.getString("Internet icon added."));
                break;
        }
        if (vertice != null) {
            vertices.add((Vertice) vertice);
            numVertices++;
            numIcones++;
            selecionados.add((Icone) vertice);
            this.janelaPrincipal.modificar();
            this.setLabelAtributos(vertice);
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return this.getSize();
    }

    public void setConectados(boolean imprimeNosConectados) {
        this.imprimeNosConectados = imprimeNosConectados;
    }

    public void setIndiretos(boolean imprimeNosIndiretos) {
        this.imprimeNosIndiretos = imprimeNosIndiretos;
    }

    public void setEscalonaveis(boolean imprimeNosEscalonaveis) {
        this.imprimeNosEscalonaveis = imprimeNosEscalonaveis;
    }

    public void setPerfil(HashMap<String,Double> perfil){
        this.perfis = perfil;
    }
    
    public HashMap<String,Double> getPerfil() {
        return perfis;
    }
    
    public HashSet<String> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(HashSet<String> usuarios) {
        this.usuarios = usuarios;
    }

    public GerarCarga getCargasConfiguracao() {
        return cargasConfiguracao;
    }

    public void setCargasConfiguracao(GerarCarga cargasConfiguracao) {
        this.cargasConfiguracao = cargasConfiguracao;
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        //janelaPrincipal.setSelectedIcon((ItemGrade) null, null);
        super.mouseClicked(me);
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent me) {
        repaint();
    }

    @Override
    public void botaoIconeActionPerformed(ActionEvent evt) {
        if (selecionados.isEmpty()) {
            JOptionPane.showMessageDialog(null, palavras.getString("No icon selected."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        } else {
            int opcao = JOptionPane.showConfirmDialog(null, palavras.getString("Remove this icon?"), palavras.getString("Remove"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opcao == JOptionPane.YES_OPTION) {
                for (Icone iconeRemover : selecionados) {
                    if (iconeRemover instanceof Aresta) {
                        ItemGrade or = (ItemGrade) ((Aresta) iconeRemover).getOrigem();
                        or.getConexoesSaida().remove((ItemGrade) iconeRemover);
                        ItemGrade de = (ItemGrade) ((Aresta) iconeRemover).getDestino();
                        de.getConexoesEntrada().remove((ItemGrade) iconeRemover);
                        ValidaValores.removeNomeIcone(((ItemGrade) iconeRemover).getId().getNome());
                        arestas.remove((Aresta) iconeRemover);
                        this.janelaPrincipal.modificar();
                    } else {
                        int cont = 0;
                        //Remover dados das conexoes q entram
                        Set<ItemGrade> listanos = ((ItemGrade) iconeRemover).getConexoesEntrada();
                        for (ItemGrade I : listanos) {
                            arestas.remove((Aresta) I);
                            ValidaValores.removeNomeIcone(I.getId().getNome());
                        }
                        //Remover dados das conexoes q saem
                        listanos = ((ItemGrade) iconeRemover).getConexoesSaida();
                        for (ItemGrade I : listanos) {
                            arestas.remove((Aresta) I);
                            ValidaValores.removeNomeIcone(I.getId().getNome());
                        }
                        ValidaValores.removeNomeIcone(((ItemGrade) iconeRemover).getId().getNome());
                        vertices.remove((Vertice) iconeRemover);
                        this.janelaPrincipal.modificar();
                    }
                }
                repaint();
            }
        }
    }

    @Override
    public void botaoVerticeActionPerformed(java.awt.event.ActionEvent evt) {
        //Não copia conexão de rede
        if (selecionados.isEmpty()) {
            JOptionPane.showMessageDialog(null, palavras.getString("No icon selected."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        } else if (selecionados.size() == 1) {
            Icone item = selecionados.iterator().next();
            if (item instanceof Vertice) {
                iconeCopiado = (Vertice) item;
                this.popupGeral.getComponent(0).setEnabled(true);
            } else {
                iconeCopiado = null;
            }
        }
        if (iconeCopiado == null) {
            this.popupGeral.getComponent(0).setEnabled(false);
        }
    }

    @Override
    public void botaoPainelActionPerformed(java.awt.event.ActionEvent evt) {
        if (iconeCopiado != null) {
            ItemGrade copy = ((ItemGrade) iconeCopiado).criarCopia(getPosicaoMouseX(), getPosicaoMouseY(), numIcones, numVertices);
            vertices.add((Vertice) copy);
            ValidaValores.addNomeIcone(copy.getId().getNome());
            numIcones++;
            numVertices++;
            selecionados.add((Icone) copy);
            this.janelaPrincipal.modificar();
            this.setLabelAtributos(copy);
            repaint();
        }
    }

    @Override
    public void botaoArestaActionPerformed(java.awt.event.ActionEvent evt) {
        if (!selecionados.isEmpty() && selecionados.size() == 1) {
            Link link = (Link) selecionados.iterator().next();
            selecionados.remove(link);
            link.setSelected(false);
            Link temp = link.criarCopia(0, 0, numIcones, numArestas);
            numArestas++;
            numIcones++;
            temp.setPosition(link.getDestino(), link.getOrigem());
            ((ItemGrade) temp.getOrigem()).getConexoesSaida().add(temp);
            ((ItemGrade) temp.getDestino()).getConexoesSaida().add(temp);
            selecionados.add(temp);
            arestas.add(temp);
            ValidaValores.addNomeIcone(temp.getId().getNome());
            this.janelaPrincipal.appendNotificacao(palavras.getString("Network connection added."));
            this.janelaPrincipal.modificar();
            this.setLabelAtributos(temp);
        }
    }

    @Override
    public String toString() {
        StringBuilder saida = new StringBuilder();
        for (Icone icon : vertices) {
            if (icon instanceof Machine) {
                Machine I = (Machine) icon;
                saida.append(String.format("MAQ %s %f %f ", I.getId().getNome(), I.getPoderComputacional(), I.getTaxaOcupacao()));
                if (((Machine) icon).isMestre()) {
                    saida.append(String.format("MESTRE " + I.getAlgoritmo() + " LMAQ"));
                    List<ItemGrade> lista = ((Machine) icon).getEscravos();
                    for (ItemGrade slv : lista) {
                        if (vertices.contains((Vertice) slv)) {
                            saida.append(" ").append(slv.getId().getNome());
                        }
                    }
                } else {
                    saida.append("ESCRAVO");
                }
                saida.append("\n");
            }
        }
        for (Icone icon : vertices) {
            if (icon instanceof Cluster) {
                Cluster I = (Cluster) icon;
                saida.append(String.format("CLUSTER %s %d %f %f %f %s\n", I.getId().getNome(), I.getNumeroEscravos(), I.getPoderComputacional(), I.getBanda(), I.getLatencia(), I.getAlgoritmo()));
            }
        }
        for (Icone icon : vertices) {
            if (icon instanceof Internet) {
                Internet I = (Internet) icon;
                saida.append(String.format("INET %s %f %f %f\n", I.getId().getNome(), I.getBanda(), I.getLatencia(), I.getTaxaOcupacao()));
            }
        }
        for (Aresta icon : arestas) {
            Link I = (Link) icon;
            saida.append(String.format("REDE %s %f %f %f CONECTA", I.getId().getNome(), I.getBanda(), I.getLatencia(), I.getTaxaOcupacao()));
            saida.append(" ").append(((ItemGrade) icon.getOrigem()).getId().getNome());
            saida.append(" ").append(((ItemGrade) icon.getDestino()).getId().getNome());
            saida.append("\n");
        }
        saida.append("CARGA");
        if (cargasConfiguracao != null) {
            switch (cargasConfiguracao.getTipo()) {
                case GerarCarga.RANDOM:
                    saida.append(" RANDOM\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
                case GerarCarga.FORNODE:
                    saida.append(" MAQUINA\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
                case GerarCarga.TRACE:
                    saida.append(" TRACE\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
            }
        }
        return saida.toString();
    }

    public void setLabelAtributos(ItemGrade icon) {
        String Texto = "<html>";
        Texto += icon.getAtributos(palavras);
        if (imprimeNosConectados && icon instanceof Vertice) {
            Texto = Texto + "<br>" + palavras.getString("Output Connection:");
            for (ItemGrade i : icon.getConexoesSaida()) {
                ItemGrade saida = (ItemGrade) ((Link) i).getDestino();
                Texto = Texto + "<br>" + saida.getId().getNome();
            }
            Texto = Texto + "<br>" + palavras.getString("Input Connection:");
            for (ItemGrade i : icon.getConexoesEntrada()) {
                ItemGrade entrada = (ItemGrade) ((Link) i).getOrigem();
                Texto = Texto + "<br>" + entrada.getId().getNome();
            }
        }
        if (imprimeNosConectados && icon instanceof Aresta) {
            for (ItemGrade i : icon.getConexoesEntrada()) {
                Texto = Texto + "<br>" + palavras.getString("Source Node:") + " " + i.getConexoesEntrada();
            }
            for (ItemGrade i : icon.getConexoesEntrada()) {
                Texto = Texto + "<br>" + palavras.getString("Destination Node:") + " " + i.getConexoesSaida();
            }
        }
        if (imprimeNosIndiretos && icon instanceof Machine) {
            Machine I = (Machine) icon;
            Set<ItemGrade> listaEntrada = I.getNosIndiretosEntrada();
            Set<ItemGrade> listaSaida = I.getNosIndiretosSaida();
            Texto = Texto + "<br>" + palavras.getString("Output Nodes Indirectly Connected:");
            for (ItemGrade i : listaSaida) {
                Texto = Texto + "<br>" + String.valueOf(i.getId().getIdGlobal());
            }
            Texto = Texto + "<br>" + palavras.getString("Input Nodes Indirectly Connected:");
            for (ItemGrade i : listaEntrada) {
                Texto = Texto + "<br>" + String.valueOf(i.getId().getIdGlobal());
            }
        }
        if (imprimeNosEscalonaveis && icon instanceof Machine) {
            Machine I = (Machine) icon;
            Texto = Texto + "<br>" + palavras.getString("Schedulable Nodes:");
            for (ItemGrade i : I.getNosEscalonaveis()) {
                Texto = Texto + "<br>" + String.valueOf(i.getId().getIdGlobal());
            }
            if (I.isMestre()) {
                List<ItemGrade> escravos = ((Machine) icon).getEscravos();
                Texto = Texto + "<br>" + palavras.getString("Slave Nodes:");
                for (ItemGrade i : escravos) {
                    Texto = Texto + "<br>" + i.getId().getNome();
                }
            }
        }
        Texto += "</html>";
        janelaPrincipal.setSelectedIcon(icon, Texto);
    }

    /**
     * Transforma os icones da area de desenho em um Document xml dom
     */
    public Document getGrade() {
        IconicoXML xml = new IconicoXML(tipoModelo);
        xml.addUsers(usuarios,perfis);
        for (Vertice vertice : vertices) {
            if (vertice instanceof Machine) {
                Machine I = (Machine) vertice;
                ArrayList<Integer> escravos = new ArrayList<Integer>();
                for (ItemGrade slv : I.getEscravos()) {
                    if (vertices.contains((Vertice) slv)) {
                        escravos.add(slv.getId().getIdGlobal());
                    }
                }
                if(tipoModelo == EscolherClasse.GRID){
                    xml.addMachine(I.getX(), I.getY(),
                            I.getId().getIdLocal(), I.getId().getIdGlobal(), I.getId().getNome(),
                            I.getPoderComputacional(), I.getTaxaOcupacao(), I.getAlgoritmo(), I.getProprietario(),
                            I.getNucleosProcessador(), I.getMemoriaRAM(), I.getDiscoRigido(),
                            I.isMestre(), escravos);
                }
                else if(tipoModelo == EscolherClasse.IAAS){
                    xml.addMachineIaaS(I.getX(), I.getY(), I.getId().getIdLocal(),I.getId().getIdGlobal(), I.getId().getNome(),
                            I.getPoderComputacional(), I.getTaxaOcupacao(),I.getVMMallocpolicy(), I.getAlgoritmo(), I.getProprietario(), I.getNucleosProcessador(), I.getMemoriaRAM(),
                            I.getDiscoRigido(), I.getCostperprocessing(), I.getCostpermemory(), I.getCostperdisk(), I.isMestre(), escravos);
                }
            } else if (vertice instanceof Cluster) {
                if(tipoModelo == EscolherClasse.GRID){
                    Cluster I = (Cluster) vertice;
                    xml.addCluster(I.getX(), I.getY(),
                            I.getId().getIdLocal(), I.getId().getIdGlobal(), I.getId().getNome(),
                            I.getNumeroEscravos(), I.getPoderComputacional(), I.getNucleosProcessador(),
                            I.getMemoriaRAM(), I.getDiscoRigido(),
                            I.getBanda(), I.getLatencia(),
                            I.getAlgoritmo(), I.getProprietario(), I.isMestre());
                }
                else if(tipoModelo == EscolherClasse.IAAS){
                    Cluster I = (Cluster) vertice;
                    xml.addClusterIaaS(I.getX(), I.getY(),
                            I.getId().getIdLocal(), I.getId().getIdGlobal(),
                            I.getId().getNome(), I.getNumeroEscravos(),
                            I.getPoderComputacional(), I.getNucleosProcessador(),
                            I.getMemoriaRAM(), I.getDiscoRigido(),
                            I.getBanda(), I.getLatencia(),
                            I.getAlgoritmo(),I.getVMMallocpolicy(), I.getCostperprocessing(),
                            I.getCostpermemory(), I.getCostperdisk(),
                            I.getProprietario(), I.isMestre());
                }
/* TODO: Para GRID
                xml.addMachine(I.getX(), I.getY(),
                        I.getId().getIdLocal(), I.getId().getIdGlobal(), I.getId().getNome(),
                        I.getPoderComputacional(), I.getTaxaOcupacao(), I.getAlgoritmo(), I.getProprietario(),
                        I.getNucleosProcessador(), I.getMemoriaRAM(), I.getDiscoRigido(),
                        I.isMestre(), escravos, I.getConsumoEnergia() );
            } else if (vertice instanceof Cluster) {
                Cluster I = (Cluster) vertice;
                xml.addCluster(I.getX(), I.getY(),
                        I.getId().getIdLocal(), I.getId().getIdGlobal(), I.getId().getNome(),
                        I.getNumeroEscravos(), I.getPoderComputacional(), I.getNucleosProcessador(),
                        I.getMemoriaRAM(), I.getDiscoRigido(),
                        I.getBanda(), I.getLatencia(),
                        I.getAlgoritmo(), I.getProprietario(), I.isMestre(), I.getConsumoEnergia() );
*/
            } else if (vertice instanceof Internet) {
                Internet I = (Internet) vertice;
                xml.addInternet(
                        I.getX(), I.getY(),
                        I.getId().getIdLocal(), I.getId().getIdGlobal(), I.getId().getNome(),
                        I.getBanda(), I.getTaxaOcupacao(), I.getLatencia());
            }
        }
        for (Aresta link : arestas) {
            Link I = (Link) link;
            xml.addLink(I.getOrigem().getX(), I.getOrigem().getY(), I.getDestino().getX(), I.getDestino().getY(),
                    I.getId().getIdLocal(), I.getId().getIdGlobal(), I.getId().getNome(),
                    I.getBanda(), I.getTaxaOcupacao(), I.getLatencia(),
                    ((ItemGrade) I.getOrigem()).getId().getIdGlobal(), ((ItemGrade) I.getDestino()).getId().getIdGlobal());
        }
        //trecho de escrita das máquinas virtuais
        if(maquinasVirtuais != null){
            for(VirtualMachine vm : maquinasVirtuais){
                VirtualMachine I = vm;
                xml.addVirtualMachines(I.getNome(),I.getProprietario(),I.getVMM(), I.getPoderComputacional(),
                        I.getMemoriaAlocada(),I.getDiscoAlocado(),I.getOS());
            }
        }
        
        //configurar carga
        if (cargasConfiguracao != null) {
            if (cargasConfiguracao instanceof CargaRandom) {
                CargaRandom cr = (CargaRandom) cargasConfiguracao;
                xml.setLoadRandom(cr.getNumeroTarefas(), cr.getTimeToArrival(),
                        cr.getMaxComputacao(), cr.getAverageComputacao(), cr.getMinComputacao(), cr.getProbabilityComputacao(),
                        cr.getMaxComunicacao(), cr.getAverageComunicacao(), cr.getMinComunicacao(), cr.getProbabilityComunicacao());
            } else if (cargasConfiguracao.getTipo() == GerarCarga.FORNODE) {
                for (GerarCarga node : ((CargaList)cargasConfiguracao).getList()) {
                    CargaForNode no = (CargaForNode) node;
                    xml.addLoadNo(no.getAplicacao(), no.getProprietario(), no.getEscalonador(), no.getNumeroTarefas(),
                            no.getMaxComputacao(), no.getMinComputacao(),
                            no.getMaxComunicacao(), no.getMinComunicacao());
                }
            } else if (cargasConfiguracao.getTipo() == GerarCarga.TRACE) {
                CargaTrace trace = (CargaTrace) cargasConfiguracao;
                xml.setLoadTrace(trace.getFile().toString(), trace.getNumberTasks().toString(), trace.getTraceType().toString());
            }
        }
        return xml.getDescricao();
    }

    public BufferedImage createImage() {
        int maiorx = 0;
        int maiory = 0;
        for (Icone I : vertices) {
            if (I.getX() > maiorx) {
                maiorx = I.getX();
            }
            if (I.getY() > maiory) {
                maiory = I.getY();
            }
        }
        BufferedImage image = new BufferedImage(maiorx + 50, maiory + 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D gc = (Graphics2D) image.getGraphics();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setColor(new Color(255, 255, 255));
        gc.fillRect(0, 0, maiorx + 50, maiory + 50);
        gc.setColor(new Color(220, 220, 220));
        int INCH = Toolkit.getDefaultToolkit().getScreenResolution();
        int units = (int) (this.isMetric() ? (INCH / 2.54) : (INCH / 2));
        if (isGridOn()) {
            for (int _w = 0; _w
                    <= maiorx + 50; _w += units) {
                gc.drawLine(_w, 0, _w, maiory + 50);
            }
            for (int _h = 0; _h
                    <= maiory + 50; _h += units) {
                gc.drawLine(0, _h, maiorx + 50, _h);
            }
        }
        // Desenhamos todos os icones
        for (Icone I : arestas) {
            I.draw(gc);
        }
        for (Icone I : vertices) {
            I.draw(gc);
        }
        return image;
    }

    /**
     * Metodo publico para efetuar a copia dos valores de uma conexão de rede
     * especifica informada pelo usuário para as demais conexões de rede.
     */
    public void matchNetwork() {
        if (!selecionados.isEmpty() && selecionados.size() == 1) {
            Link link = (Link) selecionados.iterator().next();
            double banda, taxa, latencia;
            banda = link.getBanda();
            taxa = link.getTaxaOcupacao();
            latencia = link.getLatencia();
            for (Aresta I : arestas) {
                ((Link) I).setBanda(banda);
                ((Link) I).setTaxaOcupacao(taxa);
                ((Link) I).setLatencia(latencia);
            }
        } else {
            JOptionPane.showMessageDialog(null, palavras.getString("Please select a network icon"), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /*
     * Organiza icones na área de desenho
     */
    public void iconArrange() {
        //Distancia entre os icones
        int TAMANHO = 100;
        //posição inicial
        int linha = TAMANHO, coluna = TAMANHO;
        int pos_coluna = 0;
        int totalVertice = vertices.size();
        //número de elementos por linha
        int num_coluna = ((int) Math.sqrt(totalVertice)) + 1;
        //Organiza os icones na tela
        for (Vertice icone : vertices) {
            icone.setPosition(coluna, linha);
            //busca por arestas conectadas ao vertice
            coluna += TAMANHO;
            pos_coluna++;
            if (pos_coluna == num_coluna) {
                pos_coluna = 0;
                coluna = TAMANHO;
                linha += TAMANHO;
            }
        }
    }

    /*
     * Organiza icones na área de desenho
     */
    public void iconArrangeType() {
        //Distancia entre os icones
        int TAMANHO = 75;
        //posição inicial
     /*int posX = TAMANHO, posY = TAMANHO;
         List<Vertice> mestres = new ArrayList<Vertice>();
         List<Vertice> internets = new ArrayList<Vertice>();
         for (Vertice icone : vertices) {
         if (icone instanceof Machine && icone.isMestre()) {
         mestres.add(icone);
         } else if (icone instanceof Internet) {
         internets.add(icone);
         }
         }
         for (Vertice icone : mestres) {
         int num_voltas = 2;
         int num_coluna = 3;
         if (icone.getEscravos().size() > 8) {
         num_voltas = (icone.getEscravos().size() - 8) / 4;
         num_coluna = 3 + (((int) num_voltas / 2) - 1) * 2;
         }
         int inicioX = posX;
         int inicioY = posY;
         int pos_coluna = 0;
         //mestre
         int posMestreX = inicioX + (TAMANHO * ((int) num_voltas / 2));
         int posMestreY = inicioY + (TAMANHO * ((int) num_voltas / 2));
         icone.setPosition(posMestreX, posMestreY);
         conectarRede(icone);
         //Escravos
         for (Icone icon : icones) {
         if (icone.getEscravos().contains(icon.getIdGlobal())) {
         icon.setPosition(posX, posY);
         //busca por arestas conectadas ao vertice
         conectarRede(icon);
         posX += TAMANHO;
         pos_coluna++;
         if (posX == posMestreX && posY == posMestreY) {
         posX += TAMANHO;
         pos_coluna++;
         }
         if (pos_coluna == num_coluna) {
         pos_coluna = 0;
         posX = inicioX;
         posY += TAMANHO;
         }
         }
         }
         }
         for (Icone icone : internets) {
         int num_voltas = 1;
         int num_coluna = 3;
         //adiciona elementos conectados no ícone de internet
         icone.getEscravos().clear();
         for (Icone I : icones) {
         if (I.getTipoIcone() == Icone.NETWORK) {
         if (I.getNoDestino() == icone.getIdGlobal()) {
         icone.getEscravos().add(I.getNoOrigem());
         } else if (I.getNoOrigem() == icone.getIdGlobal()) {
         icone.getEscravos().add(I.getNoDestino());
         }
         }
         }
         if (icone.getEscravos().size() > 8) {
         num_voltas = (icone.getEscravos().size() - 8) / 4;
         num_coluna = 3 + (((int) num_voltas / 2) - 1) * 2;
         }
         int inicioX = posX;
         int inicioY = posY;
         int pos_coluna = 0;
         //mestre
         int posMestreX = inicioX + (TAMANHO * ((int) num_voltas / 2));
         int posMestreY = inicioY + (TAMANHO * ((int) num_voltas / 2));
         icone.setPosition(posMestreX, posMestreY);
         conectarRede(icone);
         //Escravos
         for (Icone icon : icones) {
         if (posX == posMestreX && posY == posMestreY) {
         posX += TAMANHO;
         pos_coluna++;
         }
         if (icone.getEscravos().contains(icon.getIdGlobal())
         && icon.getTipoIcone() != Icone.INTERNET) {
         icon.setPosition(posX, posY);
         //busca por arestas conectadas ao vertice
         conectarRede(icon);
         posX += TAMANHO;
         pos_coluna++;
         if (pos_coluna == num_coluna) {
         pos_coluna = 0;
         posX = inicioX;
         posY += TAMANHO;
         }
         }
         }
         }*/
    }

    public void setIdioma(ResourceBundle palavras) {
        this.palavras = palavras;
        this.initTexts();
    }

    public List<String> getNosEscalonadores() {
        List<String> maquinas = new ArrayList<String>();
        for (Icone icon : vertices) {
            if (icon instanceof Machine && ((Machine) icon).isMestre()) {
                maquinas.add(((ItemGrade) icon).getId().getNome());
            }
            if (icon instanceof Cluster && ((Cluster) icon).isMestre()) {
                maquinas.add(((ItemGrade) icon).getId().getNome());
            }
        }
        return maquinas;
    }

    @Override
    public void showSelectionIcon(MouseEvent me, Icone icon) {
        this.setLabelAtributos((ItemGrade) icon);
    }

    @Override
    public void showActionIcon(MouseEvent me, ispd.gui.iconico.Icone icon) {
        this.janelaPrincipal.modificar();
        if (icon instanceof Machine || icon instanceof Cluster) {
            this.janelaPrincipal.getjPanelConfiguracao().setIcone((ItemGrade) icon, usuarios, tipoModelo);
            JOptionPane.showMessageDialog(
                    janelaPrincipal,
                    this.janelaPrincipal.getjPanelConfiguracao(),
                    this.janelaPrincipal.getjPanelConfiguracao().getTitle(),
                    JOptionPane.PLAIN_MESSAGE);
        } else {
            this.janelaPrincipal.getjPanelConfiguracao().setIcone((ItemGrade) icon);
            JOptionPane.showMessageDialog(
                    janelaPrincipal,
                    this.janelaPrincipal.getjPanelConfiguracao(),
                    this.janelaPrincipal.getjPanelConfiguracao().getTitle(),
                    JOptionPane.PLAIN_MESSAGE);
        }
        this.setLabelAtributos((ItemGrade) icon);
    }

    public void setIconeSelecionado(Integer object) {
        if (object != null) {
            if (object == NETWORK) {
                setAddAresta(true);
                setAddVertice(false);
                setCursor(hourglassCursor);
            } else {
                tipoDeVertice = object;
                setAddAresta(false);
                setAddVertice(true);
                setCursor(hourglassCursor);
            }
        } else {
            setAddAresta(false);
            setAddVertice(false);
            setCursor(normalCursor);
        }
    }

    public void setGrade(Object descricao) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void setGrade(Document descricao) {
        //Realiza leitura dos usuários/proprietários do modelo
        this.usuarios = IconicoXML.newSetUsers(descricao);
        this.maquinasVirtuais = IconicoXML.newListVirtualMachines(descricao);
        Element aux = (Element) descricao.getElementsByTagName("system").item(0);
        String versao = aux.getAttribute("version");
        if(versao.contentEquals("2.1"))
            tipoModelo = EscolherClasse.GRID;
        else if(versao.contentEquals("2.2"))
            tipoModelo = EscolherClasse.IAAS;
        else if(versao.contentEquals("2.3"))
            tipoModelo = EscolherClasse.PAAS;
        this.perfis = IconicoXML.newListPerfil(descricao);
        //Realiza leitura dos icones
        IconicoXML.newGrade(descricao, vertices, arestas);
        //Realiza leitura da configuração de carga do modelo
        this.cargasConfiguracao = IconicoXML.newGerarCarga(descricao);
        //Atuasliza número de vertices e arestas
        for (Icone icone : arestas) {
            Link link = (Link) icone;
            if (this.numArestas < link.getId().getIdLocal()) {
                this.numArestas = link.getId().getIdLocal();
            }
            if (this.numIcones < link.getId().getIdGlobal()) {
                this.numIcones = link.getId().getIdGlobal();
            }
        }
        for (Icone icone : vertices) {
            ItemGrade vertc = (ItemGrade) icone;
            if (this.numVertices < vertc.getId().getIdLocal()) {
                this.numVertices = vertc.getId().getIdLocal();
            }
            if (this.numIcones < vertc.getId().getIdGlobal()) {
                this.numIcones = vertc.getId().getIdGlobal();
            }
        }
        this.numIcones++;
        this.numVertices++;
        this.numArestas++;
        repaint();
    }

    private static void criarImagens() {
        if (IMACHINE == null) {
            ImageIcon maq = new ImageIcon(JPrincipal.class.getResource("imagens/botao_no.gif"));
            IMACHINE = maq.getImage();
            ImageIcon clt = new ImageIcon(JPrincipal.class.getResource("imagens/botao_cluster.gif"));
            ICLUSTER = clt.getImage();
            ImageIcon net = new ImageIcon(JPrincipal.class.getResource("imagens/botao_internet.gif"));
            IINTERNET = net.getImage();
            ImageIcon verd = new ImageIcon(JPrincipal.class.getResource("imagens/verde.png"));
            IVERDE = verd.getImage();
            ImageIcon verm = new ImageIcon(JPrincipal.class.getResource("imagens/vermelho.png"));
            IVERMELHO = verm.getImage();
        }
    }

    public Set<Vertice> getVertices() {
        return vertices;
    }
}
