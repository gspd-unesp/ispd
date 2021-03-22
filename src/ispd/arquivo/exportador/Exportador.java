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
 * Exportador.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Rafael Stabile;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.arquivo.exportador;

import ispd.arquivo.xml.IconicoXML;
import ispd.arquivo.xml.ManipuladorXML;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Classe para converter modelo do iSPD para outros simuladores
 * @author Rafael Stabile
 */
public class Exportador {

    Document descricao;

    public Exportador(Document descricao) {
        this.descricao = descricao;
    }

    /**
     * Converte modelo iconico do iSPD nos arquivos xml do SimGrid 3.2
     * 
     * @param arquivo local no qual será salvo os arquivos plataform e
     * application no formato do xml usado no SimGrid 3.2
     */
    public void toSimGrid(File arquivo) {
        IconicoXML.validarModelo(descricao);

        NodeList docCarga = descricao.getElementsByTagName("load");
        Integer numeroTarefas = 0;
        Double computacao = 0.0;
        Double comunicacao = 0.0;
        //Realiza leitura da configuração de carga random
        if (docCarga.getLength() != 0) {
            Element cargaAux = (Element) docCarga.item(0);
            docCarga = cargaAux.getElementsByTagName("random");
            if (docCarga.getLength() != 0) {
                Element carga = (Element) docCarga.item(0);
                numeroTarefas = Integer.parseInt(carga.getAttribute("tasks"));
                NodeList size = carga.getElementsByTagName("size");
                for (int i = 0; i < size.getLength(); i++) {
                    Element size1 = (Element) size.item(i);
                    if (size1.getAttribute("type").equals("computing")) {
                        computacao = Double.parseDouble(size1.getAttribute("average"));
                    } else if (size1.getAttribute("type").equals("communication")) {
                        comunicacao = Double.parseDouble(size1.getAttribute("average"));
                    }
                }
            } else {
                docCarga = cargaAux.getElementsByTagName("node");
            }
        }

        Document docApplication = ManipuladorXML.novoDocumento();
        Element tagApplication = docApplication.createElement("platform_description");
        tagApplication.setAttribute("version", "1");
        docApplication.appendChild(tagApplication);

        Document docPlataform = ManipuladorXML.novoDocumento();
        Element tagPlataform = docPlataform.createElement("platform_description");
        tagPlataform.setAttribute("version", "1");
        docPlataform.appendChild(tagPlataform);

        NodeList docmaquinas = descricao.getElementsByTagName("machine");
        NodeList docCluster = descricao.getElementsByTagName("cluster");
        NodeList doclinks = descricao.getElementsByTagName("link");
        NodeList docNets = descricao.getElementsByTagName("internet");

        int numMestre = 0;
        HashMap<Integer, String> maquinas = new HashMap<Integer, String>();
        HashMap<Integer, String> comutacao = new HashMap<Integer, String>();
        HashMap<Integer, Integer> link_origem = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> link_destino = new HashMap<Integer, Integer>();

        //Busca poder das máquinas
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                numMestre++;
            }
            Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            maquinas.put(global, maquina.getAttribute("id"));
            Element cpu = docPlataform.createElement("cpu");
            cpu.setAttribute("name", maquina.getAttribute("id"));
            cpu.setAttribute("power", maquina.getAttribute("power"));
            tagPlataform.appendChild(cpu);
        }
        //Busca clusters
        for (int i = 0; i < docCluster.getLength(); i++) {
            Element cluster = (Element) docCluster.item(i);
            Element id = (Element) cluster.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            maquinas.put(global, cluster.getAttribute("id"));
            Element cpu = docPlataform.createElement("cpu");
            cpu.setAttribute("name", cluster.getAttribute("id"));
            cpu.setAttribute("power", cluster.getAttribute("power"));
            tagPlataform.appendChild(cpu);
        }
        //Busca busca banda das redes
        for (int i = 0; i < doclinks.getLength(); i++) {
            Element link = (Element) doclinks.item(i);
            Element network = docPlataform.createElement("network_link");
            network.setAttribute("name", link.getAttribute("id"));
            network.setAttribute("bandwidth", link.getAttribute("bandwidth"));
            network.setAttribute("latency", link.getAttribute("latency"));
            tagPlataform.appendChild(network);
            Element connect = (Element) link.getElementsByTagName("connect").item(0);
            //salva origem e destino do link
            Element id = (Element) link.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            comutacao.put(global, link.getAttribute("id"));
            link_origem.put(global, Integer.parseInt(connect.getAttribute("origination")));
            link_destino.put(global, Integer.parseInt(connect.getAttribute("destination")));
        }
        for (int i = 0; i < docNets.getLength(); i++) {
            Element link = (Element) docNets.item(i);
            Element network = docPlataform.createElement("network_link");
            network.setAttribute("name", link.getAttribute("id"));
            network.setAttribute("bandwidth", link.getAttribute("bandwidth"));
            network.setAttribute("latency", link.getAttribute("latency"));
            tagPlataform.appendChild(network);
            //salva elemento na lista com os elementos de comutação
            Element id = (Element) link.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            comutacao.put(global, link.getAttribute("id"));
        }
        //Escreve mestres
        boolean primeiro = true;
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                Element mestre = docApplication.createElement("process");
                String idMestre = maquina.getAttribute("id");
                mestre.setAttribute("host", idMestre);
                mestre.setAttribute("function", "master");
                //Adicionar tarefas
                if (docCarga.item(0).getNodeName().equals("random")) {
                    //Number of tasks
                    Element carga = docApplication.createElement("argument");
                    if (primeiro) {
                        primeiro = false;
                        Integer temp = (numeroTarefas / numMestre) + (numeroTarefas % numMestre);
                        carga.setAttribute("value", temp.toString());
                    } else {
                        Integer temp = (numeroTarefas / numMestre);
                        carga.setAttribute("value", temp.toString());
                    }
                    mestre.appendChild(carga);
                    //Computation size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", computacao.toString());
                    mestre.appendChild(carga);
                    //Communication size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", comunicacao.toString());
                    mestre.appendChild(carga);
                } else if (docCarga.item(0).getNodeName().equals("node")) {
                    numeroTarefas = 0;
                    for (int j = 0; j < docCarga.getLength(); j++) {
                        Element carga = (Element) docCarga.item(j);
                        String escalonador = carga.getAttribute("id_master");
                        if (escalonador.equals(idMestre)) {
                            numeroTarefas += Integer.parseInt(carga.getAttribute("tasks"));
                            NodeList size = carga.getElementsByTagName("size");
                            for (int k = 0; k < size.getLength(); k++) {
                                Element size1 = (Element) size.item(k);
                                if (size1.getAttribute("type").equals("computing")) {
                                    double temp = Double.parseDouble(size1.getAttribute("maximum"));
                                    if (temp > computacao) {
                                        computacao = temp;
                                    }
                                } else if (size1.getAttribute("type").equals("communication")) {
                                    double temp = Double.parseDouble(size1.getAttribute("maximum"));
                                    if (temp > comunicacao) {
                                        comunicacao = temp;
                                    }
                                }
                            }
                        }
                    }
                    //Number of tasks
                    Element carga = docApplication.createElement("argument");
                    carga.setAttribute("value", numeroTarefas.toString());
                    mestre.appendChild(carga);
                    //Computation size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", computacao.toString());
                    mestre.appendChild(carga);
                    //Communication size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", comunicacao.toString());
                    mestre.appendChild(carga);
                }
                //Adiciona escravos ao mestre
                NodeList slaves = maquina.getElementsByTagName("slave");
                for (int j = 0; j < slaves.getLength(); j++) {
                    Element escravo = docApplication.createElement("argument");
                    Element slave = (Element) slaves.item(j);
                    String idEscravo = maquinas.get(Integer.parseInt(slave.getAttribute("id")));
                    escravo.setAttribute("value", idEscravo);
                    mestre.appendChild(escravo);
                    //Define roteamento entre mestre e escravo
                    Element rota = docPlataform.createElement("route");
                    rota.setAttribute("src", idMestre);
                    rota.setAttribute("dst", idEscravo);
                    ArrayList<Element> caminho = caminho(idMestre, idEscravo, docPlataform, link_origem, link_destino, comutacao, maquinas, null);
                    if (caminho != null) {
                        for (Element element : caminho) {
                            rota.appendChild(element);
                        }
                    }
                    tagPlataform.appendChild(rota);
                    rota = docPlataform.createElement("route");
                    rota.setAttribute("src", idEscravo);
                    rota.setAttribute("dst", idMestre);
                    caminho = caminho(idEscravo, idMestre, docPlataform, link_origem, link_destino, comutacao, maquinas, null);
                    if (caminho != null) {
                        for (Element element : caminho) {
                            rota.appendChild(element);
                        }
                    }
                    tagPlataform.appendChild(rota);
                }
                tagApplication.appendChild(mestre);
            }
        }
        //Escreve escravos
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() == 0) {
                Element escravo = docApplication.createElement("process");
                escravo.setAttribute("host", maquina.getAttribute("id"));
                escravo.setAttribute("function", "slave");
                tagApplication.appendChild(escravo);
            }
        }
        for (int i = 0; i < docCluster.getLength(); i++) {
            Element cluster = (Element) docCluster.item(i);
            Element escravo = docApplication.createElement("process");
            escravo.setAttribute("host", cluster.getAttribute("id"));
            escravo.setAttribute("function", "slave");
            Element maqs = docApplication.createElement("argument");
            maqs.setAttribute("value", cluster.getAttribute("nodes"));
            escravo.appendChild(maqs);
            tagApplication.appendChild(escravo);
        }
        String name = arquivo.getName();
        String diretorio = arquivo.getPath();
        ManipuladorXML.escrever(docPlataform, new File(arquivo.getParentFile(), "plataform_" + name), "surfxml.dtd", true);
        ManipuladorXML.escrever(docApplication, new File(arquivo.getParentFile(), "application_" + name), "surfxml.dtd", true);
    }

    /**
     * Converte modelo iconico do iSPD no arquivo java do GridSim
     * @param file arquivo na qual as classes serão salvas
     */
    public void toGridSim(File file) {
        NodeList owners = descricao.getElementsByTagName("owner");
        HashMap<String, Integer> usuarios = new HashMap<String, Integer>();
        HashMap<Integer, String> recursos = new HashMap<Integer, String>();
        NodeList maquinas = descricao.getElementsByTagName("machine");
        NodeList clusters = descricao.getElementsByTagName("cluster");
        NodeList internet = descricao.getElementsByTagName("internet");
        NodeList links = descricao.getElementsByTagName("link");
        NodeList cargas = descricao.getElementsByTagName("load");
        try {
            FileWriter writer = new FileWriter(file);
            PrintWriter saida = new PrintWriter(writer, true);
            /*
             * --------------------------SAIDA---------------------------
             */
            saida.println("\n import java.util.*; \n import gridsim.*;\n import gridsim.net.*;"); //Imports do modelo
            //Classe Mestre
            saida.println("\nclass Mestre extends GridSim {\n");
            saida.println("\tGridletList list; \n\tprivate Integer ID_; \n\tpublic Router r; \n\tArrayList Escravos_;\n\t int Escal;");
            saida.println("\n\n\tMestre(String nome, Link link,GridletList list, ArrayList Escravo, int esc) throws Exception {");
            saida.println("\n\t\tsuper(nome, link); \n\t\tthis.list = list;\n\t\tthis.ID_ = new Integer(getEntityId(nome));\n\t\t this.Escravos_ = Escravo; \n\t\tthis.Escal=esc; }");
            saida.println("\n\t@Override\n\tpublic void body() {");
            saida.println("\t\tArrayList<GridResource> resList = this.Escravos_;\n\t\tint ids[] = new int[resList.size()];");
            saida.println("\t\tdouble temp_ini, temp_fim; \n\twhile (true) {");
            saida.println("\t\tsuper.gridSimHold(2.0); \n\t\tLinkedList recur = GridSim.getGridResourceList(); \t\tif (recur.size() > 0) break; \n\t}");
            saida.println("\t\t\tfor(int j=0;j<resList.size(); j++){ \n\t\t ids[j] = resList.get(j).get_id();\n\t}");
            saida.println("\n\t\tfor(int i = 0; i < resList.size(); i++){");
            saida.println("\t\t\tsuper.send(ids[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);\n\t\t}");
            saida.println("\t\ttemp_ini = GridSim.clock();");
            saida.println("\t\tif(this.Escal==1){ //O escalonador é Workqueue");
            saida.println("\t\t\tint cont=0; int k; Gridlet gl;");
            saida.println("\t\t\tfor(k=0; k < Escravos_.size() && cont < list.size(); k++, cont++){");
            saida.println("\t\t\t\tint num = resList.get(k).get_id();;");
            saida.println("\t\t\t\tlist.get(cont).setUserID(this.ID_);\n\t\t\t\tsuper.gridletSubmit((Gridlet)list.get(cont),num , 0.0, true);\n\t\t\t}");
            saida.println("\t\t\tint res=0; \n\t\t\twhile(cont<list.size() || res<list.size()) {\n\t\t\t\t gl = super.gridletReceive();\n\t\t\t\tres++; \n\t\t\t\tint num = gl.getResourceID();\n\t\t\t\tif(cont<list.size()){ ");
            saida.println("\t\t\t\t\tlist.get(cont).setUserID(this.ID_); \n\t\t\t\t\tsuper.gridletSubmit((Gridlet)list.get(cont),num , 0.0, true); \n\t\t\t\t\tcont++; \n\t\t\t\t} \n\t\t\t}");
            saida.println("\t\t}else{//É RoundRobin");
            saida.println("\t\t");
            saida.println("\t\t}");
            saida.println("\t\ttemp_fim = GridSim.clock(); \n\t\tSystem.out.println(\"TEMPO DE SIMULAÇÂO:\"+(temp_fim-temp_ini));\n\t\tsuper.shutdownGridStatisticsEntity();");
            saida.println("\t\tsuper.shutdownUserEntity();\n\t\t super.terminateIOEntities();\n\t\t } \n\t}");


            //Classe principal
            saida.println(" \nclass Modelo{ \n\n  \tpublic static void main(String[] args) {\n");
            saida.println("\t\ttry {");
            saida.println("\t\t\tCalendar calendar = Calendar.getInstance(); \n\t\t\t boolean trace_flag = true;");
            saida.println("\t\t\tString[] exclude_from_file = {\"\"}; \n\t\t\t String[] exclude_from_processing = {\"\"};");
            saida.println("\t\t\tGridSim.init(" + owners.getLength() + ",calendar, true, exclude_from_file,exclude_from_processing, null);");
            saida.println("\n\t\t\tFIFOScheduler resSched = new FIFOScheduler( \" GridResSched \");");
            //Recursos
            saida.println("\t\t\tdouble baud_rate = 100.0; \n\t\t\tdouble delay =0.1; \n\t\t\tint MTU = 100;");
            for (int i = 0; i < maquinas.getLength(); i++) { //Quando o recurso é uma só máquina
                Element maquina = (Element) maquinas.item(i);
                if (maquina.getElementsByTagName("master").getLength() == 0) {
                    Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
                    int global = Integer.parseInt(id.getAttribute("global"));
                    recursos.put(global, maquina.getAttribute("id"));
                    saida.println("\n\t\t\tGridResource " + maquina.getAttribute("id") + " = createResource(\"" + maquina.getAttribute("id") + "_\",  baud_rate,  delay,  MTU, 1, (int)" + maquina.getAttribute("power") + ");");
                    saida.println("\t\t\tRouter r_" + maquina.getAttribute("id") + " = new RIPRouter( \"router_" + i + "\", trace_flag);");
                    saida.println("\t\t\tr_" + maquina.getAttribute("id") + ".attachHost( " + maquina.getAttribute("id") + ", resSched); ");

                    //saida.println("\n\t\t\tMachineList mList" + i + " = new MachineList(); ");
                    //saida.println("\t\t\tmList" + i + ".add( new Machine(" + i + ",1,(int)" + maquina.getAttribute("power") + "));");
                    //saida.println("\t\t\tResourceCharacteristics resConfig" + i + " = new ResourceCharacteristics(\"Sun Ultra\",\"Solaris\" , mList" + i + ", ResourceCharacteristics.TIME_SHARED, 0, 0);");
                    //saida.println("\t\t\tGridResource " + maquina.getAttribute("id") + " = new GridResource(\"" + maquina.getAttribute("id") + "\",0 ,resConfig" + i + ", null);");//link???

                    //saida.println("\t\t\tRouter r_"+maquina.getAttribute("id") + " = new RIPRouter( \" router_"+i + " \", trace_flag);");//Cada recurso é conectado ao seu próprio reteador
                    //saida.println("\t\t\tFIFOScheduler resSched = new FIFOScheduler( \" GridResSched \");");
                    //saida.println("\t\t\t r_"+ maquina.getAttribute("id")+".attachHost( "+maquina.getAttribute("id") + ", resSched); "); //nome roteador igual ao recurso            
                }
            }
            for (int j = 0, i = maquinas.getLength(); i < maquinas.getLength() + clusters.getLength(); i++, j++) {//Quando o recurso é um cluster
                Element cluster = (Element) clusters.item(j);
                Element id = (Element) cluster.getElementsByTagName("icon_id").item(0);
                int global = Integer.parseInt(id.getAttribute("global"));
                recursos.put(global, cluster.getAttribute("id"));
                saida.println("\n\t\t\tGridResource " + cluster.getAttribute("id") + "= createResource(\"" + cluster.getAttribute("id") + "_\",  baud_rate,  delay,  MTU," + (Integer.parseInt(cluster.getAttribute("nodes"))) + ",(int)" + cluster.getAttribute("power") + ");");
                saida.println("\t\t\tRouter r_" + cluster.getAttribute("id") + " = new RIPRouter( \"router_" + i + "\", trace_flag);");
                saida.println("\t\t\tr_" + cluster.getAttribute("id") + ".attachHost( " + cluster.getAttribute("id") + ", resSched); ");
                //saida.println("\n\t\t\tMachineList mList" + i + " = new MachineList(); ");
                //saida.println("\t\t\tfor(int k = 0; k < " + (Integer.parseInt(cluster.getAttribute("nodes"))) + "; k++)");
                //saida.println("\t\t\t\tmList" + i + ".add( new Machine( k ,1,(int)" + cluster.getAttribute("power") + "));");

                //saida.println("\t\t\tResourceCharacteristics resConfig" + i + " = new ResourceCharacteristics(\"Sun Ultra\",\"Solaris\" , mList" + i + ", ResourceCharacteristics.TIME_SHARED, 0, 0);");
                //saida.println("\t\t\tGridResource " + cluster.getAttribute("id") + " = new GridResource(" + cluster.getAttribute("id") + ",0 ,resConfig" + i + ", null);");//link???

                //saida.println("\t\t\tRouter r_"+cluster.getAttribute("id") + " = new RIPRouter( \"router_"+i +"\", trace_flag);");//Cada recurso é conectado ao seu próprio reteador
                //saida.println("\t\t\tFIFOScheduler resSched = new FIFOScheduler( \"GridResSched"+ i +"\");");
                //saida.println("\t\t\tr_"+cluster.getAttribute("id")+".attachHost( "+cluster.getAttribute("id") + ", resSched); ");
            }
            //Carga
            if (cargas.getLength() != 0) {
                Element cargaAux = (Element) cargas.item(0);
                NodeList trace = cargaAux.getElementsByTagName("trace");
                if (trace.getLength() == 0) {
                    saida.println("\n\t\t\tGridletList list = createGridlet();\n");
                } else { //Leitura de trace
                    cargaAux = (Element) trace.item(0);
                    saida.println("\n\t\t\t String[] fileName = { ");
                    saida.println("\t\t\t\t" + cargaAux.getAttribute("file_path"));
                    saida.println("\n\t\t\t}");
                    saida.println("\n\t\t\t ArrayList load = new ArrayList();");
                    saida.println("\t\t\t for (i = 0; i < fileName.length; i++){");
                    saida.println("\t\t\t\tWorkload w = new Workload(\"Load_\"+i, fileName[i], resList[], rating);");//falta acabar, resList[] e rating
                    saida.println("\t\t\t\tload.add(w);\n\t\t\t}");
                }
            }

            //Mestres
            saida.println("\n\t\t\tLink link = new SimpleLink(\"link_\", 100, 0.01, 1500 );");
            for (int i = 0; i < maquinas.getLength(); i++) {
                Element maquina = (Element) maquinas.item(i);
                if (maquina.getElementsByTagName("master").getLength() == 1) {
                    Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
                    int global = Integer.parseInt(id.getAttribute("global"));
                    recursos.put(global, maquina.getAttribute("id"));
                    NodeList escravos = ((Element) maquina.getElementsByTagName("master").item(0)).getElementsByTagName("slave");
                    saida.println("\n\t\t\tArrayList esc" + i + " = new ArrayList();");
                    for (int j = 0; j < escravos.getLength(); j++) {
                        int global_escravo = Integer.parseInt(((Element) escravos.item(j)).getAttribute("id"));
                        saida.println("\t\t\tesc" + i + ".add(" + recursos.get(global_escravo) + ");");
                    }

                    saida.println("\n\t\t\tMestre " + maquina.getAttribute("id") + " = new Mestre(\"" + maquina.getAttribute("id") + "_\", link, list, esc" + i + ", " + escravos.getLength() + ");");
                    saida.println("\t\t\tRouter r_" + maquina.getAttribute("id") + " = new RIPRouter( \"router_" + i + "\", trace_flag);");
                    saida.println("\t\t\tr_" + maquina.getAttribute("id") + ".attachHost( " + maquina.getAttribute("id") + ", resSched); ");

                    for (int j = 0; j < escravos.getLength(); j++) {
                        int global_escravo = Integer.parseInt(((Element) escravos.item(j)).getAttribute("id"));
                        saida.println("\n\t\t\tr_" + maquina.getAttribute("id") + ".attachHost( " + recursos.get(global_escravo) + ", resSched); ");
                    }

                }
            }

            //Usuário
            saida.println("\n\t\t\tResourceUserList userList = createGridUser();");


            //Rede
            for (int i = 0; i < internet.getLength(); i++) {//PAra internet
                Element net = (Element) internet.item(i);
                Element id = (Element) net.getElementsByTagName("icon_id").item(0);
                int global = Integer.parseInt(id.getAttribute("global"));
                recursos.put(global, net.getAttribute("id"));
                saida.println("\t\t\tRouter r_" + net.getAttribute("id") + " = new RIPRouter(" + net.getAttribute("id") + ",trace_flag);");
            }

            saida.println("\t\t\tFIFOScheduler rSched = new FIFOScheduler(\"r_Sched\");");
            for (int i = 0; i < links.getLength(); i++) {//Pega cada conexão que nao seja mestre
                Element link = (Element) links.item(i);
                Element connect = (Element) link.getElementsByTagName("connect").item(0);
                int origin = Integer.parseInt(connect.getAttribute("origination"));
                int dest = Integer.parseInt(connect.getAttribute("destination"));
                //if(!(recursos.get(dest)==null) && !(recursos.get(origin)==null))//Conexão que nao tenham mestre
                saida.println("\n\t\t\tLink " + link.getAttribute("id") + " = new SimpleLink(\"link_" + i + "\", " + link.getAttribute("bandwidth") + "*1000, " + link.getAttribute("latency") + "*1000,1500  );");
                //saida.println("\t\t\tr_"+recursos.get(origin)+".attachRouter(r_"+recursos.get(dest)+","+link.getAttribute("id") +",rSched, );");

            }

            //simulaçao
            saida.println("\n\t\t\tGridSim.startGridSimulation();");

            saida.println("\t\t} \t\tcatch (Exception e){ ");
            saida.println("\t\t\t  e.printStackTrace();\n \t\t\tSystem.out.println(\"Unwanted ERRORS happened\"); \n\t\t} \n\t} ");
            //Fim Main
            //metodo criação de Usuário
            saida.println("\n\n\tprivate static ResourceUserList createGridUser(){");
            saida.println("\t\tResourceUserList userList = new ResourceUserList();");
            for (int i = 0; i < owners.getLength(); i++) {
                Element owner = (Element) owners.item(i);
                usuarios.put(owner.getAttribute("id"), i);
                saida.println("\t\tuserList.add(" + i + ");");
            }
            saida.println("\t\treturn userList;\n\t}");

            //Método para a criação de recursos 
            saida.println("\n\tprivate static GridResource createResource(String name, double baud_rate, double delay, int MTU, int n_maq, int cap){");
            saida.println("\n\t\t\tMachineList mList = new MachineList();\n\t\t\tfor(int i = 0; i < n_maq; i++){");
            saida.println(" \n\t\t\t mList.add( new Machine(i, 1, cap)); \n\t\t}");
            saida.println("\n\t\t\tString arch = \"Sun Ultra\"; \n\t\t\tString os = \"Solaris\"; \n\t\t\tdouble time_zone = 9.0; \n\t\t\tdouble cost = 3.0;");
            saida.println("\n\t\tResourceCharacteristics resConfig = new ResourceCharacteristics(arch, os, mList, ResourceCharacteristics.TIME_SHARED,time_zone, cost);");
            saida.println("\n\t\tlong seed = 11L*13*17*19*23+1; \n\t\tdouble peakLoad = 0.0; \n\t\tdouble offPeakLoad = 0.0; \n\t\tdouble holidayLoad = 0.0;");
            saida.println("\n\t\tLinkedList Weekends = new LinkedList(); \n\t\tWeekends.add(new Integer(Calendar.SATURDAY)); \n\t\tWeekends.add(new Integer(Calendar.SUNDAY)); \n\t\tLinkedList Holidays = new LinkedList();");
            saida.println("\t\tGridResource gridRes=null;");
            saida.println("\n\t\ttry\n\t\t { \n\t\t\t gridRes = new GridResource(name, new SimpleLink(name + \"_link\", baud_rate, delay, MTU),seed, resConfig, peakLoad, offPeakLoad, holidayLoad,Weekends, Holidays);");
            saida.println("\n\t\t} \n\t\tcatch (Exception e) {\n\t\t\te.printStackTrace();\n\t\t}");
            saida.println("\n\t\treturn gridRes;\n\t}");

            //metodo Criação de Tarefas
            saida.println("\n\n\tprivate static GridletList createGridlet(){ \n\t\tdouble length; \n\t\tlong file_size;\n\t\tRandom random = new Random();");
            saida.println("\n\t\tGridletList list = new GridletList();");
            for (int j = 0; j < cargas.getLength(); j++) {
                Element carga = (Element) cargas.item(j);
                double minComputacao = 0;
                double maxComputacao = 0;
                double minComunicacao = 0;
                double maxComunicacao = 0;
                double value = 0;
                double val = 0;
                double mincp = 0;
                double maxcp = 0;
                double mincm = 0;
                double maxcm = 0;
                NodeList size = carga.getElementsByTagName("size");
                for (int k = 0; k < size.getLength(); k++) {
                    Element size1 = (Element) size.item(k);

                    if (size1.getAttribute("type").equals("computing")) {
                        minComputacao = Double.parseDouble(size1.getAttribute("minimum"));
                        maxComputacao = Double.parseDouble(size1.getAttribute("maximum"));
                        value = Double.parseDouble(size1.getAttribute("average"));
                        mincp = (value - minComputacao) / value;
                        if (mincp > 1.0) {
                            mincp = 1.0;
                        }
                        maxcp = (maxComputacao - value) / value;
                        if (maxcp > 1.0) {
                            maxcp = 1.0;
                        }
                    } else if (size1.getAttribute("type").equals("communication")) {
                        minComunicacao = Double.parseDouble(size1.getAttribute("minimum"));
                        maxComunicacao = Double.parseDouble(size1.getAttribute("maximum"));
                        val = Double.parseDouble(size1.getAttribute("average"));
                        mincm = (val - minComputacao) / val;
                        if (mincm > 1.0) {
                            mincp = 1.0;
                        }
                        maxcm = (maxComputacao - val) / val;
                        if (maxcm > 1.0) {
                            maxcp = 1.0;
                        }
                    }
                    saida.println("\t\tlength = GridSimRandom.real(" + value + "," + mincp + "," + maxcp + ",random.nextDouble());");
                    saida.println("\t\tfile_size = (long) GridSimRandom.real(" + val + "," + mincm + "," + maxcm + ",random.nextDouble());");
                    saida.println("\t\tGridlet gridlet" + k + " = new Gridlet(" + k + ", length, file_size,file_size);");
                    saida.println("\t\tlist.add(gridlet" + k + ");");
                    saida.println("\n\t\tgridlet" + k + ".setUserID(0);");
                }
                saida.println("\n\t\treturn list;");
            }
            saida.println("\n\t} \n}");


        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Método auxiliar na conversão para SimGrim.
     * utilizado para criar uma rota entre duas máquinas
     */
    private static ArrayList<Element> caminho(String origem, String destino, Document docPlataform, HashMap<Integer, Integer> link_origem, HashMap<Integer, Integer> link_destino, HashMap<Integer, String> comutacao, HashMap<Integer, String> maquinas, ArrayList<String> expandido) {
        ArrayList<Element> caminho = new ArrayList<Element>();
        for (Map.Entry<Integer, Integer> entry : link_origem.entrySet()) {
            Integer chaveLink = entry.getKey();
            Integer idOrigem = entry.getValue();
            if (maquinas.get(idOrigem) != null && maquinas.get(idOrigem).equals(origem)) {
                if (maquinas.get(link_destino.get(chaveLink)) == null) {
                    ArrayList<Element> temp = caminho(comutacao.get(link_destino.get(chaveLink)), destino, docPlataform, link_origem, link_destino, comutacao, maquinas, new ArrayList<String>());
                    if (temp != null) {
                        Element elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(chaveLink));
                        caminho.add(elemento);
                        for (Element element : temp) {
                            caminho.add(element);
                        }
                        return caminho;
                    }
                } else {
                    if (maquinas.get(link_destino.get(chaveLink)).equals(destino)) {
                        Element elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(chaveLink));
                        caminho.add(elemento);
                        return caminho;
                    }
                }
            } else if (comutacao.get(idOrigem) != null && comutacao.get(idOrigem).equals(origem)) {
                if (maquinas.get(link_destino.get(chaveLink)) == null) {
                    if (!expandido.contains(comutacao.get(link_destino.get(chaveLink)))) {
                        ArrayList<String> tempExp = new ArrayList<String>(expandido);
                        tempExp.add(comutacao.get(idOrigem));
                        ArrayList<Element> temp = caminho(comutacao.get(link_destino.get(chaveLink)), destino, docPlataform, link_origem, link_destino, comutacao, maquinas, tempExp);
                        if (temp != null) {
                            Element elemento = docPlataform.createElement("route_element");
                            elemento.setAttribute("name", comutacao.get(idOrigem));
                            caminho.add(elemento);
                            elemento = docPlataform.createElement("route_element");
                            elemento.setAttribute("name", comutacao.get(chaveLink));
                            caminho.add(elemento);
                            for (Element element : temp) {
                                caminho.add(element);
                            }
                            return caminho;
                        }
                    }
                } else {
                    if (maquinas.get(link_destino.get(chaveLink)).equals(destino)) {
                        Element elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(idOrigem));
                        caminho.add(elemento);
                        elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(chaveLink));
                        caminho.add(elemento);
                        return caminho;
                    }
                }
            }
        }
        return null;
    }
}
