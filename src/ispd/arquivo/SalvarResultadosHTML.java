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
 * SalvarResultadosHTML.java
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
package ispd.arquivo;

import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasGlobais;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

/**
 * Classe responsável por armazenar resultados obtidos da simulação e
 * transformar em um arquivo html
 *
 * @author denison
 */
public class SalvarResultadosHTML {

    private String globais;
    private String satisfacao;
    private String tarefas;
    private String tabela;
    private String chartstxt;
    private BufferedImage charts[];

    /**
     * Cria String com tabela com os resultados de cada centro de serviços
     * @param tabela contêm as seguintes colunas: [Label] [Owner] [Processing performed] [Communication performed]
     */
    public void setTabela(Object tabela[][]) {
        //Adicionando resultados na tabela do html
        this.tabela = "<table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" style=\"width: 80%;\">\n"
                    + "            <thead>\n"
                    + "                <tr>\n"
                    + "                    <th scope=\"col\">\n"
                    + "                        <span style=\"color:#800000;\">Label</span></th>\n"
                    + "                    <th scope=\"col\">\n"
                    + "                        <span style=\"color:#800000;\">Owner</span></th>\n"
                    + "                    <th scope=\"col\">\n"
                    + "                        <span style=\"color:#800000;\">Processing performed</span></th>\n"
                    + "                    <th scope=\"col\">\n"
                    + "                        <span style=\"color:#800000;\">Communication&nbsp;performed</span></th>\n"
                    + "                </tr>\n"
                    + "            </thead>\n"
                    + "            <tbody>\n";
        for (Object[] item : tabela) {
            this.tabela += "                <tr><td>" + item[0] + "</td><td>" + item[1] + "</td><td>" + item[2] + "</td><td>" + item[3] + "</td></tr>\n";
        }
        this.tabela += "            </tbody>\n"
                     + "        </table>\n";
    }

    /**
     * Armazena as imagens dos gráficos e cria string com a ligação dos arquivos para html
     * @param charts vetor com imagens que devem ser salvas juntos com o html
     */
    public void setCharts(BufferedImage charts[]) {
        int cont = 0;
        for (BufferedImage item : charts) {
            if (item != null) {
                cont++;
            }
        }
        this.charts = new BufferedImage[cont];
        cont = 0;
        this.chartstxt = "";
        for (BufferedImage item : charts) {
            if (item != null) {
                this.charts[cont] = item;
                this.chartstxt += "<img alt=\"\" src=\"chart" + cont + ".png\" style=\"width: 600px; height: 300px;\" />\n";
                cont++;
            }
        }

    }

    /**
     * Cria string com satisfação do usuário
     * @param satisfacao matriz contendo: [nome do usuário][satifação]
     */
    public void setSatisfacao(Object[][] satisfacao) {
        if (satisfacao.length > 1) {
            this.satisfacao = "<ul>";
            for (int i = 0; i < satisfacao.length; i++) {
                this.satisfacao += "<li><strong>User" + satisfacao[i][0] + "</strong> = " + satisfacao[i][1] + " %</li>\n";
            }
            this.satisfacao += "</ul>";
        }
    }

    /**
     * Gera String com as métricas globais
     * @param globais metricas globais obtidas da simulação
     */
    public void setMetricasGlobais(MetricasGlobais globais) {
        this.globais = "<li><strong>Total Simulated Time </strong>= " + globais.getTempoSimulacao() + "</li>\n";
        if (satisfacao == null) {
            this.globais += "<li><strong>Satisfaction</strong> = " + globais.getSatisfacaoMedia() + " %</li>\n";
        } else {
            this.globais += "<li><strong>Satisfaction</strong>" + satisfacao + "</li>\n";
        }
        this.globais += "<li><strong>Idleness of processing resources</strong> = " + globais.getOciosidadeComputacao() + " %</li>\n"
                + "<li><strong>Idleness of communication resources</strong> = " + globais.getOciosidadeComunicacao() + " %</li>\n"
                + "<li><strong>Efficiency</strong> = " + globais.getEficiencia() + " %</li>\n";
        if (globais.getEficiencia() > 70.0) {
            this.globais += "<li><span style=\"color:#00ff00;\"><strong>Efficiency GOOD</strong></span></li>\n";
        } else if (globais.getEficiencia() > 40.0) {
            this.globais += "<li><strong>Efficiency MEDIA</strong></li>\n ";
        } else {
            this.globais += "<li><span style=\"color:#ff0000;\"><strong>Efficiency BAD</strong></span></li>\n";
        }
    }

