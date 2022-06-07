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
 * InterpretadorGridSim.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Gabriel Covello Furlanetto;
 * Contributor(s):   Denison Menezes;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.arquivo.interpretador.gridsim;

import ispd.arquivo.interpretador.gridsim.JavaParser.ResourceChar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author covello
 */
public class InterpretadorGridSim {

    private String fname;
    private List<HashMap<String, JavaParser.ResourceChar>> ListaMetodo;
    private List <String> NomeMetodo = new ArrayList<String>();
    private List <String> Nome = new ArrayList<String>();
    private List <Integer> Quantidade = new ArrayList<Integer>();
    
    
    private void setFileName(File f) {
        fname = f.getName();
    }

    public String getFileName() {
        return fname;
    }

    public void interpreta(File file1) {
        try {
            setFileName(file1);
            InputStream fisfile = new FileInputStream(file1);
            JavaParser parser = new JavaParser(fisfile);
            parser.CompilationUnit();
            parser.escreverLista();
            ListaMetodo = parser.getListaMetodo();
            NomeMetodo =  parser.getNomeMetodo();
            Nome = parser.getNome();
            Quantidade = parser.getQuantidade();
        } catch (ParseException ex) {
            System.err.println("Erro ao fechar arquivo1: " + ex.getMessage());
            Logger.getLogger(InterpretadorGridSim.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            System.err.println("Erro ao fechar arquivo2: " + ex.getMessage());
            Logger.getLogger(InterpretadorGridSim.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("Erro ao fechar arquivo: " + ex.getMessage());
            Logger.getLogger(InterpretadorGridSim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("empty-statement")
    public Document getDescricao() {
        LinkedList<String> user = new LinkedList<String>();
        LinkedList<String> mestre = new LinkedList<String>();
        HashMap<JavaParser.ResourceChar, String> idGlobal = new HashMap<JavaParser.ResourceChar, String>();
        Document descricao = ispd.arquivo.xml.ManipuladorXML.novoDocumento();
        Element system = descricao.createElement("system");
        Element load = descricao.createElement("load");
        system.setAttribute("version", "1");
        descricao.appendChild(system);
        int icon = 0;
        Integer cont_machine = 0, cont_link = 0, cont_global = 0, ident_global = 0, linha = 50, coluna = 50;
        Double num_linhas, num_col, lin = 0.0, col = 0.0;
        for (int i = 0; i < Nome.size(); i++) {
            for(int j = 0; j < NomeMetodo.size(); j++){
                if(NomeMetodo.get(j).equals(Nome.get(i))){
                    for(int k = 1; k < Quantidade.get(i); k++){
                        //obter o conteúdo do método
                        HashMap<String, JavaParser.ResourceChar> temp = ListaMetodo.get(j);
                        //criar uma cópia
                        HashMap<String, JavaParser.ResourceChar> tempAux = new HashMap<String, JavaParser.ResourceChar>();
                        JavaParser criar = new JavaParser();
                        for (Map.Entry<String, JavaParser.ResourceChar> entry : temp.entrySet()) {
                            String string = entry.getKey();
                            JavaParser.ResourceChar resourceChar = entry.getValue();
                            if(resourceChar.getType().equals("GridResource"))
                            {
                                List<JavaParser.ResourceChar> mach_list = new ArrayList<ResourceChar>(); 
                                JavaParser.ResourceChar aux = criar.Criar(resourceChar, resourceChar.getName()+k);
                                aux.setLink(criar.Criar(resourceChar.getLink(), resourceChar.getLink().getName_Link()+ cont_global));
                                cont_global++;
                                int l = 100;
                                for (ResourceChar mach : resourceChar.getResConfig().getMLIST().getMach_List()) {
                                    ResourceChar maq = criar.Criar(mach, resourceChar.getId()+k);
                                    mach_list.add(maq);
                                    tempAux.put(aux.getName()+k + l, maq);
                                    l++;
                                }
                                aux.setResConfig(criar.Criar(resourceChar.getResConfig(), resourceChar.getName()+k));
                                aux.getResConfig().setMlist(criar.Criar(resourceChar.getResConfig().getMLIST(), "mlist"+k));
                                aux.getResConfig().getMLIST().setMach_List(mach_list);
                                tempAux.put(aux.getName()+k, aux);
                            }
                        }
                        //adicionar cópia na lista
                        ListaMetodo.add(tempAux);
                    }
                }
            }
        }
        for (int i = 0; i < ListaMetodo.size(); i++) {
            //For para percorrer lista
            for (Map.Entry<String, JavaParser.ResourceChar> object : ListaMetodo.get(i).entrySet()) {
                String key = object.getKey();
                JavaParser.ResourceChar res = object.getValue();
                if (res.getType().equals("ResourceUser")) {
                    Element owner = descricao.createElement("owner");
                    owner.setAttribute("id", res.getUserID());
                    user.add(res.getUserID());
                    system.appendChild(owner);
                } else if (res.getType().equals("Machine") || res.getType().equals("Router")) {
                    icon++;
                    idGlobal.put(res, ident_global.toString());
                    //if(i >= NomeMetodo.size())
                        //System.out.println("Adicionando id  " + ident_global + " para " + res);
                    ident_global++;
                } else if (res.getType().equals("GridResource")) {
                    icon++;
                    idGlobal.put(res, ident_global.toString());
                    ident_global++;
                    mestre.add(res.getName());
                }
            }
        }
        num_linhas = Math.floor(Math.sqrt(icon));
        num_col = num_linhas + 1;
        if (user.isEmpty()) {
            Element owner = descricao.createElement("owner");
            owner.setAttribute("id", "user1");
            user.add("user1");
            system.appendChild(owner);
        }
        if (mestre.isEmpty()) {
            mestre.add("---");
        }
        for (int i = 0; i < ListaMetodo.size(); i++) {
            Iterator iUser = user.iterator();
            Iterator iMestre = mestre.iterator();
            //For para percorrer lista
            for (Map.Entry<String, JavaParser.ResourceChar> object : ListaMetodo.get(i).entrySet()) {
                String key = object.getKey();
                JavaParser.ResourceChar res = object.getValue();
                if (res.getType().equals("gridlet")) {
                    Element node = descricao.createElement("node");
                    node.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    node.setAttribute("application", "application0");
                    node.setAttribute("id_master", iMestre.next().toString());
                    if (!iMestre.hasNext()) {
                        iMestre = mestre.iterator();
                    }
                    node.setAttribute("tasks", "1");
                    Element size1 = descricao.createElement("size");
                    size1.setAttribute("type", "computing");
                    String computing = getDouble(res.getLenght());
                    size1.setAttribute("maximum", computing);
                    size1.setAttribute("minimum", computing);
                    Element size2 = descricao.createElement("size");
                    size2.setAttribute("type", "communication");
                    Double comunication = Double.valueOf(getDouble(res.getOutput_size()));
                    comunication += Double.valueOf(getDouble(res.getFile_size()));
                    size2.setAttribute("maximum", comunication.toString());
                    size2.setAttribute("minimum", comunication.toString());
                    node.appendChild(size1);
                    node.appendChild(size2);
                    load.appendChild(node);
                }
                if (res.getType().equals("Machine")) {
                    Element machine = descricao.createElement("machine");
                    machine.setAttribute("id", "maq" + cont_global);
                    cont_global++;
                    machine.setAttribute("power", getInt(res.getMipsRating()) + "");
                    machine.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    machine.setAttribute("load", "0.0");
                    Element pos = descricao.createElement("position");
                    pos.setAttribute("x", linha.toString());
                    pos.setAttribute("y", coluna.toString());

                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin = 0.0;
                            linha = 50;
                            col = col + 1.0;
                        }
                    }
                    machine.appendChild(pos);
                    Element id = descricao.createElement("icon_id");
                    id.setAttribute("global", idGlobal.get(res));
                    id.setAttribute("local", cont_machine.toString());
                    cont_machine++;
                    machine.appendChild(id);
                    system.appendChild(machine);
                }
                if (res.getType().equals("Router")) {
                    Element internet = descricao.createElement("internet");
                    internet.setAttribute("id", res.getName_Router() + "");
                    internet.setAttribute("bandwidth", "1000.0");
                    internet.setAttribute("latency", "0.001");
                    internet.setAttribute("load", "0.0");
                    Element pos = descricao.createElement("position");
                    pos.setAttribute("x", linha.toString());
                    pos.setAttribute("y", coluna.toString());
                    internet.appendChild(pos);
                    Element id = descricao.createElement("icon_id");
                    id.setAttribute("global", idGlobal.get(res));
                    id.setAttribute("local", cont_machine.toString());
                    cont_machine++;
                    internet.appendChild(id);
                    system.appendChild(internet);
                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin = 0.0;
                            linha = 50;
                            col = col + 1.0;
                        }
                    }
                }
                if (res.getType().equals("GridResource")) {
                    Element machine = descricao.createElement("machine");
                    machine.setAttribute("id", res.getName() + "");
                    machine.setAttribute("power", "100.0");
                    machine.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    machine.setAttribute("load", "0.0");
                    Element master = descricao.createElement("master");
                    master.setAttribute("scheduler", "RoundRobin");
                    for (JavaParser.ResourceChar slv : res.getResConfig().getMLIST().getMach_List()) {
                        Element slave = descricao.createElement("slave");
                        slave.setAttribute("id", idGlobal.get(slv));
                        master.appendChild(slave);
                        //adiciona o link de ida
                        Element link_gr = descricao.createElement("link");
                        link_gr.setAttribute("id", "link_" + res.getLink().getName_Link() + cont_global + "");
                        cont_global++;
                        link_gr.setAttribute("bandwidth", getDouble(res.getLink().getBaud_rate()));
                        //System.out.println("Latencia" + getDouble(res.getLink().getPropDelay()));
                        link_gr.setAttribute("latency", getDouble(res.getLink().getPropDelay()));
                        link_gr.setAttribute("load", "0.0");
                        Element connect = descricao.createElement("connect");
                        connect.setAttribute("origination", idGlobal.get(slv));
                        connect.setAttribute("destination", idGlobal.get(res));
                        link_gr.appendChild(connect);
                        Element id = descricao.createElement("icon_id");
                        id.setAttribute("global", ident_global.toString());
                        id.setAttribute("local", cont_link.toString());
                        ident_global++;
                        cont_link++;
                        link_gr.appendChild(id);
                        system.appendChild(link_gr);
                        //adiciona link de volta
                        link_gr = descricao.createElement("link");
                        link_gr.setAttribute("id", "link_" + res.getLink().getName_Link() + cont_global + "_1");
                        cont_global++;
                        link_gr.setAttribute("bandwidth", getDouble(res.getLink().getBaud_rate()));
                        link_gr.setAttribute("latency", getDouble(res.getLink().getPropDelay()));
                        link_gr.setAttribute("load", "0.0");
                        connect = descricao.createElement("connect");
                        connect.setAttribute("origination", idGlobal.get(res));
                        connect.setAttribute("destination", idGlobal.get(slv));
                        link_gr.appendChild(connect);
                        id = descricao.createElement("icon_id");
                        id.setAttribute("global", ident_global.toString());
                        id.setAttribute("local", cont_link.toString());
                        ident_global++;
                        cont_link++;
                        link_gr.appendChild(id);
                        system.appendChild(link_gr);
                    }
                    machine.appendChild(master);
                    Element pos = descricao.createElement("position");
                    pos.setAttribute("x", linha.toString());
                    pos.setAttribute("y", coluna.toString());
                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin = 0.0;
                            linha = 50;
                            col = col + 1.0;
                        }
                    }
                    machine.appendChild(pos);
                    Element id = descricao.createElement("icon_id");
                    id.setAttribute("global", idGlobal.get(res));
                    id.setAttribute("local", cont_machine.toString());
                    cont_machine++;
                    machine.appendChild(id);
                    system.appendChild(machine);
                }
            }
        }
        for (int i = 0; i < ListaMetodo.size(); i++) {
            Iterator iUser = user.iterator();
            //For para percorrer lista
            for (Map.Entry<String, JavaParser.ResourceChar> object : ListaMetodo.get(i).entrySet()) {
                String key = object.getKey();
                JavaParser.ResourceChar res = object.getValue();
                if (res.getType().equals("SimpleLink")) {
                    if (res.getOrigination() != null && res.getRouter() != null) {
                        //adiciona link de ida
                        Element link = descricao.createElement("link");
                        link.setAttribute("id", res.getName_Link() + "");
                        link.setAttribute("bandwidth", getDouble(res.getBaud_rate()));
                        link.setAttribute("latency", getDouble(res.getPropDelay()));
                        link.setAttribute("load", "0.0");
                        Element connect = descricao.createElement("connect");
                        connect.setAttribute("origination", idGlobal.get(res.getOrigination()));
                        connect.setAttribute("destination", idGlobal.get(res.getRouter()));
                        link.appendChild(connect);
                        Element id = descricao.createElement("icon_id");
                        id.setAttribute("global", ident_global.toString());
                        id.setAttribute("local", cont_link.toString());
                        ident_global++;
                        cont_link++;
                        link.appendChild(id);
                        system.appendChild(link);
                        //adiciona link de volta
                        link = descricao.createElement("link");
                        link.setAttribute("id", res.getName_Link() + "1");
                        link.setAttribute("bandwidth", getDouble(res.getBaud_rate()));
                        link.setAttribute("latency", getDouble(res.getPropDelay()));
                        link.setAttribute("load", "0.0");
                        connect = descricao.createElement("connect");
                        connect.setAttribute("destination", idGlobal.get(res.getOrigination()));
                        connect.setAttribute("origination", idGlobal.get(res.getRouter()));
                        link.appendChild(connect);
                        id = descricao.createElement("icon_id");
                        id.setAttribute("global", ident_global.toString());
                        id.setAttribute("local", cont_link.toString());
                        ident_global++;
                        cont_link++;
                        link.appendChild(id);
                        system.appendChild(link);
                    }
                }
                if (res.getType().equals("attachHost") && res.getCoreAttach().equals("ALL")) {
                    for (JavaParser.ResourceChar elem : idGlobal.keySet()) {
                        if (elem.getType().equals("GridResource") && elem.getLink() != null && res.getOrigination() != null) {
                            if (res.getOrigination() != null) {
                                //adiciona link de ida
                                Element link = descricao.createElement("link");
                                link.setAttribute("id", "link_all" + elem.getLink().getName_Link() + "");
                                link.setAttribute("bandwidth", getDouble(elem.getLink().getBaud_rate()));
                                link.setAttribute("latency", getDouble(elem.getLink().getPropDelay()));
                                link.setAttribute("load", "0.0");
                                Element connect = descricao.createElement("connect");
                                connect.setAttribute("origination", idGlobal.get(elem));
                                connect.setAttribute("destination", idGlobal.get(res.getOrigination()));
                                link.appendChild(connect);
                                Element id = descricao.createElement("icon_id");
                                id.setAttribute("global", ident_global.toString());
                                id.setAttribute("local", cont_link.toString());
                                ident_global++;
                                cont_link++;
                                link.appendChild(id);
                                system.appendChild(link);
                                //adiciona link de volta
                                link = descricao.createElement("link");
                                link.setAttribute("id", "link_all_" + elem.getLink().getName_Link() + "1");
                                link.setAttribute("bandwidth", getDouble(elem.getLink().getBaud_rate()));
                                link.setAttribute("latency", getDouble(elem.getLink().getPropDelay()));
                                link.setAttribute("load", "0.0");
                                connect = descricao.createElement("connect");
                                connect.setAttribute("destination", idGlobal.get(elem));
                                connect.setAttribute("origination", idGlobal.get(res.getOrigination()));
                                link.appendChild(connect);
                                id = descricao.createElement("icon_id");
                                id.setAttribute("global", ident_global.toString());
                                id.setAttribute("local", cont_link.toString());
                                ident_global++;
                                cont_link++;
                                link.appendChild(id);
                                system.appendChild(link);
                            }
                        }
                    }
                } else if (res.getType().equals("attachHost") && res.getCoreAttach() instanceof JavaParser.ResourceChar) {
                    Element link = descricao.createElement("link");
                    link.setAttribute("id", "link_temp" + cont_global);
                    cont_global++;
                    link.setAttribute("bandwidth", "1000");//res.getLink().getBaud_rate());
                    link.setAttribute("latency", "0.001");//res.getLink().getPropDelay());
                    link.setAttribute("load", "0.0");
                    Element connect = descricao.createElement("connect");
                    connect.setAttribute("origination", idGlobal.get(res.getOrigination()));
                    connect.setAttribute("destination", idGlobal.get(res.getCoreAttach()));
                    link.appendChild(connect);
                    Element id = descricao.createElement("icon_id");
                    id.setAttribute("global", ident_global.toString());
                    id.setAttribute("local", cont_link.toString());
                    ident_global++;
                    cont_link++;
                    link.appendChild(id);
                    system.appendChild(link);
                    //adiciona link de volta
                    link = descricao.createElement("link");
                    link.setAttribute("id", "link_temp" + cont_global);
                    cont_global++;
                    link.setAttribute("bandwidth", "1000");//res.getLink().getBaud_rate());
                    link.setAttribute("latency", "0.001");//res.getLink().getPropDelay());
                    link.setAttribute("load", "0.0");
                    connect = descricao.createElement("connect");
                    connect.setAttribute("destination", idGlobal.get(res.getOrigination()));
                    connect.setAttribute("origination", idGlobal.get(res.getCoreAttach()));
                    link.appendChild(connect);
                    id = descricao.createElement("icon_id");
                    id.setAttribute("global", ident_global.toString());
                    id.setAttribute("local", cont_link.toString());
                    ident_global++;
                    cont_link++;
                    link.appendChild(id);
                    system.appendChild(link);
                } else if (res.getType().equals("attachHost")) {
                    Element ident = descricao.createElement("icon_id");
                    String mestreGlobal = ident_global.toString();
                    ident.setAttribute("global", ident_global.toString());
                    ident.setAttribute("local", cont_machine.toString());
                    ident_global++;
                    cont_machine++;
                    Element machine = descricao.createElement("machine");
                    machine.setAttribute("id", res.getName() + "mestre");
                    machine.setAttribute("power", "100.0");
                    machine.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    machine.setAttribute("load", "0.0");
                    Element master = descricao.createElement("master");
                    master.setAttribute("scheduler", "RoundRobin");
                    //adiciona escravos
                    for (JavaParser.ResourceChar slv : idGlobal.keySet()) {
                        if (slv.getType().equals("GridResource")) {
                            Element slave = descricao.createElement("slave");
                            slave.setAttribute("id", idGlobal.get(slv));
                            master.appendChild(slave);
                        }
                    }
                    //adiciona o link de ida
                    Element link_gr = descricao.createElement("link");
                    link_gr.setAttribute("id", "link_temp" + cont_global);
                    cont_global++;
                    link_gr.setAttribute("bandwidth", "1000");//res.getLink().getBaud_rate());
                    link_gr.setAttribute("latency", "0.001");//res.getLink().getPropDelay());
                    link_gr.setAttribute("load", "0.0");
                    Element connect = descricao.createElement("connect");
                    connect.setAttribute("origination", mestreGlobal);
                    connect.setAttribute("destination", idGlobal.get(res.getOrigination()));
                    link_gr.appendChild(connect);
                    Element id = descricao.createElement("icon_id");
                    id.setAttribute("global", ident_global.toString());
                    id.setAttribute("local", cont_link.toString());
                    ident_global++;
                    cont_link++;
                    link_gr.appendChild(id);
                    system.appendChild(link_gr);
                    //adiciona link de volta
                    link_gr = descricao.createElement("link");
                    link_gr.setAttribute("id", "link_temp" + cont_global + "");
                    cont_global++;
                    link_gr.setAttribute("bandwidth", "1000");//res.getLink().getBaud_rate());
                    link_gr.setAttribute("latency", "0.001");//res.getLink().getPropDelay());
                    link_gr.setAttribute("load", "0.0");
                    connect = descricao.createElement("connect");
                    connect.setAttribute("origination", idGlobal.get(res.getOrigination()));
                    connect.setAttribute("destination", mestreGlobal);
                    link_gr.appendChild(connect);
                    id = descricao.createElement("icon_id");
                    id.setAttribute("global", ident_global.toString());
                    id.setAttribute("local", cont_link.toString());
                    ident_global++;
                    cont_link++;
                    link_gr.appendChild(id);
                    system.appendChild(link_gr);
                    machine.appendChild(master);
                    Element pos = descricao.createElement("position");
                    pos.setAttribute("x", linha.toString());
                    pos.setAttribute("y", coluna.toString());
                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin = 0.0;
                            linha = 50;
                            col = col + 1.0;
                        }
                    }
                    machine.appendChild(pos);
                    machine.appendChild(ident);
                    system.appendChild(machine);
                }

            }
        }
        system.appendChild(load);
        //IconicoXML.escrever(descricao, new File("/home/gabriel/arq.imsx"));
        return descricao;
    }

    public int getW() {
        return 1500;
    }

    public int getH() {
        return 1500;
    }
    
    private String getDouble(String valor) {
        Random random = new Random();
        if(valor.equals("random")){
            String Low, high;
            //Low = JOptionPane.showInputDialog("Digite o limite inferior do número randômico a ser gerado");
            //high = JOptionPane.showInputDialog("Digite o limite superior do número randômico a ser gerado");
            Low = "500000";
            high = "1000000";
            double med, hi, low;
            hi = Double.parseDouble(high);
            low = Double.parseDouble(Low);
            med = 750000;
            double prob = 1;
            double a;
            double b;
            double tsu;
            double u;
            Random randomico = new Random();
            u = randomico.nextDouble();
            if (u <= prob) { /* uniform(low , med) */
                a = low;
                b = med;
            } else { /* uniform(med , hi) */
                a = med;
                b = hi;
            }

            //generate a value of a random variable from distribution uniform(a,b) 
            tsu = (random.nextDouble() * (b - a)) + a;
            return String.valueOf(Math.abs(tsu));
        }
        else{
            return valor;
        }
    }
    
    private String getInt(String valor) {
        Random random = new Random();
        if(valor.equals("random")){
            return String.valueOf(Math.abs(random.nextInt()));
        }else{
            return valor;
        }
    }
}
