package ispd.application.terminal;

import java.awt.Color;
import java.io.File;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;

import ispd.application.Application;
import ispd.arquivo.SalvarResultadosHTML;
import ispd.arquivo.xml.IconicoXML;
import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.motor.ProgressoSimulacao;
import ispd.motor.SimulacaoParalela;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.Simulation;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.Metricas;

/**
 * A class for setting up the terminal part of iSPD and run the simulations.
 */
public class TerminalApplication implements Application {
    private static final int DEFAULT_PORT = 2004;
    private final Optional<File> inputFile;
    private final Optional<File> outputFolder;
    private final Modes mode;
    private final int nThreads;
    private final boolean parallel;
    private final int serverPort;
    private final Options options;
    private final Inet4Address serverAddress;
    private final int nExecutions;
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
        this.serverPort = setPort(cmd);
        this.nExecutions = setNExecutions(cmd);
        this.nThreads = setNThreads(cmd);
        this.inputFile = setInputFile(cmd);
        this.outputFolder = setOutputFolder(cmd);
        this.parallel = setParallelSimulation(cmd);
        this.serverAddress = setServerAddress(cmd);

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
        options.addOption("in", "input", true, "specify the input file of the" + "model to simulate.");
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
            System.out.println("\"" + cmd.getOptionValue(op) + "\" is not a valid number");
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
        return cmd.hasOption("P") ? setValueFromOption(cmd, "P") : DEFAULT_PORT;
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
     * @return The folder coming from the command line argument or an empty
     *         optional.
     */
    private Optional<File> setOutputFolder(final CommandLine cmd) {
        return cmd.hasOption("o") ? Optional.of(new File(cmd.getOptionValue("o"))) : Optional.empty();
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
                      Source Code: https://github.com/gspd/ispd
                                               """);
            case SIMULATE -> {
                if (this.inputFile.isPresent()) {
                    var file = this.inputFile.get();

                    if (file.getName().endsWith(".imsx") && file.exists()) {
                        this.runNSimulations();
                    } else {
                        System.out.println("iSPD can not open the file: " + file.getName());
                    }
                }
            }
            case SERVER ->
                this.serverSimulation();
            case CLIENT ->
                this.clientSimulate();
        }
        System.exit(0);
    }

    /**
     * Run a number of simulations and calculate the time for each one,
     * printing the results of them at the end.
     */
    private void runNSimulations() {
        System.out.println("Simulation Initiated.");
        System.out.print("Opening iconic model. ->");

        final Document model = getModelFromFile();
        final Metricas metrics = new Metricas(IconicoXML.newListUsers(model));
        double totalDuration = 0.0;
        for (int i = 1; i <= this.nExecutions; i++) {
            System.out.println("* Simulation " + i);

            final double preSimInstant = System.currentTimeMillis();
            final Metricas simMetric = runASimulation(model);
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
     * @param metrics       The metrics from the simulations
     * @param totalDuration The total duration of the simulations
     */
    private void printSimulationResults(final Metricas metrics, final double totalDuration) {
        System.out.println("Results:");
        metrics.calculaMedia();

        System.out.println("  Total Simulation Execution Time = " + totalDuration + "seconds");
        System.out.println(metrics.getMetricasGlobais());

        if (this.outputFolder.isPresent()) {
            final var html = new SalvarResultadosHTML();
            final var chartMaker = new SimulationResultChartMaker(metrics);

            html.setMetricasTarefas(metrics);
            html.setMetricasGlobais(metrics.getMetricasGlobais());
            html.setTabela(metrics.makeResourceTable());

            html.saveHtml(this.outputFolder.get(), chartMaker);

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
    private Metricas runASimulation(final Document model) {
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
     * @param jobs         The job list
     * @return The chosen simulation
     */
    private Simulation selectSimulation(final RedeDeFilas queueNetwork, final List<Tarefa> jobs) {
        return this.parallel ? new SimulacaoParalela(this.simulationProgress, queueNetwork, jobs, this.nThreads)
                : new SimulacaoSequencial(this.simulationProgress, queueNetwork, jobs);
    }

    /**
     * Create a job list from the model and the queue network from it.
     * 
     * @param model        The model from the simulation
     * @param queueNetwork The queue network from the model
     * @return The respective job list from the model
     */
    private List<Tarefa> createJobsList(final Document model, final RedeDeFilas queueNetwork) {
        System.out.print("  Creating tasks: ");
        final var jobs = IconicoXML.newGerarCarga(model).makeTaskList(queueNetwork);
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
     * Hosts a server for simulating models coming from clients
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private void serverSimulation() {
        while (true) {
            try {
                final var simServer = new Server(this.serverPort);
                final var newModel = simServer.getMetricsFromClient();
                final var modelMetrics = runASimulation(newModel);

                simServer.returnMetricsToClient(modelMetrics);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Sends a model for a server to simulate and receives back its metrics,
     * printing the results at the end.
     */
    private void clientSimulate() {
        final var simClient = new Client(this.serverAddress, this.serverPort);
        final var model = getModelFromFile();

        simClient.sendModelToServer(model);
        final var metrics = simClient.receiveMetricsFromServer();

        System.out.println(metrics.getMetricasGlobais());
    }

    /**
     * An enum for run modes for the terminal application.
     */
    private enum Modes {
        HELP("h"),
        VERSION("v"),
        SIMULATE(""),
        CLIENT("c"),
        SERVER("s"),
        ;

        private final String str;

        Modes(String s) {
            this.str = s;
        }
    }
}