    /**
     * Cria String com as métricas dos clientes na rede de filas
     * @param metricas 
     */
    public void setMetricasTarefas(Metricas metricas) {
        double tempoMedioSistemaComunicacao = metricas.getTempoMedioFilaComunicacao() + metricas.getTempoMedioComunicacao();
        double tempoMedioSistemaProcessamento = metricas.getTempoMedioFilaProcessamento() + metricas.getTempoMedioProcessamento();
        this.tarefas = "<ul><li><h2>Tasks</h2><ul><li><strong>Communication</strong><ul>\n"
                + "<li>Queue average time: " + metricas.getTempoMedioFilaComunicacao() + " seconds.</li>\n"
                + "<li>Communication average time: " + metricas.getTempoMedioComunicacao() + " seconds.</li>\n"
                + "<li>System average time: " + tempoMedioSistemaComunicacao + " seconds.</li>\n"
                + "</ul></li><li><strong>Processing</strong><ul>\n"
                + "<li>Queue average time: " + metricas.getTempoMedioFilaProcessamento() + " seconds.</li>\n"
                + "<li>Processing average time: " + metricas.getTempoMedioProcessamento() + " seconds.</li>\n"
                + "<li>System average time: " + tempoMedioSistemaProcessamento + " seconds.</li></ul></li></ul></li></ul>";
    }

    /**
     * Cria texto da descrição completa em html contendo os resultados inseridos
     * @return texto completo do html
     */
    public String getHTMLText() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Simulation Results</title>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "    </head>\n"
                + "    <body background=\"fundo_html.jpg\" style=\"background-position: top center; background-repeat: no-repeat;\">\n"
                + "        <h1 id=\"topo\" style=\"text-align: center;\">\n"
                + "            <span style=\"color:#8b4513;\">\n"
                + "            <img alt=\"\" src=\"Logo_iSPD_128.png\" align=\"left\" style=\"width: 70px; height: 70px;\" />\n"
                + "            Simulation Results</span>\n"
                + "            <img alt=\"\" src=\"Logo_UNESP.png\" align=\"right\" style=\"width: 70px; height: 70px;\" />\n"
                + "        </h1>\n"
                + "        <hr /><br />\n"
                + "        <div>\n"
                + "            <a href=\"#global\">Global metrics</a> <br/>\n"
                + "            <a href=\"#table\">Table of Resource</a> <br/>\n"
                + "            <a href=\"#chart\">Charts</a> <br/>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"global\" style=\"text-align: center;\">\n"
                + "            Global metrics</h2>\n"
                + "        " + globais + tarefas
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"table\" style=\"text-align: center;\">\n"
                + "            Table of Resource\n"
                + "        </h2>\n"
                +          tabela
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"chart\" style=\"text-align: center;\">\n"
                + "            Charts\n"
                + "        </h2>\n"
                + "        <p style=\"text-align: center;\">\n"
                + "        " + chartstxt
                + "        </p>\n"
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <p style=\"font-size:10px;\">\n"
                + "            <a href=\"http://gspd.dcce.ibilce.unesp.br/\">GSPD</a></p>\n"
                + "    </body>\n"
                + "</html>";
    }

    /**
     * Cria diretório contendo todos os arquivos necessários para abrir um html com os resultados da simulação
     * @param diretorio diretório para criar arquivos
     * @throws IOException gera uma exceção se não for possivel criar diretório indicado
     */
    public void gerarHTML(File diretorio) throws IOException {
        if (!diretorio.exists()) {
            if (!diretorio.mkdir()) {
                throw new IOException("Could not create directory");
            }
        }
        File arquivo = new File(diretorio, "result.html");
        FileWriter writer;
        writer = new FileWriter(arquivo);
        PrintWriter saida = new PrintWriter(writer, true);
        saida.print(this.getHTMLText());
        saida.close();
        writer.close();
        for (int i = 0; i < charts.length; i++) {
            arquivo = new File(diretorio, "chart" + i + ".png");
            ImageIO.write(charts[i], "png", arquivo);
        }
        arquivo = new File(diretorio, "fundo_html.jpg");
        if (!arquivo.exists()) {
            ImageIO.write(ImageIO.read(ispd.gui.JPrincipal.class.getResource("imagens/fundo_html.jpg")), "jpg", arquivo);
        }
        arquivo = new File(diretorio, "Logo_iSPD_128.png");
        if (!arquivo.exists()) {
            ImageIO.write(ImageIO.read(ispd.gui.JPrincipal.class.getResource("imagens/Logo_iSPD_128.png")), "png", arquivo);
        }
        arquivo = new File(diretorio, "Logo_UNESP.png");
        if (!arquivo.exists()) {
            ImageIO.write(ImageIO.read(ispd.gui.JPrincipal.class.getResource("imagens/Logo_UNESP.png")), "png", arquivo);
        }
    }
}