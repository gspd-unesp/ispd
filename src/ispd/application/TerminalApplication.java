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
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A class for setting up the terminal part of iSPD and run the simulations.
 */
public class TerminalApplication implements Application {
    public static final int DEFAULT_PORT = 2004;
    private final Optional<File> inputFile;
    private final Optional<File> outputFolder;
    private final Modes mode;
    private final int nThreads;
    private final boolean parallel;
    private final int port;
    private final Options options;
    private final Inet4Address serverAddress;
    MetricasGlobais globalResults;
    private int nExecutions;
    private final ProgressoSimulacao simulationProgress;

    /**
     * Pre run of the terminal application, adding the necessary flags to the
     * class before it runs.
     *
     * @param args Arguments from the command line.
     */
    public TerminalApplication(String[] args) {
        this.options = getAllOptions();
        final CommandLine cmd = commandLinePreparation(this.options, args);

        this.mode = setMode(cmd);
        this.port = setPort(cmd);
        this.nExecutions = setNExecutions(cmd);
        this.nThreads = setNThreads(cmd);
        this.inputFile = setInputFile(cmd);
        this.outputFolder = setOutputFolder(cmd);
        this.parallel = setParallelSimulation(cmd);
        this.serverAddress = setServerAddress(cmd);
        this.globalResults = new MetricasGlobais();

        this.simulationProgress = new ProgressoSimulacao() {
            @Override
            public void incProgresso(int n) {
            }
            @Override
            public void print(String text, Color cor) {
            }
        };

        if ((this.mode == Modes.CLIENT || this.mode == Modes.SIMULATE) &&
                this.inputFile.isEmpty()) {
            System.out.println("It needs a model to simulate.");
            System.exit(1);
        }
    }

    /**
     * Set the mode for running the terminal application.
     *
     * @param cmd CommandLine used in the application.
     * @return An int representing a mode based on the options.
     */
    private Inet4Address setServerAddress(final CommandLine cmd) {
        try {
            if (!cmd.hasOption("a")) {
                return (Inet4Address) Inet4Address.getByName("127.0.0.1");
            }

            return (Inet4Address) Inet4Address.getByName(cmd.getOptionValue("a"));
        } catch (UnknownHostException e) {
            System.out.println("Error at getting the server address from " +
                    "command line. (" + e.getMessage() + ")");
            System.exit(1);
            throw new AssertionError("should not be reached");
        }
    }

    /**
     * A simple method for creating the Common Cli's command line class.
     *
     * @param options The class Common Cli's Options class for the
     *                command line options.
     * @param args    The arguments got from the command line.
     * @return The command line class with the chosen options.
     */
    private CommandLine commandLinePreparation(final Options options, final String[] args) {
        try {
            return (new DefaultParser()).parse(options, args);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
            throw new AssertionError("Should not be reachable.");
        }
    }

    /**
     * Set options to use in the command line for the configuration of the
     * simulation.
     *
     * @return An object for options of the command line from Common Cli.
     */
    private Options getAllOptions() {
        final Options options = new Options();

        options.addOption("h", "help", false, "print this help message.");
        options.addOption("v", "version", false, "print the version of iSPD.");
        options.addOption("s", "server", false, "run iSPD as a server.");
        options.addOption("c", "client", false, "run iSPD as a client.");
        options.addOption("P", "port", true, "specify a port.");
        options.addOption("t", "threads", true, "specify the number of threads.");
        options.addOption("conf", "conf", true, "specify a configuration file.");
        options.addOption("in", "input", true, "specify the input file of the\n" + "model to simulate.");
        options.addOption("o", "output", true, "specify an output folder for the html export.");
        options.addOption("e", "executions", true, "specify the number of executions.");
        options.addOption("a", "address", true, "specify the server address.");
        options.addOption("p", "parallel", false, "runs the simulation parallel.");

        return options;
    }

