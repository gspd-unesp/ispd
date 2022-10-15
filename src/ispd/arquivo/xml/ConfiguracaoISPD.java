package ispd.arquivo.xml;

import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.escalonador.Carregar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Responsible for reading and updating the software's configuration file
 */
public class ConfiguracaoISPD {
    public static final byte DEFAULT = 0;
    public static final byte OPTIMISTIC = 1;
    public static final byte GRAPHICAL = 2;
    private static final String FILENAME = "configuration.xml";
    private final File configurationFile = new File(
            Carregar.DIRETORIO_ISPD,
            ConfiguracaoISPD.FILENAME
    );
    private SimulationType simulationType = SimulationType.Default;
    private Integer threadCount = 1;
    private Integer simulationCount = 1;
    private Boolean shouldChartProc = true;
    private Boolean shouldChartComms = true;
    private Boolean shouldChartUserTime = true;
    private Boolean shouldChartMachineTime = false;
    private Boolean shouldChartTaskTime = false;
    private File lastModelOpen = Carregar.DIRETORIO_ISPD;

    /**
     * If the configuration file exists, reads configuration from it.
     * Otherwise, default values are used.
     */
    public ConfiguracaoISPD() {
        try {
            final var doc = new WrappedDocument(ManipuladorXML.read(
                    this.configurationFile,
                    "configurationFile.dtd"
            ));

            this.readConfigFromDoc(doc);
        } catch (final IOException |
                       ParserConfigurationException |
                       SAXException ignored) {
            Logger.getLogger(ConfiguracaoISPD.class.getName())
                    .warning("Error while reading configuration file '%s'"
                            .formatted(this.configurationFile));
        }
    }

    private void readConfigFromDoc(final WrappedDocument doc) {
        final var e = doc.ispd();

        this.readGeneralConfig(e);
        this.readChartCreationConfig(e);
        this.readLastOpenModelConfig(e);
    }

    /**
     * Read 'generalist' configurations from the element, such as simulation
     * type, number of threads to be used in the simulation(s), and number of
     * simulations to execute.
     */
    private void readGeneralConfig(final WrappedElement e) {
        final var mode = e.simulationMode();

        this.simulationType = Arrays.stream(SimulationType.values())
                .filter(t -> t.hasName(mode))
                .findFirst()
                .orElseThrow(() -> new IllegalSimulationModeException(mode));

        this.threadCount = e.numberOfThreads();
        this.simulationCount = e.numberOfSimulations();
    }

    /**
     * Read information pertained to which charts will be created to display
     * the results of the simulation(s).
     */
    private void readChartCreationConfig(final WrappedElement e) {
        final var c = e.chartCreate();

        this.shouldChartProc = c.shouldChartProcessing();
        this.shouldChartComms = c.shouldChartCommunication();
        this.shouldChartUserTime = c.shouldChartUserTime();
        this.shouldChartMachineTime = c.shouldChartMachineTime();
        this.shouldChartTaskTime = c.shouldChartTaskTime();
    }

    private void readLastOpenModelConfig(final WrappedElement e) {
        final var lastFile = e.modelOpen().lastFile();
        if (!lastFile.isEmpty()) {
            this.lastModelOpen = new File(lastFile);
        }
    }

    /**
     * Returns which simulation mode is being used<br>
     * {@literal 0}: <b>Default</b> simulation mode<br>
     * {@literal 1}: <b>Optimisitc</b><br>
     * {@literal 2}: <b>Graphical</b>
     */
    public int getSimulationMode() {
        return this.simulationType.asInt;
    }

