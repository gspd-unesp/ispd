package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.random.Distribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

class TraceLoadHelper {

    private static final int USER_FIELD_INDEX = 11;
    private static final int HEADER_LINE_COUNT = 5;
    private static final double RECEIVING_FILE_SIZE = 0.0009765625;
    private final List<Tarefa> tasks = new ArrayList<>(0);
    private final List<String> users = new ArrayList<>(0);
    private final List<Double> computationalPowers = new ArrayList<>(0);
    private final List<Double> profiles = new ArrayList<>(0);
    private final int taskCount;
    private final String traceType;
    private final RedeDeFilas queueNetwork;
    private final Distribution random =
            new Distribution(System.currentTimeMillis());

    TraceLoadHelper(
            final RedeDeFilas rdf,
            final String traceType,
            final int taskCount) {
        this.queueNetwork = rdf;
        this.traceType = traceType;
        this.taskCount = taskCount;
    }

    List<Tarefa> toTaskList(final String path) {

        try (final var br = new BufferedReader(new FileReader(path))) {

            TraceLoadHelper.skipHeader(br);
            final String[] attrs = this.parseAttrs(br);

            if ("SWF".equals(this.traceType) || "GWF".equals(this.traceType)) {
                this.addTasksToMachines(
                        (master) -> this.addTaskSwfGwf(master, attrs));
            } else if ("iSPD".equals(this.traceType)) {
                this.addTasksToMachines(
                        (master) -> this.addTaskIspd(master, attrs));
            }

            for (final var master : this.queueNetwork.getMestres()) {
                ((CS_Mestre) master).getEscalonador().getMetricaUsuarios().addAllUsuarios(
                        this.users,
                        this.computationalPowers,
                        this.profiles
                );
            }

            this.queueNetwork.getUsuarios().addAll(this.users);
            return this.tasks;

        } catch (final IOException ex) {
            Logger.getLogger(TraceLoadHelper.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static void skipHeader(final BufferedReader bf) throws IOException {
        int i = 0;
        while (bf.ready() && i < TraceLoadHelper.HEADER_LINE_COUNT) {
            bf.readLine();
            i++;
        }
    }

    private String[] parseAttrs(final BufferedReader bf) throws IOException {
        final String[] attrs = bf.readLine().split("\"");
        final String user = attrs[TraceLoadHelper.USER_FIELD_INDEX];
        if (!this.queueNetwork.getUsuarios().contains(user) && !this.users.contains(user)) {
            this.addDefaultUser(attrs);
        }
        return attrs;
    }

    private void addTasksToMachines(final AddTaskFunction add) throws IOException {
        for (final var master : this.queueNetwork.getMestres()) {
            TraceLoadHelper.addTasksToMaster(
                    master, this.tasksPerMaster(), add);
        }

        TraceLoadHelper.addTasksToMaster(
                this.firstMaster(), this.taskRemainder(), add);
    }

    private void addTaskSwfGwf(
            final CS_Processamento master,
            final String[] attrs) {
        final var task = this.makeTaskAndAddToList(
                master,
                fs -> this.random.twoStageUniform(200, 5000, 25000, 0.5),
                fs -> Double.parseDouble(fs[7]) * this.averageComputationalPower(),
                attrs
        );

        if (attrs[5].contains("0") || attrs[5].contains("5")) {
            task.setLocalProcessamento(master);
            task.cancelar(0);
        }
    }

    private void addTaskIspd(
            final CS_Processamento master,
            final String[] attrs) {
        this.makeTaskAndAddToList(
                master,
                fs -> Double.parseDouble(fs[9]),
                fs -> Double.parseDouble(fs[7]),
                attrs
        );
    }

    private void addDefaultUser(final String[] attrs) {
        this.users.add(attrs[TraceLoadHelper.USER_FIELD_INDEX]);
        this.profiles.add(100.0);
        this.computationalPowers.add(0.0);
    }

    private static void addTasksToMaster(
            final CS_Processamento master,
            final int count,
            final AddTaskFunction addFun) throws IOException {
        for (int i = 0; i < count; i++) {
            addFun.accept(master);
        }
    }

    private int tasksPerMaster() {
        return this.taskCount / this.queueNetwork.getMestres().size();
    }

    private CS_Processamento firstMaster() {
        return this.queueNetwork.getMestres().get(0);
    }

    private int taskRemainder() {
        return this.taskCount % this.queueNetwork.getMestres().size();
    }

    private Tarefa makeTaskAndAddToList(
            final CS_Processamento master,
            final Function<String[], Double> sentFileSize,
            final Function<String[], Double> processingTime,
            final String[] attrs) {
        final var task = new Tarefa(
                Integer.parseInt(attrs[1]),
                attrs[TraceLoadHelper.USER_FIELD_INDEX],
                "application1",
                master,
                sentFileSize.apply(attrs),
                TraceLoadHelper.RECEIVING_FILE_SIZE,
                processingTime.apply(attrs),
                Double.parseDouble(attrs[3])
        );

        this.tasks.add(task);

        return task;
    }

    private double averageComputationalPower() {
        return this.queueNetwork.getMaquinas().stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .average()
                .orElse(0.0);
    }

    @FunctionalInterface
    private interface AddTaskFunction {
        void accept(CS_Processamento master) throws IOException;
    }
}