    /**
     * Get a value from an option from the command line.
     *
     * @param cmd The CommandLine that is being used.
     * @param op  The string relative to the argument from the command line.
     * @return The value of the option (if it exists and is valid).
     */
    private int setValueFromOption(final CommandLine cmd, final String op) {
        try {
            return Integer.parseInt(cmd.getOptionValue(op));
        } catch (NumberFormatException e) {
            System.out.println("\"" + cmd.getOptionValue(op) + "\" is not a valid number\n");
            System.exit(1);
            throw new AssertionError("should not be reached");
        }
    }

    /**
     * Set the mode for running the terminal application.
     *
     * @param cmd CommandLine used in the application.
     * @return An int representing a mode based on the options.
     */
    private Modes setMode(final CommandLine cmd) {
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
    private int setPort(final CommandLine cmd) {
        return cmd.hasOption("P") ?
                setValueFromOption(cmd, "P") :
                DEFAULT_PORT;
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

        final int threads = setValueFromOption(cmd, "t");

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
    private int setNExecutions(final CommandLine cmd) {
        return cmd.hasOption("e") ? setValueFromOption(cmd, "e") : 1;
    }

    /**
     * Get the name of the output folder for the html export from the command line.
     *
     * @param cmd The command line class from Common Cli.
     * @return The folder coming from the command line argument or an empty optional.
     */
    private Optional<File> setOutputFolder(final CommandLine cmd) {
        return cmd.hasOption("o") ?
            Optional.of(new File(cmd.getOptionValue("o"))) :
            Optional.empty();
    }

    /**
     * Get the name of the model file from the command line.
     *
     * @param cmd The command line class from Common Cli.
     * @return A configuration file with information for the simulation
     */
    private Optional<File> setInputFile(CommandLine cmd) {
        if (cmd.hasOption("in")) {
            return Optional.of(new File(cmd.getOptionValue("in")));
        }

        final var restArgs = cmd.getArgList();
        if (!restArgs.isEmpty()) {
            return Optional.of(new File(restArgs.get(0)));
        }

        return Optional.empty();
    }

    /**
     * Method for running the simulation based on the configuration done before.
     */
    @Override
    public void run() {
        final HelpFormatter helpFormatter = new HelpFormatter();

        switch (this.mode) {
            case HELP ->
                    helpFormatter.printHelp("java -jar iSPD.jar", this.options);
            case VERSION -> System.out.println("""
                    iSPD version 3.1
                      Iconic Simulator of Parallel and Distributed System
                      Copyright 2010-2022, by GSPD from UNESP.
                      Project Info: https://dcce.ibilce.unesp.br/spd
                      Source Code: https://github.com/gspd/ispd""");
            case SIMULATE -> {
                if (this.inputFile.isPresent()) {
                    var file = this.inputFile.get();

                    if (file.getName().endsWith(".imsx") && file.exists()) {
                        this.runSimulation();
                    } else {
                        System.out.println("iSPD can not open the file: " + file.getName());
                    }
                }
            }
            case SERVER ->
                this.simularRedeServidor();
            case CLIENT ->
                this.simularRedeCliente(this.serverAddress);
        }
        System.exit(0);
    }

    /**
     *
     */
    private void runSimulation() {
        System.out.println("Simulation Initiated.");
        System.out.print("Opening iconic model. ->");

        final Document model = getModelFromFile();
        final Metricas metrics = new Metricas(IconicoXML.newListUsers(model));
        double totalDuration = 0.0;
        for (int i = 1; i <= this.nExecutions; i++) {
            System.out.println("* Simulation " + i);

            final double preSimInstant = System.currentTimeMillis();
            final Metricas simMetric = runSimulation(model);
            final double postSimInstant = System.currentTimeMillis();
            final double totalSimDuration = (postSimInstant - preSimInstant) / 1000.0;

            System.out.println("Simulation Execution Time = " + totalSimDuration + "seconds");

            totalDuration += totalSimDuration;
            metrics.addMetrica(simMetric);
        }

        printSimulationResults(metrics, totalDuration);
    }

    /**
     * Print the simulation results
     *
     * @param metrics The metrics from the simulations
     * @param totalDuration The total duration of the simulations
     */
    private void printSimulationResults(final Metricas metrics, final double totalDuration) {
        System.out.println("Results:");
        metrics.calculaMedia();

        System.out.println("  Total Simulation Execution Time = " + totalDuration + "seconds");
        System.out.println(metrics.getMetricasGlobais());

        if (this.outputFolder.isPresent()) {
            ResultsDialog result = new ResultsDialog(metrics);
            result.salvarHTML(this.outputFolder.get());

            System.out.println("Results were exported to " + this.outputFolder.get().getName());
        }
    }

    /**
     * Get a model from the xml file containing the configuration of
     * the simulation.
     *
     * @return The model coming from the configuration file
     */
    private Document getModelFromFile() {
        if (this.inputFile.isEmpty()) {
            System.out.println("The ");
            System.exit(-1);
        }
        try {
            final Document model = IconicoXML.ler(this.inputFile.get());
            System.out.println(model.toString() + this.inputFile.get().getName());
            System.out.println(ConsoleColors.GREEN + "OK" + ConsoleColors.RESET);

            this.simulationProgress.validarInicioSimulacao(model);

            return model;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
            throw new AssertionError("Code shouldn't be reachable.");
        }
    }

    /**
     * Run a simulation from a model
     *
     * @param model A model with configurations for a simulation
     * @return The metrics resulted from the simulation
     */
    private Metricas runSimulation(final Document model) {
        final var queueNetwork = createQueueNetwork(model);
        final var jobs = createJobsList(model, queueNetwork);
        final var sim = selectSimulation(queueNetwork, jobs);

        sim.createRouting();

        sim.simulate();

        return sim.getMetrics();
    }

    /**
     * Select a simulation type based on the <i>parallel</i> field from the class.
     *
     * @param queueNetwork The queueNetwork created from the model of a simulation
     * @param jobs The job list
     * @return The chosen simulation
     */
    private Simulation selectSimulation(final RedeDeFilas queueNetwork, final List<Tarefa> jobs) {
        return this.parallel ?
                new SimulacaoParalela(this.simulationProgress, queueNetwork, jobs, this.nThreads) :
                new SimulacaoSequencial(this.simulationProgress, queueNetwork, jobs);
    }

    /**
     * Create a job list from the model and the queue network from it.
     * 
     * @param model The model from the simulation
     * @param queueNetwork The queue network from the model
     * @return The respective job list from the model
     */
    private List<Tarefa> createJobsList(final Document model, final RedeDeFilas queueNetwork) {
        System.out.print("  Creating tasks: ");
        final var jobs = IconicoXML.newGerarCarga(model).toTarefaList(queueNetwork);
        System.out.print(ConsoleColors.GREEN + "OK\n  " + ConsoleColors.RESET);

        return jobs;
    }

    /**
     * Create a queue network from a simulation model.
     *
     * @param model The model from a simulation
     * @return A queue network from the model
     */
    private RedeDeFilas createQueueNetwork(final Document model) {
        System.out.print("  Mounting network queue: ");
        final var queueNetwork = IconicoXML.newRedeDeFilas(model);
        System.out.println(ConsoleColors.GREEN + "OK" + ConsoleColors.RESET);

        return queueNetwork;
    }

    /**
     * Executa n simulações do modelo passado por parametro de forma paralela
     * (usando o motor de execução padrão em várias threads, ou usando o motor experimental de simulação otimista)
     */
    @Deprecated
    @SuppressWarnings("Optional without isPresent check.")
    private void simularParalelo() {
        this.simulationProgress.println("Simulation Initiated.");
        this.simulationProgress.print("Opening iconic model.");
        this.simulationProgress.print(" -> ");
        this.simulationProgress.incProgresso(5);//[5%] --> 5%
        this.simulationProgress.println("OK", Color.green);
            //Escrever Modelo
            //this.modelo(redeDeFilas);
            //criar tarefas

        try {
            Document[] modelo = IconicoXML.clone(this.inputFile.get(), this.nThreads);
            Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo[0]));
            //Verifica se foi construido modelo corretamente
            this.simulationProgress.validarInicioSimulacao(modelo[0]);
            int inicio = 0, incremento = this.nExecutions / this.nThreads;
            RunnableImpl[] trabalhador = new RunnableImpl[this.nThreads];
            Thread[] thread = new Thread[this.nThreads];
            System.out.println("Será executado com " + this.nThreads + " threads");
            boolean visible = false;
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
            visible = true;
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
                this.globalResults = metricas.getMetricasGlobais();
            } else {
                this.globalResults = new MetricasGlobais();
                for (int i = 0; i < this.nThreads; i++) {
                    this.globalResults.add(trabalhador[i].getMetricasGlobais());
                }
            }
            this.simulationProgress.println("OK");
            this.simulationProgress.println("Results:");
            if (this.outputFolder != null) {
                t1 = System.currentTimeMillis();
                ResultsDialog result = new ResultsDialog(metricas);
                result.salvarHTML(this.outputFolder.get());
                t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                tempototal = (t2 - t1) / 1000;
                this.simulationProgress.println("  Time to create html = " + tempototal + "seconds");
            }
            this.simulationProgress.println(this.globalResults.toString());
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
    @Deprecated
    private void simularRedeServidor() {
        Document modelo = null;
        Metricas metricas = new Metricas(null);
        String origem = null;
        //Recebendo Modelo
        System.out.println("Creating a server socket");

        try {
            ServerSocket providerSocket = new ServerSocket(2004, 10);

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
            Socket requestSocket = new Socket(origem, 2005);
            System.out.println("Connection received from " + requestSocket.getInetAddress().getHostName());
            ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
            this.globalResults = metricas.getMetricasGlobais();
            System.out.println("Mensagem... resultados obtidos: " + this.globalResults.toString());
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
    @SuppressWarnings("Optional without isPresent() check.")
    @Deprecated
    private void simularRedeCliente(Inet4Address server_address) {
        //Obtem modelo
        String server_ip = server_address.getHostAddress();
        this.simulationProgress.print("Opening iconic model.");
        this.simulationProgress.print(" -> ");
        Document modelo = null;
        try {
            modelo = IconicoXML.ler(this.inputFile.get());
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.simulationProgress.println("OK", Color.green);
        //Verifica se foi construido modelo corretamente
        this.simulationProgress.validarInicioSimulacao(modelo);
        //Enviar modelo...
        try {
            System.out.println("Creating a server socket to " + server_ip + " " + this.port);
            Socket requestSocket = new Socket(server_ip, 2004);
            System.out.println("Connection received from " + requestSocket.getInetAddress().getHostName());
            ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
            System.out.println("Mensagem... número de execuções: " + this.nExecutions);
            out.flush();
            out.writeObject(this.nExecutions);
            out.flush();
            out.writeObject(modelo);
            out.flush();
            out.close();
            System.out.println("Closing connection");
            requestSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo));
        //Recebendo resultados
        try {
            System.out.println("Creating a server socket");
            ServerSocket providerSocket = new ServerSocket(2005, 10);
            System.out.println("Waiting for connection");
            Socket connection = providerSocket.accept();
            System.out.println("Connection received from " + connection.getInetAddress().getHostName());
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            System.out.println("Recebendo mensagem");
            metricas.addMetrica((Metricas) in.readObject());
            this.globalResults = metricas.getMetricasGlobais();
            System.out.println(this.globalResults.toString());
            in.close();
            System.out.println("Closing connection");
            providerSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(TerminalApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Apresentando Resultados
        System.out.println("Realizados " + metricas.getNumeroDeSimulacoes() + " simulações");
    }

    /**
     * An enum for run modes for the terminal application.
     */
    public enum Modes {
        HELP("h"), VERSION("v"), SIMULATE(""), CLIENT("c"), SERVER("s"),
        ;

        final private String str;

        Modes(String s) {
            this.str = s;
        }
    }

    /**
     * Classe interna para executar uma simulação em thread
     */
    @Deprecated
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
