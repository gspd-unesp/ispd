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
 * TraceXML.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author: Diogo Tavares;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.arquivo.xml;

import ispd.motor.filas.Tarefa;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diogo Tavares
 */
public class TraceXML {

    private String caminho, tipo, saida;
    private int num_tasks;

    public TraceXML(String caminho) {
        this.caminho = caminho;
        String aux;
        int i = caminho.lastIndexOf(".");
        this.saida = (caminho.substring(0, i) + ".wmsx");
        this.tipo = caminho.substring(i + 1).toUpperCase();
        System.out.println(this.caminho + "-" + this.saida + "-" + this.tipo);
        
    }

    public String getSaida() {
        return this.saida;
    }
    
    public String getTipo(){
        return this.tipo;
    }
    public int getNum_Tasks(){
        return num_tasks;
    }
    
    public void convert() {
        // TODO code application logic here
        try {
            BufferedReader in = new BufferedReader(new FileReader(caminho));
            BufferedWriter out = new BufferedWriter(new FileWriter(saida));
            
            String str;
            int task1_arrive = 0;
            boolean flag = false;
            //iniciando a escrita do arquivo
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE system SYSTEM \"iSPDcarga.dtd\">");
            out.write("\n<system>");
            out.write("\n<trace>");
            out.write("\n<format kind=\"" + tipo + "\" />");
            int i = 0;
            while (in.ready()) {
                str = in.readLine();
                if (str.equals("") || str.charAt(0) == ';' || str.charAt(0) == '#') {/*System.out.println("linha em branco");*/

                } else if ("SWF".equals(tipo)) {
                    p_tasksSWF(str, out);
                    i++;
                } else if ("GWF".equals(tipo)) {
                    if (!flag) {
                        task1_arrive = acha1task(str);
                        flag = true;
                        p_tasksGWF(str, out, task1_arrive);
                        i++;
                    } else {
                        p_tasksGWF(str, out, task1_arrive);
                        i++;
                    }
                }
            }
            out.write("\n</trace>");
            out.write("\n</system>");

            //FECHANDO ARQUIVOS
            num_tasks = i;
            in.close();
            out.close();
        } catch (IOException e) {
        }
    }

    private static void p_tasksGWF(String str, BufferedWriter out, int first_task) throws IOException {
        //System.out.println(str);
        str = str.replaceAll("\t", " ");//elimina espaços em branco desnecessários
        str = str.trim();
        //System.out.println(str);

        String[] campos = str.split(" ");
        if (campos[3].equals("-1")) {
        } else {
            out.write("\n<task ");
            out.write("id=" + "\"" + campos[0]
                    + "\" arr=\"" + (Integer.parseInt(campos[1]) - first_task)
                    + "\" sts=\"" + campos[10]
                    + "\" cpsz =\"" + campos[3]
                    + "\" cmsz=\"" + campos[20]
                    + "\" usr=\"" + campos[11]);
            out.write("\" />");
        }
    }

    private static void p_tasksSWF(String str, BufferedWriter out) throws IOException {
        str = str.replaceAll("\\s\\s++", " ");//elimina espaços em branco desnecessários
        str = str.trim();
        out.write("\n<task ");
        String[] campos = str.split(" ");
        out.write("id=" + "\"" + campos[0]
                + "\" arr=\"" + campos[1]
                + "\" sts=\"" + campos[10]
                + "\" cpsz =\"" + campos[3]
                + "\" cmsz=\"-1"
                + "\" usr=\"" + "user" + campos[11]);
        out.write("\" />");
    }

    private int acha1task(String str) {
        str = str.replaceAll("\t", " ");//elimina tabs desnecessários
        str = str.trim();
        //System.out.println(str);
        String[] campos = str.split(" ");
        int a = Integer.parseInt(campos[1]);
        System.out.println(a);
        return a;
    }

    @Override
    public String toString() {
        int i = saida.lastIndexOf("\\");
        saida = saida.substring(i + 1);
        return ("File " + saida + " was generated sucessfully:\n"
                + "\t- Generated from the format: "+ tipo 
                + "\n\t- File has a workload of " + num_tasks + " tasks");

    }

    public String LerCargaWMS() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(caminho));
            String texto = "";
            int j = caminho.lastIndexOf("\\");
            //pega o nome do arquivo
            String nome;
            nome = caminho.substring(j + 1);
            texto = texto.concat("File " + nome + " was opened sucessfully:\n");
            String aux;
            int i = 0;
            while (in.ready()) {
                aux = in.readLine();
                if (i == 4) {
                    String[] campos = aux.split(" ");
                    campos = campos[1].split("\"");
                    texto = texto.concat("\t- File was extracted of trace in the format: " + campos[1] + "\n");
                    this.tipo = campos[1];
                }
                i++;
            }
            //desconta as 7 linhas de tags que não são tarefas..
            i -= 7;
            num_tasks=i;
            texto = texto.concat("\t- File has a workload of " + i + " tasks");
            return (texto);

        } catch (IOException ex) {
            Logger.getLogger(TraceXML.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ("File has no correct format");
    }

    public void geraTraceSim(List<Tarefa> tarefas) {
        try {
            tipo = "iSPD";
            FileWriter fp = new FileWriter(caminho);
            BufferedWriter out = new BufferedWriter(fp);
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE system SYSTEM \"iSPDcarga.dtd\">");
            out.write("\n<system>");
            out.write("\n<trace>");
            out.write("\n<format kind=\"" + tipo + "\" />\n");
            int i = 0;
            for (Tarefa tarefa : tarefas) {
                if (tarefa.isCopy() == false) {
                    out.write("<task " + "id=\"" + tarefa.getIdentificador()
                            + "\" arr=\"" + tarefa.getTimeCriacao()
                            + "\" sts=\"" + "1"
                            + "\" cpsz =\"" + tarefa.getTamProcessamento()
                            + "\" cmsz=\"" + tarefa.getArquivoEnvio()
                            + "\" usr=\"" + tarefa.getProprietario());
                    out.write("\" />\n");
                    i++;
                }
            }
            out.write("</trace>");
            out.write("\n</system>");
            
            num_tasks = i;
            saida = caminho;
            out.close();
            fp.close();
        } catch (IOException ex) {
            System.out.println("ERROR");
        }
    }
}


