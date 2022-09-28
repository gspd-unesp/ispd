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
 * Terminal.java
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
package ispd.application;

import ispd.arquivo.xml.IconicoXML;
import ispd.gui.ResultsDialog;
import ispd.motor.ProgressoSimulacao;
import ispd.motor.Simulation;
import ispd.motor.SimulacaoParalela;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasGlobais;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Classe de controle para chamada do terminal.
 * Atende aos seguintes parametros:
 * java -jar iSPD.jar [option] [model_file.imsx]
 * [option] pode ser um ou mais:
 *     -n  [number]   number of simulation
 *     -th [number]   number of threads
 *     -p             Optimistic parallel simulation
 *     -o [directory] directory to save html output
 *     -h             print this help message
 *     -v             print the version message
 * @author denison
 */
public class TerminalApplication implements Application
{

    private final int HELP = 0;
    private final int VERSION = 1;
    private final int SIMULATE = 2;
    private final int CLIENT = 3;
    private final int SERVER = 4;

    /**
     * Arquivo contendo o modelo que será simulados
     */
    private File inputFile;
    /**
     * Diretório no qual será salvo html com os resultados da simulação
     */
    private File outputFolder = null;
    /**
     * Arquivo de configuração para um processo cliente executar simulações em rede
     */
    final private File configuracao;
    final private int mode;
    private int numExecucoes;
    final private int numThreads;
    private ProgressoSimulacao progrSim;
    private boolean paralelo = false;
    private boolean optimisticParallel = false;
    private boolean visible = true;
    final private int port;
    //Resultados
    MetricasGlobais resuladosGlobais;
    final private CommandLine cmd;
    final private Options options;

    private CommandLine commandLinePreparation(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(options, args);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return null;
    }

    private Options getAllOptions() {
        Options options = new Options();


        options.addOption("h", "help",
                false, "print this help message.");
        options.addOption("v", "version",
                false, "print the version of iSPD.");
        options.addOption("s", "server",
                false, "run iSPD as a server.");
        options.addOption("P", "port",
                true, "specify a port.");
        options.addOption("t", "threads",
                true, "specify the number of threads.");
        options.addOption("conf", "conf",
                true, "specify a configuration file.");
        options.addOption("in", "input",
                true, "specify the input file of the\n" +
                        "model to simulate.");
        options.addOption("o", "output", true,
                        "specify an output folder for the html export.");

        return options;
    }

    /**
     * Get a value from an option from the command line.
     *
     * @param cmd The CommandLine that is being used.
     * @param op The string relative to the argument from the command line.
     * @return The value of the option (if it exists and is valid).
     */
    private int setValueFromOption(CommandLine cmd, String op) {
        try {
            return Integer.parseInt(cmd.getOptionValue(op));
        } catch (NumberFormatException e) {
            System.out.println(e);
            System.exit(1);
        }

        return 0;
    }

    /**
     * Set the mode for running the terminal application.
     *
     * @param cmd CommandLine used in the application.
     * @return An int representing a mode based on the options.
     */
    private int setMode(CommandLine cmd) {
        if (cmd.hasOption("h")) {
            return HELP;

        } else if (cmd.hasOption("v")) {
            return VERSION;

        } else if (cmd.hasOption("s")) {
            return SERVER;

        } else if (cmd.hasOption("c")) {
            return CLIENT;

        } else {
            return SIMULATE;
        }
    }