    /**
     * Update the simulation type that will be executed.
     */
    public void setSimulationMode(final byte simulationMode) {
        this.simulationType = Arrays.stream(SimulationType.values())
                .filter(t -> t.asInt == simulationMode)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Saves current state of the configurations to the configuration file
     */
    public void saveCurrentConfig() {
        final var doc = Objects.requireNonNull(ManipuladorXML.newDocument());

        final var ispd = this.saveGeneralConfig(doc);
        ispd.appendChild(this.saveChartConfig(doc));
        ispd.appendChild(this.saveLastOpenModelConfig(doc));

        doc.appendChild(ispd);

        ManipuladorXML.write(doc,
                this.configurationFile,
                "configurationFile.dtd",
                false
        );
    }

    private Element saveGeneralConfig(final Document doc) {
        final var ispd = doc.createElement("ispd");

        ispd.setAttribute(
                "simulation_mode", this.simulationType.xmlName);
        ispd.setAttribute(
                "number_simulations", this.simulationCount.toString());
        ispd.setAttribute(
                "number_threads", this.threadCount.toString());

        return ispd;
    }

    private Element saveChartConfig(final Document doc) {
        final var c = doc.createElement("chart_create");
        c.setAttribute("processing", this.shouldChartProc.toString());
        c.setAttribute("communication", this.shouldChartComms.toString());
        c.setAttribute("user_time", this.shouldChartUserTime.toString());
        c.setAttribute("machine_time", this.shouldChartMachineTime.toString());
        c.setAttribute("task_time", this.shouldChartTaskTime.toString());
        return c;
    }

    private Element saveLastOpenModelConfig(final Document doc) {
        final var files = doc.createElement("model_open");

        if (this.lastModelOpen != null) {
            files.setAttribute(
                    "last_file", this.lastModelOpen.getAbsolutePath());
        }

        return files;
    }

    /**
     * @return The number of threads that will be used in executing the
     * simulation(s)
     */
    public Integer getNumberOfThreads() {
        return this.threadCount;
    }

    /**
     * Set the number of threads to be used in the simulation execution.
     */
    public void setNumberOfThreads(final Integer numberOfThreads) {
        this.threadCount = numberOfThreads;
    }

    /**
     * @return number of simulation that will be executed
     */
    public Integer getNumberOfSimulations() {
        return this.simulationCount;
    }

    /**
     * Set the number of simulation to be executed.
     */
    public void setNumberOfSimulations(final Integer numberOfSimulations) {
        this.simulationCount = numberOfSimulations;
    }

    /**
     * @return {@code true} if the "processing" chart will be created
     */
    public Boolean getCreateProcessingChart() {
        return this.shouldChartProc;
    }

    /**
     * Set if the "processing" chart should be created.
     */
    public void setCreateProcessingChart(final Boolean b) {
        this.shouldChartProc = b;
    }

    /**
     * @return {@code true} if the "communication" chart will be created
     */
    public Boolean getCreateCommunicationChart() {
        return this.shouldChartComms;
    }

    /**
     * Set if the "communication" chart should be created.
     */
    public void setCreateCommunicationChart(final Boolean b) {
        this.shouldChartComms = b;
    }

    /**
     * @return {@code true} if the "user through time" chart will be created
     */
    public Boolean getCreateUserThroughTimeChart() {
        return this.shouldChartUserTime;
    }

    /**
     * Set if the "user through time" chart should be created.
     */
    public void setCreateUserThroughTimeChart(final Boolean b) {
        this.shouldChartUserTime = b;
    }

    /**
     * @return {@code true} if the "machine through time" chart will be created
     */
    public Boolean getCreateMachineThroughTimeChart() {
        return this.shouldChartMachineTime;
    }

    /**
     * Set if the "machine through time" chart should be created.
     */
    public void setCreateMachineThroughTimeChart(final Boolean b) {
        this.shouldChartMachineTime = b;
    }

    /**
     * @return {@code true} if the "task through time" chart will be created
     */
    public Boolean getCreateTaskThroughTimeChart() {
        return this.shouldChartTaskTime;
    }

    /**
     * Set if the "task through time" chart should be created.
     */
    public void setCreateTaskThroughTimeChart(final Boolean b) {
        this.shouldChartTaskTime = b;
    }

    /**
     * @return last model opened
     */
    public File getLastFile() {
        return this.lastModelOpen;
    }

    /**
     * Set which was the last model opened.
     */
    public void setLastFile(final File lastDir) {
        if (lastDir == null) {
            return;
        }

        this.lastModelOpen = lastDir;
    }

    private enum SimulationType {
        Default((byte) 0, "default"),
        Optimistic((byte) 1, "optimistic"),
        Graphical((byte) 2, "graphical");

        private final String xmlName;
        private final byte asInt;

        SimulationType(final byte i, final String xmlName) {
            this.asInt = i;
            this.xmlName = xmlName;
        }

        private boolean hasName(final String modeName) {
            return this.xmlName.equals(modeName);
        }
    }

    private static class IllegalSimulationModeException extends IllegalArgumentException {
        private IllegalSimulationModeException(final String mode) {
            super("Invalid simulation mode '%s' found in configuration file.".formatted(mode));
        }
    }
}
