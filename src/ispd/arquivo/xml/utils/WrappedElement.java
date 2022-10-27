package ispd.arquivo.xml.utils;

import ispd.motor.carga.task.TaskSize;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class to add convenience methods to manipulate XML Element objects.
 * It functions as a wrapper, outsourcing method calls to the inner object.
 */
public class WrappedElement {
    private final Element element;

    /**
     * Construct a {@link TaskSize} from this instance, with the values
     * acquired from {@link #minimum()}, {@link #maximum()} and
     * {@link #average()}.
     *
     * @return {@link TaskSize} with a minimum, maximum, and average values
     * as the respective tags in this instance, and {@code 0} for  probability.
     */
    public TaskSize toTaskSizeNoProbability() {
        return new TaskSize(this.minimum(), this.maximum(), this.average(), 0);
    }

    /**
     * Construct a {@link TaskSize} from this instance, with the values
     * acquired from {@link #minimum()} and {@link #maximum()}.
     *
     * @return {@link TaskSize} with a minimum and maximum values as the
     * respective tags in this instance, and default values for the fields
     * {@link TaskSize#average()} and {@link TaskSize#probability()}.
     * @see TaskSize#TaskSize(double, double)
     */
    public TaskSize toTaskSizeRange() {
        return new TaskSize(this.minimum(), this.maximum());
    }

    /**
     * Construct a {@link TaskSize} from this instance, with the values
     * acquired from {@link #minimum()}, {@link #maximum()},
     * {@link #average()} and {@link #probability()}.
     *
     * @return {@link TaskSize} with a minimum, maximum, average and
     * probability values as the respective tags in this instance.
     */
    public TaskSize toTaskSize() {
        return new TaskSize(
                this.minimum(), this.maximum(),
                this.average(), this.probability()
        );
    }

    /**
     * @return id of origination vertex
     */
    public int origination() {
        return this.vertex("origination");
    }

    private int vertex(final String vertexEnd) {
        return this.connection().getInt(vertexEnd);
    }

    private int getInt(final String attributeName) {
        return Integer.parseInt(this.getAttribute(attributeName));
    }

    /**
     * @return inner element with tag "connect"
     */
    private WrappedElement connection() {
        return this.firstTagElement("connect");
    }

    private String getAttribute(final String s) {
        return this.element.getAttribute(s);
    }

    /**
     * @return first element with given tag name
     */
    private WrappedElement firstTagElement(final String tagName) {
        return new WrappedElement((Element) this.getElementsByTagName(tagName).item(0));
    }

    private NodeList getElementsByTagName(final String s) {
        return this.element.getElementsByTagName(s);
    }

    /**
     * @return id of destination vertex
     */
    public int destination() {
        return this.vertex("destination");
    }

    /**
     * @return inner element with tag "position"
     */
    public WrappedElement position() {
        return this.firstTagElement("position");
    }

    /**
     * @return "vmm" attribute
     */
    public String vmm() {
        return this.getAttribute("vmm");
    }

    /**
     * @return inner element with tag "master"
     */
    public WrappedElement master() {
        return this.firstTagElement("master");
    }

    /**
     * @return {@link Stream} of all inner elements with tag name "slave"
     */
    public Stream<WrappedElement> slaves() {
        return this.elementsWithTag("slave");
    }

    private Stream<WrappedElement> elementsWithTag(final String tag) {
        return WrappedElement.nodeListToWrappedElementStream(
                this.getElementsByTagName(tag)
        );
    }

    /**
     * @return convert given {@link NodeList} into a {@link Stream} of {@code
     * WrappedElement}s
     */
    /* package-private */
    static Stream<WrappedElement> nodeListToWrappedElementStream(final NodeList nl) {
        return IntStream.range(0, nl.getLength())
                .mapToObj(nl::item)
                .map(Element.class::cast)
                .map(WrappedElement::new);
    }

    public WrappedElement(final Element element) {
        this.element = element;
    }

    /**
     * @return "id" attribute
     */
    public String id() {
        return this.getAttribute("id");
    }

    /**
     * @return "bandwidth" attribute
     */
    public double bandwidth() {
        return this.getDouble("bandwidth");
    }