    /**
     * Pre run of the terminal application, adding the necessary flags to the
     * class before it runs.
     *
     * @param args Arguments from the command line.
     */
    public TerminalApplication (String[] args) {
        this.options = getAllOptions();
        this.cmd = commandLinePreparation(options, args);
        this.mode = setMode(cmd);

        if (cmd.hasOption("P")) {
            this.port = setValueFromOption(cmd, "P");
        } else {
            this.port = 2004;
        }

        if (cmd.hasOption("t")) {
            int threads = setValueFromOption(cmd, "t");

            if (threads > this.numExecucoes) {
                this.numThreads = this.numExecucoes;
            } else {
                this.numThreads = threads;
            }
        } else {
            this.numThreads = 1;
        }

        if (cmd.hasOption("conf")) {
            this.configuracao = new File(cmd.getOptionValue("conf"));
        } else {
            this.configuracao = null;
        }

        List<String> restArgs;
        if (cmd.hasOption("in")) {
            this.inputFile = new File(cmd.getOptionValue("in"));
        } else {
            restArgs = cmd.getArgList();
            if (!restArgs.isEmpty()) {
                this.inputFile = new File(restArgs.get(0));
            }
        }

        if (cmd.hasOption("o")) {
            this.outputFolder = new File(cmd.getOptionValue("o"));
        } else {
            this.outputFolder = null;
        }

        this.numExecucoes = 1;

//        } else if (args[0].equals("-client")) {
//            opcao = 3;
//            modo = CLIENT;
//            numThreads = 1;
//            configuracao = new File(args[1]);
//            arquivoIn = new File(args[2]);
//        } else {
//            int atual = 0;
//            numThreads = 1;
//            numExecucoes = 1;
//            modo = SIMULATE;
//            while (args[atual].charAt(0) == '-') {
//                if (args[atual].equals("-n")) {
//                    numExecucoes = Integer.parseInt(args[atual + 1]);
//                    atual += 2;
//                } else if (args[atual].equals("-th")) {
//                    numThreads = Integer.parseInt(args[atual + 1]);
//                    atual += 2;
//                } else if (args[atual].equals("-o")) {
//                    atual++;
//                    String dirSaida = args[atual];
//                    while (args[atual].charAt(args[atual].length() - 1) == '\\') {
//                        atual++;
//                        dirSaida += " " + args[atual];
//                    }
//                    arquivoOut = new File(dirSaida);
//                    atual++;
//                } else if (args[atual].equals("-p")) {
//                    paralelo = true;
//                    atual++;
//                } else {
//                    atual++;
//                }
//            }
//            opcao = 1;
//            String nomeArquivo = args[atual];
//            for (int i = atual + 1; i < args.length; i++) {
//                nomeArquivo = nomeArquivo + " " + args[i];
//            }
//            arquivoIn = new File(nomeArquivo);
//        }
    }

    /**
     * Inicia atendimento de acordo com parametros
     */
    @Override
    public void run () {
        HelpFormatter helpFormatter = new HelpFormatter();

        switch (mode) {
            case HELP:
                helpFormatter.printHelp("java -jar iSPD.jar", options);
//                System.out.println("Usage: java -jar iSPD.jar");
//                System.out.println("\t\t(to execute the graphical interface of the iSPD)");
//                System.out.println("\tjava -jar iSPD.jar [option] [model file.imsx]");
//                System.out.println("\t\t(to execute a model in terminal)");
//                System.out.println("where options include:");
//                System.out.println("\t-n <number>\tnumber of simulation");
//                System.out.println("\t-th <number>\tnumber of threads");
//                System.out.println("\t-p \tOptimistic parallel simulation");
//                System.out.println("\t-o <directory>\tdirectory to save html output");
//                System.out.println("\t-server <port>");
//                System.out.println("\t-client <> <model file.imsx>");
//                System.out.println("\t-help\tprint this help message");
                break;
            case VERSION:
                System.out.println("""
                        iSPD version 3.1
                          Iconic Simulator of Parallel and Distributed System
                          Copyright 2010-2022, by GSPD from UNESP.
                          Project Info: https://dcce.ibilce.unesp.br/spd""");
                break;
            case SIMULATE:

                progrSim = new ProgressoSimulacao() {
                    @Override
                    public void incProgresso(int n) {
                    }

                    @Override
                    public void print(String text, Color cor) {
                        if (visible) {
                            System.out.print(text);
                        }
                    }
                };
                if (inputFile.getName().endsWith(".imsx") && inputFile.exists()) {
                    if (numThreads > 1 && !paralelo) {
                        this.simularParalelo();
                    } else {
                        this.simularSequencial();
                    }
                } else {
                    System.out.println("iSPD can not open the file: " + inputFile.getName());
                }
                break;
            case SERVER:
                this.progrSim = new ProgressoSimulacao() {
                    @Override
                    public void incProgresso(int n) {

                    }

                    @Override
                    public void print(String text, Color cor) {

                    }
                };

                this.simularRedeServidor();
                break;
            case CLIENT:
                Object conf[] = lerConfiguracao(configuracao);
                String server[] = new String[conf.length / 3];//{"localhost","localhost"};
                int ports[] = new int[conf.length / 3];//{2005,2006};
                int numSim[] = new int[conf.length / 3];//{15,15};
                for (int i = 0, j = 0; i < server.length; i++) {
                    server[i] = (String) conf[j];
                    j++;
                    ports[i] = Integer.parseInt(conf[j].toString());
                    j++;
                    numSim[i] = Integer.parseInt(conf[j].toString());
                    j++;
                }

                this.progrSim = new ProgressoSimulacao() {
                    @Override
                    public void incProgresso(int n) {
                    }

                    @Override
                    public void print(String text, Color cor) {
                        if (visible) {
                            System.out.print(text);
                        }
                    }
                };
                this.simularRedeCliente(server, ports, numSim);
                break;
        }
        System.exit(0);
    }

