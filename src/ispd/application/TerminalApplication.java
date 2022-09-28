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
public class TerminalApplication implements Application {
    public enum Modes {
        HELP(0, "h"),
        VERSION(1, "v"),
        SIMULATE(2, ""),
        CLIENT(3, "c"),
        SERVER(4, "s"),
        ;

        final private int num;
        final private String str;

        Modes(int i, String s) {
            this.num = i;
            this.str = s;
        }
    }

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
    final private File configFile;
    final private Modes mode;
    private int nExecutions;
    final private int nThreads;
    private ProgressoSimulacao progrSim;
    private boolean paralelo = false;
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

    /**
     * Set options to use in the command line for the configuration of the
     * simulation.
     *
     * @return options A class for options from Common Cli
     */
    private Options getAllOptions() {
        Options options = new Options();

        options.addOption("h", "help",
                false, "print this help message.");
        options.addOption("v", "version",
                false, "print the version of iSPD.");
        options.addOption("s", "server",
                false, "run iSPD as a server.");
        options.addOption("c", "client",
                false, "run iSPD as a client.");
        options.addOption("P", "port",
                true, "specify a port.");
        options.addOption("t", "threads",
                true, "specify the number of threads.");
        options.addOption("conf", "conf",
                true, "specify a configuration file.");
        options.addOption("in", "input",
                true, "specify the input file of the\n" + "model to simulate.");
        options.addOption("o", "output",
                true, "specify an output folder for the html export.");
        options.addOption("e", "executions",
                true, "specify the number of executions.");

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
    private Modes setMode(CommandLine cmd) {
        for (final var v : Modes.values()) {
            if (cmd.hasOption(v.str)) {
                return v;
            }
        }

        return Modes.SIMULATE;
    }

    private int setPort(CommandLine cmd) {
        if (!cmd.hasOption("P")) {
            return 2004;
        }

        return setValueFromOption(cmd, "P");
    }


    /**
     * Pre run of the terminal application, adding the necessary flags to the
     * class before it runs.
     *
     * @param args Arguments from the command line.
     */
    public TerminalApplication (String[] args) {
        /* Prepare Common Cli */
        this.options = getAllOptions();
        this.cmd = commandLinePreparation(options, args);

        /* Configure all options based on the command line arguments */
        this.mode = setMode(cmd);
        this.port = setPort(cmd);
        this.nExecutions = setNExecutions(cmd);
        this.nThreads = setNThreads(cmd);
        this.configFile = setConfFile(cmd);
        this.inputFile = setInputFile(cmd);
        this.outputFolder = setOutputFolder(cmd);
        this.paralelo = setParallelSimulation(cmd);

        if ((this.mode == Modes.CLIENT || this.mode == Modes.SIMULATE) && inputFile == null) {
            System.out.println("It needs a model to simulate.");
            System.exit(1);
        }
    }

    private boolean setParallelSimulation(CommandLine cmd) {
        return cmd.hasOption("p");
    }

    private int setNThreads(CommandLine cmd) {
        if (!cmd.hasOption("t")) {
            return 1;
        }

        int threads = setValueFromOption(cmd, "t");

        if (this.nExecutions < 1) {
            System.out.println("Number of executions is invalid (" + nExecutions + ")");
            System.exit(1);
        }

        return Math.min(threads, this.nExecutions);
    }

    private int setNExecutions(CommandLine cmd) {
        if (!cmd.hasOption("e")) {
            return 1;
        }

        return setValueFromOption(cmd, "e");
    }

    private File setOutputFolder(CommandLine cmd) {
        if (cmd.hasOption("o")) {
            return new File(cmd.getOptionValue("o"));
        } else {
            return null;
        }
    }

    private File setInputFile(CommandLine cmd) {
        if (cmd.hasOption("in")) {
            return new File(cmd.getOptionValue("in"));
        } else {
            final List<String> restArgs = cmd.getArgList();
            if (!restArgs.isEmpty()) {
                return new File(restArgs.get(0));
            }
        }

        return null;
    }

    private File setConfFile(CommandLine cmd) {
        if (cmd.hasOption("conf")) {
            return new File(cmd.getOptionValue("conf"));
        } else {
            return null;
        }
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
//                System.out.println("\t-n <number>\tnumber of simulation");
                break;
            case VERSION:
                System.out.println("""
                        iSPD version 3.1
                          Iconic Simulator of Parallel and Distributed System
                          Copyright 2010-2022, by GSPD from UNESP.
                          Project Info: https://dcce.ibilce.unesp.br/spd
                          Source Code: https://github.com/gspd/ispd""");
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
                    if (nThreads > 1 && !paralelo) {
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
                Object conf[] = lerConfiguracao(configFile);
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

            for (int i = 1; i <= nExecutions; i++) {
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
                    sim = new SimulacaoParalela(progrSim, redeDeFilas, tarefas, nThreads);
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
            if (nExecutions > 1 && outputFolder != null) {
                metricas.calculaMedia();
                resuladosGlobais = metricas.getMetricasGlobais();
            }
            progrSim.println("Results:");
            if (nExecutions > 1) {
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

            Document[] modelo = IconicoXML.clone(inputFile, nThreads);
            Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo[0]));
            //Verifica se foi construido modelo corretamente
            progrSim.validarInicioSimulacao(modelo[0]);
            int inicio = 0, incremento = nExecutions / nThreads;
            RunnableImpl[] trabalhador = new RunnableImpl[nThreads];
            Thread[] thread = new Thread[nThreads];
            System.out.println("Será executado com " + nThreads + " threads");
            visible = false;
            double t1 = System.currentTimeMillis();
            for (int i = 0; i < nThreads - 1; i++) {
                trabalhador[i] = new RunnableImpl(modelo[i], inicio, incremento);
                thread[i] = new Thread(trabalhador[i]);
                thread[i].start();
                inicio += incremento;
            }
            trabalhador[nThreads - 1] = new RunnableImpl(modelo[nThreads - 1], inicio, nExecutions - inicio);
            thread[nThreads - 1] = new Thread(trabalhador[nThreads - 1]);
            thread[nThreads - 1].start();
            for (int i = 0; i < nThreads; i++) {
                thread[i].join();
            }
            visible = true;
            double t2 = System.currentTimeMillis();
            //Calcula tempo de simulação em segundos
            double tempototal = (t2 - t1) / 1000;
            progrSim.println("  Total Simulation Execution Time = " + tempototal + "seconds");
            progrSim.print("Getting Results.");
            progrSim.print(" -> ");
            if (nExecutions > 1 && outputFolder != null) {
                for (int i = 0; i < nThreads; i++) {
                    metricas.addMetrica(trabalhador[i].getMetricas());
                }
                metricas.calculaMedia();
                resuladosGlobais = metricas.getMetricasGlobais();
            } else {
                resuladosGlobais = new MetricasGlobais();
                for (int i = 0; i < nThreads; i++) {
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
            nExecutions = (Integer) in.readObject();
            System.out.println("Será feitas " + nExecutions + " simulações");
            modelo = (Document) in.readObject();
            in.close();
            System.out.println("Closing connection");
            providerSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Executando simulação
        if (nThreads <= 1) {
            System.out.println("Será realizado " + nExecutions + " simulações.");
            double t1 = System.currentTimeMillis();
            for (int i = 1; i <= nExecutions; i++) {
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