    private double getDouble(final String attributeName) {
        return Double.parseDouble(this.getAttribute(attributeName));
    }

    /**
     * @return "latency" attribute
     */
    public double latency() {
        return this.getDouble("latency");
    }

    /**
     * @return inner "icon id" element's "global" attribute value
     */
    public int globalIconId() {
        return this.iconId().global();
    }

    /**
     * @return "global" attribute
     */
    public int global() {
        return this.getInt("global");
    }

    /**
     * @return inner element with tag "icon_id"
     */
    public WrappedElement iconId() {
        return this.firstTagElement("icon_id");
    }

    /**
     * @return "owner" attribute
     */
    public String owner() {
        return this.getAttribute("owner");
    }

    /**
     * @return "power" attribute
     */
    public double power() {
        return this.getDouble("power");
    }

    /**
     * @return "mem_alloc" attribute
     */
    public double memAlloc() {
        return this.getDouble("mem_alloc");
    }

    /**
     * @return "nodes" attribute
     */
    public int nodes() {
        return this.getInt("nodes");
    }

    /**
     * @return "vm_alloc" attribute
     */
    public String vmAlloc() {
        return this.getAttribute("vm_alloc");
    }

    /**
     * @return "disk_alloc" attribute
     */
    public double diskAlloc() {
        return this.getDouble("disk_alloc");
    }

    /**
     * @return "op_system" attribute
     */
    public String opSystem() {
        return this.getAttribute("op_system");
    }

    /**
     * @return "scheduler" attribute
     */
    public String scheduler() {
        return this.getAttribute("scheduler");
    }

    /**
     * @return "energy" attribute
     */
    public double energy() {
        return this.getDouble("energy");
    }

    /**
     * @return whether the attribute "master" in this element is true
     */
    public boolean isMaster() {
        return this.getBoolean("master");
    }

    private boolean getBoolean(final String attr) {
        return Boolean.parseBoolean(this.getAttribute(attr));
    }

    /**
     * @return "load" attribute
     */
    public double load() {
        return this.getDouble("load");
    }

    /**
     * @return whether this element has a inner "master" element
     */
    public boolean hasMasterAttribute() {
        return this.hasElementsWithTag("master");
    }

    private boolean hasElementsWithTag(final String master) {
        return this.getElementsByTagName(master).getLength() > 0;
    }

    /**
     * @return whether this element has a inner "characteristic" element
     */
    public boolean hasCharacteristicAttribute() {
        return this.hasElementsWithTag("characteristic");
    }

    /**
     * @return whether this element has a inner "cost" element
     */
    public boolean hasCostAttribute() {
        return this.hasElementsWithTag("cost");
    }

    /**
     * @return "cost_proc" attribute
     */
    public double costProcessing() {
        return this.getDouble("cost_proc");
    }

    /**
     * @return "cost_mem" attribute
     */
    public double costMemory() {
        return this.getDouble("cost_mem");
    }

    /**
     * @return "x" attribute
     */
    public int x() {
        return this.getInt("x");
    }

    /**
     * @return "y" attribute
     */
    public int y() {
        return this.getInt("y");
    }

    /**
     * @return "local" attribute
     */
    public int local() {
        return this.getInt("local");
    }

    /**
     * @return "powerlimit" attribute
     */
    public double powerLimit() {
        return this.getDouble("powerlimit");
    }

    /**
     * @return "cost_disk" attribute
     */
    public double costDisk() {
        return this.getDouble("cost_disk");
    }

    /**
     * @return "size" attribute
     */
    public double size() {
        return this.getDouble("size");
    }

    /**
     * @return "number" attribute, usually representing the number of cores
     */
    public int number() {
        return this.getInt("number");
    }

    /**
     * @return inner element with tag "characteristic", containing processing
     * and storage information
     */
    public WrappedElement characteristics() {
        return this.firstTagElement("characteristic");
    }

    /**
     * @return inner element with tag "process", representing the processor
     */
    public WrappedElement processor() {
        return this.firstTagElement("process");
    }

    /**
     * @return "memory" attribute
     */
    public WrappedElement memory() {
        return this.firstTagElement("memory");
    }

