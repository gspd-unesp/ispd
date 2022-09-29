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
    public static final int DEFAULT_PORT = 2004;
    private final File inputFile;
    private final File outputFolder ;
    private final File configFile;
    private final Modes mode;
    private final int nThreads;
    private final boolean paralelo;
    private final int port;
    private final  Options options;
    //Resultados
    MetricasGlobais resuladosGlobais;
    private int nExecutions;
    private final ProgressoSimulacao simulationProgress;
    private boolean visible = true;

    /**
     * Pre run of the terminal application, adding the necessary flags to the
     * class before it runs.
     *
     * @param args Arguments from the command line.
     */
    public TerminalApplication (String[] args) {
        /* Prepare Common Cli */
        this.options = getAllOptions();
        final CommandLine cmd = commandLinePreparation(this.options, args);

        /* Configure all options based on the command line arguments */
        this.mode = setMode(cmd);
        this.port = setPort(cmd);
        this.nExecutions = setNExecutions(cmd);
        this.nThreads = setNThreads(cmd);
        this.configFile = setConfFile(cmd);
        this.inputFile = setInputFile(cmd);
        this.outputFolder = setOutputFolder(cmd);
        this.paralelo = setParallelSimulation(cmd);
        this.simulationProgress = setSimulationProgress();


        if ((this.mode == Modes.CLIENT || this.mode == Modes.SIMULATE) && this.inputFile == null) {
            System.out.println("It needs a model to simulate.");
            System.exit(1);
        }
    }

    private ProgressoSimulacao setSimulationProgress() {
        switch (this.mode) {
            case SIMULATE, CLIENT -> {
                return new ProgressoSimulacao() {
                    @Override
                    public void incProgresso(int n) {
                    }

                    @Override
                    public void print(String text, Color cor) {
                        if (TerminalApplication.this.visible) {
                            System.out.print(text);
                        }
                    }
                };
            }
            case SERVER -> {
                return new ProgressoSimulacao() {
                    @Override
                    public void incProgresso(int n) {

                    }

                    @Override
                    public void print(String text, Color cor) {

                    }
                };

            }
            default -> {
                return null;
            }
        }
    }

    /**
     * A simple method for creating the Common Cli's command line class.
     *
     * @param options The class Common Cli's Options class for the
     *                 command line options.
     * @param args    The arguments got from the command line.
     * @return        The command line class with the chosen options.
     */
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
     * @return A class for options of the command line from Common Cli.
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
            System.out.println(e.getMessage());
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

    /**
     * Set a port from the command line options or set the default port.
     *
     * @param cmd The command line class from Common Cli.
     * @return A value from the command line or the default port.
     */
    private int setPort(CommandLine cmd) {
        if (!cmd.hasOption("P")) {
            return DEFAULT_PORT;
        }

        return setValueFromOption(cmd, "P");
    }

    /**
     * Get the option of parallel simulation from the command line and configure it.
     *
     * @param cmd The command line class from Command Cli.
     * @return True if there is "p" in the command line of false otherwise.
     */
    private boolean setParallelSimulation(CommandLine cmd) {
        return cmd.hasOption("p");
    }

    /**
     * Get the number of threads to use from the command line.
     *
     * @param cmd The command line class from Common Cli.
     * @return A number got from the command line or the default 1.
     */
    private int setNThreads(CommandLine cmd) {
        if (!cmd.hasOption("t")) {
            return 1;
        }

        int threads = setValueFromOption(cmd, "t");

        if (this.nExecutions < 1) {
            System.out.println("Number of executions is invalid (" + this.nExecutions + ")");
            System.exit(1);
        }

        return Math.min(threads, this.nExecutions);
    }

    /**
     * Get the number of executions of the chosen model from the command line.
     *
     * @param cmd The command line class from Common Cli.
     * @return A number got from the command line or the default 1.
     */
    private int setNExecutions(CommandLine cmd) {
        if (!cmd.hasOption("e")) {
            return 1;
        }

        return setValueFromOption(cmd, "e");
    }

    /**
     * Get the name of the output folder for the html export from the command line.
     *
     * @param cmd The command line class from Common Cli.
     * @return The name of a folder or null if it doesn't exist.
     */
    private File setOutputFolder(CommandLine cmd) {
        if (!cmd.hasOption("o")) {
            return null;
        }

        return new File(cmd.getOptionValue("o"));
    }

    /**
     * Get the name of the model file from the command line.
     * @param cmd The command line class from Common Cli.
     * @return The name of the model file or null if it doesn't exist.
     */
    private File setInputFile(CommandLine cmd) {
        if (cmd.hasOption("in")) {
            return new File(cmd.getOptionValue("in"));
        }

        final List<String> restArgs = cmd.getArgList();
        if (!restArgs.isEmpty()) {
            return new File(restArgs.get(0));
        }

        return null;
    }

    /**
     * Get the name of the configuration file from the command line.
     *
     * @param cmd The command line class from Common Cli.
     * @return The name of the configuration file or null if it doesn't exist.
     */
    private File setConfFile(CommandLine cmd) {
        if (!cmd.hasOption("conf")) {
            return null;
        }

        return new File(cmd.getOptionValue("conf"));
    }

    /**
     * Method for running the simulation based on the configuration done before.
     */
    @Override
    public void run () {
        HelpFormatter helpFormatter = new HelpFormatter();

        switch (this.mode) {
            case HELP -> helpFormatter.printHelp("java -jar iSPD.jar", this.options);
            case VERSION -> System.out.println("""
                    iSPD version 3.1
                      Iconic Simulator of Parallel and Distributed System
                      Copyright 2010-2022, by GSPD from UNESP.
                      Project Info: https://dcce.ibilce.unesp.br/spd
                      Source Code: https://github.com/gspd/ispd""");
            case SIMULATE -> {
                if (this.inputFile.getName().endsWith(".imsx") && this.inputFile.exists()) {
                    if (this.nThreads > 1 && !this.paralelo) {
                        this.simularParalelo();
                    } else {
                        this.simularSequencial();
                    }
                } else {
                    System.out.println("iSPD can not open the file: " + this.inputFile.getName());
                }
            }
            case SERVER -> {
                this.simularRedeServidor();
            }
            case CLIENT -> {
                Object[] conf = lerConfiguracao(this.configFile);
                String[] server = new String[conf.length / 3];//{"localhost","localhost"};
                int[] ports = new int[conf.length / 3];//{2005,2006};
                int[] numSim = new int[conf.length / 3];//{15,15};
                for (int i = 0, j = 0; i < server.length; i++) {
                    server[i] = (String) conf[j];
                    j++;
                    ports[i] = Integer.parseInt(conf[j].toString());
                    j++;
                    numSim[i] = Integer.parseInt(conf[j].toString());
                    j++;
                }
                this.simularRedeCliente(server, ports, numSim);
            }
        }
        System.exit(0);
    }

    /**
     * Executa n simulações do modelo passado por parametro de forma sequencial
     * (usando o motor de execução padrão)
     */
    private void simularSequencial() {
        this.simulationProgress.println("Simulation Initiated.");
        try {
            this.simulationProgress.print("Opening iconic model.");
            this.simulationProgress.print(" -> ");
            Document modelo = IconicoXML.ler(this.inputFile);
            this.simulationProgress.println("OK", Color.green);
            //Verifica se foi construido modelo corretamente
            this.simulationProgress.validarInicioSimulacao(modelo);
            //Escrever Modelo
            //this.modelo(redeDeFilas);
            //criar tarefas

            Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo));
            this.resuladosGlobais = new MetricasGlobais();
            double total = 0;

            for (int i = 1; i <= this.nExecutions; i++) {
                double t1 = System.currentTimeMillis();
                this.simulationProgress.println("* Simulation " + i);
                //criar grade
                this.simulationProgress.print("  Mounting network queue.");
                this.simulationProgress.print(" -> ");
                RedeDeFilas redeDeFilas = IconicoXML.newRedeDeFilas(modelo);
                this.simulationProgress.println("OK", Color.green);
                this.simulationProgress.print("  Creating tasks.");
                this.simulationProgress.print(" -> ");
                List<Tarefa> tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilas);
                this.simulationProgress.print("OK\n  ", Color.green);
                //Verifica recursos do modelo e define roteamento
                Simulation sim;
                if (!this.paralelo) {
                    sim = new SimulacaoSequencial(this.simulationProgress, redeDeFilas, tarefas);//[10%] --> 55 %
                } else {
                    System.out.println("Execução paralela da simulação");
                    sim = new SimulacaoParalela(this.simulationProgress, redeDeFilas, tarefas, this.nThreads);
                }
                sim.createRouting();
                //Realiza asimulação
                this.simulationProgress.println("  Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a simulação
                sim.simulate();//[30%] --> 85%
                if (this.outputFolder == null) {
                    this.resuladosGlobais.add(new MetricasGlobais(redeDeFilas, sim.getTime(null), tarefas));
                } else {
                    Metricas temp = sim.getMetrics();
                    metricas.addMetrica(temp);
                }
                //Recebe instante de tempo em milissegundos ao fim da execução da simulação
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                total += tempototal;
                this.simulationProgress.println("  Simulation Execution Time = " + tempototal + "seconds");
            }
            if (this.nExecutions > 1 && this.outputFolder != null) {
                metricas.calculaMedia();
                this.resuladosGlobais = metricas.getMetricasGlobais();
            }
            this.simulationProgress.println("Results:");
            if (this.nExecutions > 1) {
                this.simulationProgress.println("  Total Simulation Execution Time = " + total + "seconds");
            }
            if (this.outputFolder != null) {
                this.resuladosGlobais = metricas.getMetricasGlobais();
                double t1 = System.currentTimeMillis();
                ResultsDialog result = new ResultsDialog(metricas);
                result.salvarHTML(this.outputFolder);
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                this.simulationProgress.println("  Time to create html = " + tempototal + "seconds");
            }
            this.simulationProgress.println(this.resuladosGlobais.toString());
        } catch (Exception erro) {
            this.simulationProgress.println(erro.getMessage(), Color.red);
            this.simulationProgress.print("Simulation Aborted", Color.red);
            this.simulationProgress.println("!", Color.red);
        }
    }

    /**
     * Executa n simulações do modelo passado por parametro de forma paralela
     * (usando o motor de execução padrão em várias threads, ou usando o motor experimental de simulação otimista)
     */
    private void simularParalelo() {
        this.simulationProgress.println("Simulation Initiated.");
        try {
            this.simulationProgress.print("Opening iconic model.");
            this.simulationProgress.print(" -> ");
            this.simulationProgress.incProgresso(5);//[5%] --> 5%
            this.simulationProgress.println("OK", Color.green);
            //Escrever Modelo
            //this.modelo(redeDeFilas);
            //criar tarefas

            Document[] modelo = IconicoXML.clone(this.inputFile, this.nThreads);
            Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo[0]));
            //Verifica se foi construido modelo corretamente
            this.simulationProgress.validarInicioSimulacao(modelo[0]);
            int inicio = 0, incremento = this.nExecutions / this.nThreads;
            RunnableImpl[] trabalhador = new RunnableImpl[this.nThreads];
            Thread[] thread = new Thread[this.nThreads];
            System.out.println("Será executado com " + this.nThreads + " threads");
            this.visible = false;
            double t1 = System.currentTimeMillis();
            for (int i = 0; i < this.nThreads - 1; i++) {
                trabalhador[i] = new RunnableImpl(modelo[i], inicio, incremento);
                thread[i] = new Thread(trabalhador[i]);
                thread[i].start();
                inicio += incremento;
            }
            trabalhador[this.nThreads - 1] = new RunnableImpl(modelo[this.nThreads - 1], inicio, this.nExecutions - inicio);
            thread[this.nThreads - 1] = new Thread(trabalhador[this.nThreads - 1]);
            thread[this.nThreads - 1].start();
            for (int i = 0; i < this.nThreads; i++) {
                thread[i].join();
            }
            this.visible = true;
            double t2 = System.currentTimeMillis();

            //Calcula tempo de simulação em segundos
            double tempototal = (t2 - t1) / 1000;
            this.simulationProgress.println("  Total Simulation Execution Time = " + tempototal + "seconds");
            this.simulationProgress.print("Getting Results.");
            this.simulationProgress.print(" -> ");
            if (this.nExecutions > 1 && this.outputFolder != null) {
                for (int i = 0; i < this.nThreads; i++) {
                    metricas.addMetrica(trabalhador[i].getMetricas());
                }
                metricas.calculaMedia();
                this.resuladosGlobais = metricas.getMetricasGlobais();
            } else {
                this.resuladosGlobais = new MetricasGlobais();
                for (int i = 0; i < this.nThreads; i++) {
                    this.resuladosGlobais.add(trabalhador[i].getMetricasGlobais());
                }
            }
            this.simulationProgress.println("OK");
            this.simulationProgress.println("Results:");
            if (this.outputFolder != null) {
                t1 = System.currentTimeMillis();
                ResultsDialog result = new ResultsDialog(metricas);
                result.salvarHTML(this.outputFolder);
                t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                tempototal = (t2 - t1) / 1000;
                this.simulationProgress.println("  Time to create html = " + tempototal + "seconds");
            }
            this.simulationProgress.println(this.resuladosGlobais.toString());
        } catch (Exception erro) {
            this.simulationProgress.println(erro.getMessage(), Color.red);
            this.simulationProgress.print("Simulation Aborted", Color.red);
            this.simulationProgress.println("!", Color.red);
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

            this.nExecutions = (Integer) in.readObject();
            System.out.println("Será feitas " + this.nExecutions + " simulações");
            modelo = (Document) in.readObject();
            in.close();
            System.out.println("Closing connection");
            providerSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Executando simulação
        if (this.nThreads <= 1) {
            System.out.println("Será realizado " + this.nExecutions + " simulações.");
            double t1 = System.currentTimeMillis();
            for (int i = 1; i <= this.nExecutions; i++) {
                RedeDeFilas redeDeFilas = IconicoXML.newRedeDeFilas(modelo);
                List<Tarefa> tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilas);
                Simulation sim = new SimulacaoSequencial(this.simulationProgress, redeDeFilas, tarefas);//[10%] --> 55 %
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
            Socket requestSocket = new Socket(origem, this.port);
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
    private void simularRedeCliente(String[] servers, int[] ports, int[] numSim) {
        //Obtem modelo
        this.simulationProgress.print("Opening iconic model.");
        this.simulationProgress.print(" -> ");
        Document modelo = null;
        try {
            modelo = IconicoXML.ler(this.inputFile);
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.simulationProgress.println("OK", Color.green);
        //Verifica se foi construido modelo corretamente
        this.simulationProgress.validarInicioSimulacao(modelo);
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
            ServerSocket providerSocket = new ServerSocket(this.port, 10);
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
        ArrayList<String> config = new ArrayList<>();
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
        } catch (IOException ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (arq != null) {
                    arq.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return config.toArray();
    }

    public enum Modes {
        HELP("h"),
        VERSION("v"),
        SIMULATE(""),
        CLIENT("c"),
        SERVER("s"),
        ;

        final private String str;

        Modes(String s) {
            this.str = s;
        }
    }

    /**
     * Classe interna para executar uma simulação em thread
     */
    private class RunnableImpl implements Runnable {

        private final Document modelo;
        private final int numExecucaoThread;
        private final int inicio;
        private final Metricas metricas;
        private final MetricasGlobais metricasGlobais;

        public RunnableImpl(Document modelo, int inicio, int numExecucao) {
            this.modelo = modelo;
            this.numExecucaoThread = numExecucao;
            this.inicio = inicio;
            this.metricas = new Metricas(null);
            this.metricasGlobais = new MetricasGlobais();
        }

        public MetricasGlobais getMetricasGlobais() {
            return this.metricasGlobais;
        }

        public Metricas getMetricas() {
            System.out.println("Simulados: " + this.metricas.getNumeroDeSimulacoes());
            return this.metricas;
        }

        @Override
        public void run() {
            for (int i = 0; i < this.numExecucaoThread; i++) {
                double t1 = System.currentTimeMillis();
                //criar grade
                RedeDeFilas redeDeFilas;
                redeDeFilas = IconicoXML.newRedeDeFilas(this.modelo);
                List<Tarefa> tarefas = IconicoXML.newGerarCarga(this.modelo).toTarefaList(redeDeFilas);
                //Verifica recursos do modelo e define roteamento
                Simulation sim = new SimulacaoSequencial(TerminalApplication.this.simulationProgress, redeDeFilas, tarefas);//[10%] --> 55 %
                sim.createRouting();
                //Realiza asimulação
                sim.simulate();//[30%] --> 85%
                MetricasGlobais global;
                if (TerminalApplication.this.outputFolder == null) {
                    global = new MetricasGlobais(redeDeFilas, sim.getTime(null), tarefas);
                } else {
                    Metricas temp = sim.getMetrics();
                    this.metricas.addMetrica(temp);
                    global = temp.getMetricasGlobais();

                }

                this.metricasGlobais.setTempoSimulacao(this.metricasGlobais.getTempoSimulacao() + global.getTempoSimulacao());
                this.metricasGlobais.setSatisfacaoMedia(this.metricasGlobais.getSatisfacaoMedia() + global.getSatisfacaoMedia());
                this.metricasGlobais.setOciosidadeComputacao(this.metricasGlobais.getOciosidadeComputacao() + global.getOciosidadeComputacao());
                this.metricasGlobais.setOciosidadeComunicacao(this.metricasGlobais.getOciosidadeComunicacao() + global.getOciosidadeComunicacao());
                this.metricasGlobais.setEficiencia(this.metricasGlobais.getEficiencia() + global.getEficiencia());

                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                System.out.println("* Simulation " + (this.inicio + 1 + i) + " Time = " + tempototal + "seconds");
            }
        }
    }
}