    /**
     * Executa n simulações do modelo passado por parametro de forma sequencial
     * (usando o motor de execução padrão)
     */
    private void simularSequencial() {
        progrSim.println("Simulation Initiated.");
        try {
            progrSim.print("Opening iconic model.");
            progrSim.print(" -> ");
            Document modelo = IconicoXML.ler(inputFile);
            progrSim.println("OK", Color.green);
            //Verifica se foi construido modelo corretamente
            progrSim.validarInicioSimulacao(modelo);
            //Escrever Modelo
            //this.modelo(redeDeFilas);
            //criar tarefas

            Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo));
            resuladosGlobais = new MetricasGlobais();
            double total = 0;

            for (int i = 1; i <= numExecucoes; i++) {
                double t1 = System.currentTimeMillis();
                progrSim.println("* Simulation " + i);
                //criar grade
                progrSim.print("  Mounting network queue.");
                progrSim.print(" -> ");
                RedeDeFilas redeDeFilas = IconicoXML.newRedeDeFilas(modelo);
                progrSim.println("OK", Color.green);
                progrSim.print("  Creating tasks.");
                progrSim.print(" -> ");
                List<Tarefa> tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilas);
                progrSim.print("OK\n  ", Color.green);
                //Verifica recursos do modelo e define roteamento
                Simulation sim;
                if (!paralelo) {
                    sim = new SimulacaoSequencial(progrSim, redeDeFilas, tarefas);//[10%] --> 55 %
                } else {
                    System.out.println("Execução paralela da simulação");
                    sim = new SimulacaoParalela(progrSim, redeDeFilas, tarefas, numThreads);
                }
                sim.createRouting();
                //Realiza asimulação
                progrSim.println("  Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a simulação
                sim.simulate();//[30%] --> 85%
                if (outputFolder == null) {
                    resuladosGlobais.add(new MetricasGlobais(redeDeFilas, sim.getTime(null), tarefas));
                } else {
                    Metricas temp = sim.getMetrics();
                    metricas.addMetrica(temp);
                }
                //Recebe instante de tempo em milissegundos ao fim da execução da simulação
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                total += tempototal;
                progrSim.println("  Simulation Execution Time = " + tempototal + "seconds");
            }
            if (numExecucoes > 1 && outputFolder != null) {
                metricas.calculaMedia();
                resuladosGlobais = metricas.getMetricasGlobais();
            }
            progrSim.println("Results:");
            if (numExecucoes > 1) {
                progrSim.println("  Total Simulation Execution Time = " + total + "seconds");
            }
            if (outputFolder != null) {
                resuladosGlobais = metricas.getMetricasGlobais();
                double t1 = System.currentTimeMillis();
                ResultsDialog result = new ResultsDialog(metricas);
                result.salvarHTML(outputFolder);
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                progrSim.println("  Time to create html = " + tempototal + "seconds");
            }
            progrSim.println(resuladosGlobais.toString());
        } catch (Exception erro) {
            progrSim.println(erro.getMessage(), Color.red);
            progrSim.print("Simulation Aborted", Color.red);
            progrSim.println("!", Color.red);
        }
    }

    /**
     * Executa n simulações do modelo passado por parametro de forma paralela
     * (usando o motor de execução padrão em várias threads, ou usando o motor experimental de simulação otimista)
     */
    private void simularParalelo() {
        progrSim.println("Simulation Initiated.");
        try {
            progrSim.print("Opening iconic model.");
            progrSim.print(" -> ");
            progrSim.incProgresso(5);//[5%] --> 5%
            progrSim.println("OK", Color.green);
            //Escrever Modelo
            //this.modelo(redeDeFilas);
            //criar tarefas

            Document[] modelo = IconicoXML.clone(inputFile, numThreads);
            Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo[0]));
            //Verifica se foi construido modelo corretamente
            progrSim.validarInicioSimulacao(modelo[0]);
            int inicio = 0, incremento = numExecucoes / numThreads;
            RunnableImpl[] trabalhador = new RunnableImpl[numThreads];
            Thread[] thread = new Thread[numThreads];
            System.out.println("Será executado com " + numThreads + " threads");
            visible = false;
            double t1 = System.currentTimeMillis();
            for (int i = 0; i < numThreads - 1; i++) {
                trabalhador[i] = new RunnableImpl(modelo[i], inicio, incremento);
                thread[i] = new Thread(trabalhador[i]);
                thread[i].start();
                inicio += incremento;
            }
            trabalhador[numThreads - 1] = new RunnableImpl(modelo[numThreads - 1], inicio, numExecucoes - inicio);
            thread[numThreads - 1] = new Thread(trabalhador[numThreads - 1]);
            thread[numThreads - 1].start();
            for (int i = 0; i < numThreads; i++) {
                thread[i].join();
            }
            visible = true;
            double t2 = System.currentTimeMillis();
            //Calcula tempo de simulação em segundos
            double tempototal = (t2 - t1) / 1000;
            progrSim.println("  Total Simulation Execution Time = " + tempototal + "seconds");
            progrSim.print("Getting Results.");
            progrSim.print(" -> ");
            if (numExecucoes > 1 && outputFolder != null) {
                for (int i = 0; i < numThreads; i++) {
                    metricas.addMetrica(trabalhador[i].getMetricas());
                }
                metricas.calculaMedia();
                resuladosGlobais = metricas.getMetricasGlobais();
            } else {
                resuladosGlobais = new MetricasGlobais();
                for (int i = 0; i < numThreads; i++) {
                    resuladosGlobais.add(trabalhador[i].getMetricasGlobais());
                }
            }
            progrSim.println("OK");
            progrSim.println("Results:");
            if (outputFolder != null) {
                t1 = System.currentTimeMillis();
                ResultsDialog result = new ResultsDialog(metricas);
                result.salvarHTML(outputFolder);
                t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                tempototal = (t2 - t1) / 1000;
                progrSim.println("  Time to create html = " + tempototal + "seconds");
            }
            progrSim.println(resuladosGlobais.toString());
        } catch (Exception erro) {
            progrSim.println(erro.getMessage(), Color.red);
            progrSim.print("Simulation Aborted", Color.red);
            progrSim.println("!", Color.red);
        }
    }

    /**
     * Simulação cliente servidor
     * Executa as ações do servidor, porem ainda não é a versão definitiva
     */
    private void simularRedeServidor() {
        Document modelo = null;
        Metricas metricas = new Metricas(null);
        String origem = null;
        //Recebendo Modelo
        try {
            System.out.println("Creating a server socket");
            ServerSocket providerSocket = new ServerSocket(this.port, 10);
            System.out.println("Waiting for connection");
            Socket connection = providerSocket.accept();
            origem = connection.getInetAddress().getHostName();
            System.out.println("Connection received from " + origem);
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            System.out.println("Recebendo mensagem");
            numExecucoes = (Integer) in.readObject();
            System.out.println("Será feitas " + numExecucoes + " simulações");
            modelo = (Document) in.readObject();
            in.close();
            System.out.println("Closing connection");
            providerSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Executando simulação
        if (numThreads <= 1) {
            System.out.println("Será realizado " + numExecucoes + " simulações.");
            double t1 = System.currentTimeMillis();
            for (int i = 1; i <= numExecucoes; i++) {
                RedeDeFilas redeDeFilas = IconicoXML.newRedeDeFilas(modelo);
                List<Tarefa> tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilas);
                Simulation sim = new SimulacaoSequencial(progrSim, redeDeFilas, tarefas);//[10%] --> 55 %
                sim.createRouting();
                sim.simulate();//[30%] --> 85%
                Metricas temp = sim.getMetrics();
                metricas.addMetrica(temp);
            }
            double t2 = System.currentTimeMillis();
            double tempototal = (t2 - t1) / 1000;
            System.out.println("  Simulation Execution Time = " + tempototal + "seconds");
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        //Enviando Resultados
        System.out.println("Devolvendo resultados...");
        try {
            System.out.println("Creating a server socket");
            Socket requestSocket = new Socket(origem, 2004);
            System.out.println("Connection received from " + requestSocket.getInetAddress().getHostName());
            ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
            System.out.println("Mensagem... resultados obtidos: " + metricas);
            out.flush();
            out.writeObject(metricas);
            out.flush();
            out.close();
            System.out.println("Closing connection");
            requestSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Simulação cliente servidor
     * Executa as ações do cliente, porem ainda não é a versão definitiva
     */
    private void simularRedeCliente(String servers[], int ports[], int numSim[]) {
        //Obtem modelo
        progrSim.print("Opening iconic model.");
        progrSim.print(" -> ");
        Document modelo = null;
        try {
            modelo = IconicoXML.ler(inputFile);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        progrSim.println("OK", Color.green);
        //Verifica se foi construido modelo corretamente
        progrSim.validarInicioSimulacao(modelo);
        //Enviar modelo...
        for (int i = 0; i < servers.length; i++) {
            try {
                System.out.println("Creating a server socket to " + servers[i] + " " + ports[i]);
                Socket requestSocket = new Socket(servers[i], ports[i]);
                System.out.println("Connection received from " + requestSocket.getInetAddress().getHostName());
                ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                System.out.println("Mensagem... número de execuções: " + numSim[i]);
                out.flush();
                out.writeObject(numSim[i]);
                out.flush();
                out.writeObject(modelo);
                out.flush();
                out.close();
                System.out.println("Closing connection");
                requestSocket.close();
            } catch (Exception ex) {
                Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo));
        //Recebendo resultados
        try {
            System.out.println("Creating a server socket");
            ServerSocket providerSocket = new ServerSocket(2004, 10);
            for (int i = 0; i < servers.length; i++) {
                System.out.println("Waiting for connection");
                Socket connection = providerSocket.accept();
                System.out.println("Connection received from " + connection.getInetAddress().getHostName());
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                System.out.println("Recebendo mensagem");
                metricas.addMetrica((Metricas) in.readObject());
                in.close();
            }
            System.out.println("Closing connection");
            providerSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Apresentando Resultados
        System.out.println("Realizados " + metricas.getNumeroDeSimulacoes() + " simulações");
    }

    /**
     * Realiza a leitura de um arquivo de configuração para a simulação cliente/servidor
     * @param configuracao aquivo com linhas contendo: [servidor/ip] [porta] [numero de simulações]
     * @return vetor contendo todos os objetos lidos do arquivo
     */
    private Object[] lerConfiguracao(File configuracao) {
        ArrayList config = new ArrayList();
        FileReader arq = null;
        try {
            arq = new FileReader(configuracao);
            BufferedReader lerArq = new BufferedReader(arq);
            String linha = lerArq.readLine();
            while (linha != null) {
                config.addAll(Arrays.asList(linha.split(" ")));
                System.out.printf("%s\n", linha);
                linha = lerArq.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                arq.close();
            } catch (IOException ex) {
                Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
            return config.toArray();
        }
    }

    /**
     * Classe interna para executar uma simulação em thread
     */
    private class RunnableImpl implements Runnable {

        private final Document modelo;
        private final int numExecucaoThread;
        private final int inicio;
        private Metricas metricas;
        private MetricasGlobais metricasGlobais;

        public RunnableImpl(Document modelo, int inicio, int numExecucao) {
            this.modelo = modelo;
            this.numExecucaoThread = numExecucao;
            this.inicio = inicio;
            this.metricas = new Metricas(null);
            this.metricasGlobais = new MetricasGlobais();
        }

        public MetricasGlobais getMetricasGlobais() {
            return metricasGlobais;
        }

        public Metricas getMetricas() {
            System.out.println("Simulados: " + metricas.getNumeroDeSimulacoes());
            return metricas;
        }

        @Override
        public void run() {
            for (int i = 0; i < numExecucaoThread; i++) {
                double t1 = System.currentTimeMillis();
                //criar grade
                RedeDeFilas redeDeFilas;
                redeDeFilas = IconicoXML.newRedeDeFilas(modelo);
                List<Tarefa> tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilas);
                //Verifica recursos do modelo e define roteamento
                Simulation sim = new SimulacaoSequencial(progrSim, redeDeFilas, tarefas);//[10%] --> 55 %
                sim.createRouting();
                //Realiza asimulação
                sim.simulate();//[30%] --> 85%
                if (outputFolder == null) {
                    MetricasGlobais global = new MetricasGlobais(redeDeFilas, sim.getTime(null), tarefas);
                    metricasGlobais.setTempoSimulacao(metricasGlobais.getTempoSimulacao() + global.getTempoSimulacao());
                    metricasGlobais.setSatisfacaoMedia(metricasGlobais.getSatisfacaoMedia() + global.getSatisfacaoMedia());
                    metricasGlobais.setOciosidadeComputacao(metricasGlobais.getOciosidadeComputacao() + global.getOciosidadeComputacao());
                    metricasGlobais.setOciosidadeComunicacao(metricasGlobais.getOciosidadeComunicacao() + global.getOciosidadeComunicacao());
                    metricasGlobais.setEficiencia(metricasGlobais.getEficiencia() + global.getEficiencia());
                } else {
                    Metricas temp = sim.getMetrics();
                    metricas.addMetrica(temp);
                    MetricasGlobais global = temp.getMetricasGlobais();
                    metricasGlobais.setTempoSimulacao(metricasGlobais.getTempoSimulacao() + global.getTempoSimulacao());
                    metricasGlobais.setSatisfacaoMedia(metricasGlobais.getSatisfacaoMedia() + global.getSatisfacaoMedia());
                    metricasGlobais.setOciosidadeComputacao(metricasGlobais.getOciosidadeComputacao() + global.getOciosidadeComputacao());
                    metricasGlobais.setOciosidadeComunicacao(metricasGlobais.getOciosidadeComunicacao() + global.getOciosidadeComunicacao());
                    metricasGlobais.setEficiencia(metricasGlobais.getEficiencia() + global.getEficiencia());
                }
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                System.out.println("* Simulation " + (inicio + 1 + i) + " Time = " + tempototal + "seconds");
            }
        }
    }
}