    /**
     * @return inner element with tag "hard_disk"
     */
    public WrappedElement hardDisk() {
        return this.firstTagElement("hard_disk");
    }

    /**
     * @return inner element with tag "cost"
     */
    public WrappedElement costs() {
        return this.firstTagElement("cost");
    }

    /**
     * @return "tasks" attribute
     */
    public int tasks() {
        return this.getInt("tasks");
    }

    /**
     * @return "time_arrival" attribute
     */
    public int arrivalTime() {
        return this.getInt("time_arrival");
    }

    /**
     * @return {@link Stream} of all inner elements with tag name "size"
     */
    public Stream<WrappedElement> sizes() {
        return this.elementsWithTag("size");
    }

    /**
     * @return whether this element's "type" attribute contains the value
     * "computing"
     */
    public boolean isComputingType() {
        return "computing".equals(this.type());
    }

    /**
     * @return "type" attribute
     */
    private String type() {
        return this.getAttribute("type");
    }

    /**
     * @return whether this element's "type" attribute contains the value
     * "communication"
     */
    public boolean isCommunicationType() {
        return "communication".equals(this.type());
    }

    /**
     * @return "minimum" attribute
     */
    public double minimum() {
        return this.getDouble("minimum");
    }

    /**
     * @return "maximum" attribute
     */
    public double maximum() {
        return this.getDouble("maximum");
    }

    /**
     * @return "average" attribute
     */
    public double average() {
        return this.getDouble("average");
    }

    /**
     * @return "probability" attribute
     */
    public double probability() {
        return this.getDouble("probability");
    }

    /**
     * @return "application" attribute
     */
    public String application() {
        return this.getAttribute("application");
    }

    /**
     * @return "id_master" attribute
     */
    public String masterId() {
        return this.getAttribute("id_master");
    }

    /**
     * @return "file_path" attribute
     */
    public String filePath() {
        return this.getAttribute("file_path");
    }

    /**
     * @return "format" attribute
     */
    public String format() {
        return this.getAttribute("format");
    }

    /**
     * @return {@link Stream} of all inner elements with tag name "random"
     */
    public Stream<WrappedElement> randomLoads() {
        return this.elementsWithTag("random");
    }

    /**
     * @return {@link Stream} of all inner elements with tag name "node"
     */
    public Stream<WrappedElement> nodeLoads() {
        return this.elementsWithTag("node");
    }

    /**
     * @return {@link Stream} of all inner elements with tag name "trace"
     */
    public Stream<WrappedElement> traceLoads() {
        return this.elementsWithTag("trace");
    }

    /**
     * @return "simulation_mode" attribute
     */
    public String simulationMode() {
        return this.getAttribute("simulation_mode");
    }

    /**
     * @return "number_threads" attribute
     */
    public int numberOfThreads() {
        return this.getInt("number_threads");
    }

    /**
     * @return "number_simulations" attribute
     */
    public int numberOfSimulations() {
        return this.getInt("number_simulations");
    }

    /**
     * @return inner element with tag "chart_create"
     */
    public WrappedElement chartCreate() {
        return this.firstTagElement("chart_create");
    }

    /**
     * @return whether the attribute "processing" is true
     */
    public boolean shouldChartProcessing() {
        return this.getBoolean("processing");
    }

    /**
     * @return whether the attribute "communication" is true
     */
    public boolean shouldChartCommunication() {
        return this.getBoolean("communication");
    }

    /**
     * @return whether the attribute "user_time" is true
     */
    public boolean shouldChartUserTime() {
        return this.getBoolean("user_time");
    }

    /**
     * @return whether the attribute "machine_time" is true
     */
    public boolean shouldChartMachineTime() {
        return this.getBoolean("machine_time");
    }

    /**
     * @return whether the attribute "task_time" is true
     */
    public boolean shouldChartTaskTime() {
        return this.getBoolean("task_time");
    }

    /**
     * @return inner element with tag "model_open"
     */
    public WrappedElement modelOpen() {
        return this.firstTagElement("model_open");
    }

    /**
     * @return "last_file" attribute
     */
    public String lastFile() {
        return this.getAttribute("last_file");
    }
}