package ispd.arquivo.xml;

import ispd.arquivo.xml.models.builders.CloudQueueNetworkBuilder;
import ispd.arquivo.xml.models.builders.IconicModelBuilder;
import ispd.arquivo.xml.models.builders.LoadBuilder;
import ispd.arquivo.xml.models.builders.QueueNetworkBuilder;
import ispd.arquivo.xml.models.builders.ServiceCenterBuilder;
import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.gui.PickModelTypeDialog;
import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.VirtualMachine;
import ispd.motor.workload.impl.CollectionWorkloadGenerator;
import ispd.motor.workload.impl.TraceFileWorkloadGenerator;
import ispd.motor.workload.impl.GlobalWorkloadGenerator;
import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for manipulating xml files into iconic or simulable models,
 * and building a document with a model from an iconic one.
 */
public class IconicoXML {
    private static final Element[] NO_CHILDREN = {};
    private static final Object[][] NO_ATTRS = {};
    private static final int DEFAULT_MODEL_TYPE = -1;
    private final WrappedDocument doc =
            new WrappedDocument(ManipuladorXML.newDocument());
    private final Element system = this.doc.createElement("system");
    private Element load = null;

    public IconicoXML() {
        this(IconicoXML.DEFAULT_MODEL_TYPE);
    }

    public IconicoXML(final int modelType) {
        this.system.setAttribute("version",
                IconicoXML.getVersionForModelType(modelType));
        this.doc.appendChild(this.system);
    }

    /**
     * @throws IllegalArgumentException if modelType is not in -1, 0, 1 or 2
     */
    private static String getVersionForModelType(final int modelType) {
        return switch (modelType) {
            case PickModelTypeDialog.GRID -> "2.1";
            case PickModelTypeDialog.IAAS -> "2.2";
            case PickModelTypeDialog.PAAS -> "2.3";
            case IconicoXML.DEFAULT_MODEL_TYPE -> "1.2";
            default -> throw new IllegalArgumentException(
                    "Invalid model type " + modelType);
        };
    }

    /**
     * Write iconic model in {@link Document} to path
     *
     * @param doc  {@link Document} containing an iconic model
     * @param path path in which to save the file
     * @return {@code true} if the file was saved successfully, {@code false}
     * otherwise
     */
    public static boolean escrever(final Document doc, final File path) {
        return ManipuladorXML.write(doc, path, "iSPD.dtd", false);
    }

    /**
     * Reads xml file in path and parses it into a {@link Document}
     * containing an iconic model
     *
     * @param path path to xml file with an iconic model
     * @return {@link Document} with the iconic model in the file
     */
    public static Document ler(final File path) throws ParserConfigurationException, IOException, SAXException {
        return ManipuladorXML.read(path, "iSPD.dtd");
    }

    /**
     * Checks the integrity of the model in the {@link Document}.
     * Performs very simple checks such as if the model has at least one user
     * and machine.
     *
     * @param doc {@link Document} containing iconic model
     * @throws IllegalArgumentException if the model is incomplete
     */
    public static void validarModelo(final Document doc) {
        final var document = new WrappedDocument(doc);

        if (document.hasNoOwners()) {
            throw new IllegalArgumentException("The model has no users.");
        }

        if (document.hasNoMachines() && document.hasNoClusters()) {
            throw new IllegalArgumentException("The model has no icons.");
        }

        if (document.hasNoLoads()) {
            throw new IllegalArgumentException(
                    "One or more workloads have not been configured.");
        }

        if (document.hasNoMasters()) {
            throw new IllegalArgumentException(
                    "One or more parameters have not been configured.");
        }
    }

    /**
     * Convert an iconic model into a queue network, usable in the simulation
     * motor.
     *
     * @param model Object from xml with modeled computational grid
     * @return Simulable queue network, in accordance to given model
     */
    public static RedeDeFilas newRedeDeFilas(final Document model) {
        return new QueueNetworkBuilder()
                .parseDocument(new WrappedDocument(model))
                .build();
    }

    /**
     * Convert an iconic model into a cloud queue network, usable in the cloud
     * simulation motor.
     *
     * @param model Object from xml with modeled computational grid
     * @return Simulable cloud queue network, in accordance to given model
     */
    public static RedeDeFilasCloud newRedeDeFilasCloud(final Document model) {
        return (RedeDeFilasCloud) new CloudQueueNetworkBuilder()
                .parseDocument(new WrappedDocument(model))
                .build();
    }

    /**
     * Get load configuration containing in the iconic model present in the
     * {@link Document}
     *
     * @return {@link WorkloadGenerator} with load configuration from the model, if
     * a valid one is present, {@code null} otherwise
     * @see LoadBuilder
     * @see TraceFileWorkloadGenerator
     * @see CollectionWorkloadGenerator
     * @see GlobalWorkloadGenerator
     */
    public static WorkloadGenerator newGerarCarga(final Document doc) {
        final var model = LoadBuilder.build(new WrappedDocument(doc));
        if (model.isEmpty()) {
            return null;
        }
        return model.get();
    }

    /**
     * Add iconic model vertices and edges to the collections passed as
     * arguments. <b>The collections are modified.</b>
     *
     * @param doc {@link Document} containing the iconic model
     * @see IconicModelBuilder
     */
    public static void newGrade(
            final Document doc,
            final Collection<? super Vertex> vertices,
            final Collection<? super Edge> edges) {
        final var model = new IconicModelBuilder(
                new WrappedDocument(doc)).build();
        vertices.addAll(model.vertices());
        edges.addAll(model.edges());
    }

    /**
     * @return set with all user ids from the iconic model
     */
    public static HashSet<String> newSetUsers(final Document doc) {
        return new WrappedDocument(doc).owners()
                .map(WrappedElement::id)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * @return list with all user ids from the iconic model
     */
    public static List<String> newListUsers(final Document doc) {
        return new WrappedDocument(doc).owners()
                .map(WrappedElement::id)
                .toList();
    }

    /**
     * @return set with all virtual machines from the (cloud) iconic model
     */
    public static HashSet<VirtualMachine> newListVirtualMachines(final Document doc) {
        return new WrappedDocument(doc).virtualMachines()
                .map(ServiceCenterBuilder::aVirtualMachineWithVmm)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * @return map with all user power limits, indexed by id
     */
    public static HashMap<String, Double> newListPerfil(final Document doc) {
        return new WrappedDocument(doc).owners()
                .collect(Collectors.toMap(
                        WrappedElement::id,
                        WrappedElement::powerLimit,
                        (prev, next) -> next,
                        HashMap::new
                ));
    }

    /**
     * Parse xml file multiple times, producing copies of the resulting
     * {@link Document}
     *
     * @param file   file to be parsed
     * @param number number of copies to be produced
     * @return array of length {@code number} of identical {@link Document}s
     * @throws SAXException if the file is ill-formed
     */
    public static Document[] clone(final File file, final int number)
            throws ParserConfigurationException, IOException, SAXException {

        final var builder = IconicoXML.getCloningBuilder();

        final var docs = new Document[number];

        for (int i = 0; i < number; i++) {
            builder.setEntityResolver(new CloningEntityResolver());
            docs[i] = builder.parse(file);
        }

        return docs;
    }

    private static DocumentBuilder getCloningBuilder() throws ParserConfigurationException {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        return factory.newDocumentBuilder();
    }

    /**
     * Add users' ids and power limit info to the current model being built.
     *
     * @param users  collection of userIds
     * @param limits map with users' power limits
     * @throws NullPointerException if a user id given in the collection is
     *                              missing from the map of power limits
     */
    public void addUsers(final Collection<String> users,
                         final Map<String, Double> limits) {
        users.stream()
                .map(user -> this.anElement("owner",
                        "id", user,
                        "powerlimit", limits.get(user)
                ))
                .forEach(this.system::appendChild);
    }

    /**
     * Create an element with only two attributes, k1 and k2, with values v1
     * and v2, respectively.
     * See {@link #anElement(String, Object[][], Node[])} for further info.
     *
     * @return element with two attributes k1, k2 of value v1, v2
     */
    private Element anElement(
            final String name,
            final String k1, final Object v1,
            final String k2, final Object v2) {
        return this.anElement(name, new Object[][] {
                { k1, v1 },
                { k2, v2 },
        });
    }

    /**
     * Create an element with no children.
     * See {@link #anElement(String, Object[][], Node[])} for further info.
     *
     * @return children-less element.
     */
    private Element anElement(
            final String name, final Object[][] attrs) {
        return this.anElement(name, attrs, IconicoXML.NO_CHILDREN);
    }

    /**
     * Create an element in the {@link Document} being currently built.
     * The element needs a name, a map of attributes, and (optionally) children.
     *
     * @param name     name of the element
     * @param attrs    array of width 2 (key, value) with the attributes to
     *                 be added to the element
     * @param children children to be appended to the built element
     * @return Element with the given name, attributes and children
     */
    private Element anElement(
            final String name,
            final Object[][] attrs,
            final Node[] children) {
        final var e = this.doc.createElement(name);

        for (final var attr : attrs) {
            final var key = attr[0];
            final var value = attr[1];
            e.setAttribute((String) key, value.toString());
        }

        Arrays.stream(children)
                .forEach(e::appendChild);

        return e;
    }

    /**
     * Add internet icon with the given attributes to the current model being
     * built.
     */
    public void addInternet(
            final int x, final int y,
            final int idLocal, final int idGlobal, final String name,
            final double bandwidth, final double internetLoad,
            final double latency) {
        this.system.appendChild(this.anElement(
                "internet", new Object[][] {
                        { "id", name },
                        { "bandwidth", bandwidth },
                        { "load", internetLoad },
                        { "latency", latency },
                }, new Element[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(idGlobal, idLocal),
                }
        ));
    }

    /**
     * Add cluster icon with the given attributes to the current model being
     * built.
     * <b>Note:</b> its computational, storage and memory costs are set to 0.
     */
    public void addCluster(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Integer slaveCount,
            final Double power, final Integer coreCount,
            final Double memory, final Double disk,
            final Double bandwidth, final Double latency,
            final String scheduler,
            final String owner,
            final Boolean isMaster,
            final Double energy) {
        this.system.appendChild(this.anElement(
                "cluster", new Object[][] {
                        { "nodes", slaveCount },
                        { "power", power },
                        { "bandwidth", bandwidth },
                        { "latency", latency },
                        { "scheduler", scheduler },
                        { "owner", owner },
                        { "master", isMaster },
                        { "id", name },
                        { "energy", energy },
                }, new Node[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(globalId, localId),
                        this.newCharacteristic(
                                power, coreCount, memory, disk,
                                0.0, 0.0, 0.0
                        ),
                }
        ));
    }

    /**
     * Add IaaS cluster icon with the given attributes to the current model
     * being built.
     * <B>Note:</B> no 'energy' attribute is added to the element.
     */
    public void addClusterIaaS(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Integer slaveCount,
            final Double power, final Integer coreCount,
            final Double memory, final Double disk,
            final Double bandwidth, final Double latency,
            final String scheduler, final String vmAlloc,
            final Double processingCost, final Double memoryCost,
            final Double diskCost,
            final String owner, final Boolean isMaster) {
        this.system.appendChild(this.anElement(
                "cluster", new Object[][] {
                        { "id", name },
                        { "nodes", slaveCount },
                        { "power", power },
                        { "bandwidth", bandwidth },
                        { "latency", latency },
                        { "scheduler", scheduler },
                        { "vm_alloc", vmAlloc },
                        { "owner", owner },
                        { "master", isMaster },
                }, new Node[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(globalId, localId),
                        this.newCharacteristic(
                                power, coreCount, memory, disk,
                                processingCost, memoryCost, diskCost
                        )
                }
        ));
    }

    /**
     * Create an element with icon id information: those being the local and
     * global ids.
     */
    private Element anIconIdElement(final int global, final int local) {
        return this.anElement("icon_id", "global", global, "local", local);
    }

    /**
     * Create an element with attributes describing the characteristics of a
     * processing center, such as computational power and cost, storage, etc.
     */
    private Node newCharacteristic(final Double power, final Integer coreCount,
                                   final Double memory, final Double disk,
                                   final Double processingCost,
                                   final Double memoryCost,
                                   final Double diskCost) {
        return this.anElement(
                "characteristic", IconicoXML.NO_ATTRS, new Element[] {
                        this.anElement("process",
                                "power", power,
                                "number", coreCount),
                        this.anElement("memory", "size", memory),
                        this.anElement("hard_disk", "size", disk),
                        this.anElement("cost", new Object[][] {
                                { "cost_proc", processingCost },
                                { "cost_mem", memoryCost },
                                { "cost_disk", diskCost },
                        }),
                }
        );
    }

    /**
     * Add a machine icon with the given attributes to the current model being
     * built.
     */
    public void addMachine(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final boolean isMaster, final Collection<Integer> slaves,
            final Double energy) {
        this.addMachineInner(x, y, localId, globalId, name,
                power, occupancy, scheduler, owner, coreCount, memory, disk,
                null, null, null, isMaster, slaves,
                new Object[][] { { "energy", energy } }, IconicoXML.NO_ATTRS
        );
    }

    /**
     * Create an element with attributes describing the characteristics of a
     * processing center, but without costs.
     * See
     * {@link #newCharacteristic(Double, Integer, Double, Double, Double, Double, Double)} for further info.
     */
    private Node newCharacteristic(final Double power, final Integer coreCount,
                                   final Double memory, final Double disk) {
        return this.anElement(
                "characteristic", IconicoXML.NO_ATTRS, new Element[] {
                        this.anElement("process",
                                "power", power,
                                "number", coreCount
                        ),
                        this.anElement("memory", "size", memory),
                        this.anElement("hard_disk", "size", disk),
                });
    }

    /**
     * Create a simple element with just a name and one attribute
     */
    private Element anElement(
            final String name, final String key, final Object value) {
        return this.anElement(name, new Object[][] {
                { key, value },
        });
    }

    /**
     * Add a machine icon with the given attributes to the current model being
     * built.
     * <b>Notes:</b> No 'energy' attribute is added to the element; costs are
     * set to 0.
     * See
     * {@link #addMachine(Integer, Integer, Integer, Integer, String, Double, Double, String, String, Integer, Double, Double, boolean, Collection, Double)}.
     */
    public void addMachine(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final boolean isMaster, final Collection<Integer> slaves) {
        this.addMachineInner(x, y, localId, globalId, name,
                power, occupancy, scheduler, owner, coreCount, memory, disk,
                0.0, 0.0, 0.0, isMaster, slaves,
                IconicoXML.NO_ATTRS, IconicoXML.NO_ATTRS
        );
    }

    /**
     * Add a IaaS machine icon with the given attributes to the current model
     * being built.
     * <b>Notes:</b> No 'energy' attribute is added to the element; an extra
     * 'vm_alloc' attribute is added, compared to the other machine-adding
     * methods.
     * See
     * {@link #addMachine(Integer, Integer, Integer, Integer, String, Double, Double, String, String, Integer, Double, Double, boolean, Collection, Double)}.
     */
    public void addMachineIaaS(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String vmAlloc, final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final Double costPerProcessing,
            final Double costPerMemory,
            final Double costPerDisk,
            final boolean isMaster, final Collection<Integer> slaves) {
        this.addMachineInner(x, y, localId, globalId, name,
                power, occupancy, scheduler, owner, coreCount, memory, disk,
                costPerProcessing, costPerMemory, costPerDisk, isMaster, slaves,
                IconicoXML.NO_ATTRS, new Object[][] { { "vm_alloc", vmAlloc }, }
        );
    }

    /**
     * Helper method to abstract away the addition of a machine element to
     * the model being built. It takes in all attributes in common between
     * the methods
     * {@link #addMachine(Integer, Integer, Integer, Integer, String, Double, Double, String, String, Integer, Double, Double, boolean, Collection)},
     * {@link #addMachine(Integer, Integer, Integer, Integer, String, Double, Double, String, String, Integer, Double, Double, boolean, Collection, Double)},
     * and
     * {@link #addMachineIaaS(Integer, Integer, Integer, Integer, String, Double, Double, String, String, String, Integer, Double, Double, Double, Double, Double, boolean, Collection)},
     * but also two extra params, {@code extraAttrs} and {@code
     * extraMasterAttrs}, which are arrays containing the specific attributes
     * of each of the outer methods.
     *
     * @param isMaster         indicates whether or not to include a inner
     *                         'master' element
     * @param extraAttrs       extra attributes to be added ot the element
     * @param extraMasterAttrs extra attributes to be added ot the inner
     *                         'master' element, if the element is a master
     */
    private void addMachineInner(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final Double costPerProcessing,
            final Double costPerMemory,
            final Double costPerDisk,
            final boolean isMaster, final Collection<Integer> slaves,
            final Object[][] extraAttrs, final Object[][] extraMasterAttrs) {
        // Note: Arrays.asList returns a fixed-size list, which throws on .add()
        final var attrList = Arrays.stream(new Object[][] {
                { "id", name },
                { "power", power },
                { "load", occupancy },
                { "owner", owner },
        }).collect(Collectors.toList());

        attrList.addAll(Arrays.asList(extraAttrs));

        final Node characteristic;

        if (costPerProcessing != null) {
            characteristic = this.newCharacteristic(
                    power, coreCount, memory, disk,
                    costPerProcessing, costPerMemory, costPerDisk
            );
        } else {
            characteristic = this.newCharacteristic(
                    power, coreCount, memory, disk
            );
        }

        final var machine = this.anElement(
                "machine", attrList.toArray(Object[][]::new), new Node[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(globalId, localId),
                        characteristic,
                }
        );

        if (isMaster) {
            machine.appendChild(this.aMasterElement(
                    scheduler, slaves, extraMasterAttrs
            ));
        }

        this.system.appendChild(machine);
    }

    /**
     * Create a master element with given scheduling policy, and slaves as
     * children.
     *
     * @param extraAttrs potential extra attributes to add to the element
     */
    private Element aMasterElement(final String scheduler,
                                   final Collection<Integer> slaves,
                                   final Object[][] extraAttrs) {
        // Note: Arrays.asList returns a fixed-size list, which throws on .add()
        final var attrList = Arrays.stream(new Object[][] {
                { "scheduler", scheduler },
        }).collect(Collectors.toList());

        attrList.addAll(Arrays.asList(extraAttrs));

        return this.anElement(
                "master", attrList.toArray(Object[][]::new),
                slaves.stream()
                        .map(this::aSlaveElement)
                        .toArray(Element[]::new)
        );
    }

    /**
     * Simple slave element with given id
     */
    private Element aSlaveElement(final Integer id) {
        return this.anElement("slave", "id", id);
    }

    /**
     * Add a link icon with the given attributes to the current model being
     * built.
     */
    public void addLink(
            final int x0, final int y0,
            final int x1, final int y1,
            final int localId, final int globalId,
            final String name, final double bandwidth,
            final double linkLoad, final double latency,
            final int origination, final int destination) {
        this.system.appendChild(this.anElement(
                "link", new Object[][] {
                        { "id", name },
                        { "bandwidth", bandwidth },
                        { "load", linkLoad },
                        { "latency", latency },
                }, new Element[] {
                        this.anElement("connect",
                                "origination", origination,
                                "destination", destination),
                        this.aPositionElement(x0, y0),
                        this.aPositionElement(x1, y1),
                        this.anIconIdElement(globalId, localId),
                }
        ));
    }

    /**
     * Create a position element with position information (x, y)
     */
    private Element aPositionElement(final int x, final int y) {
        return this.anElement("position", "x", x, "y", y);
    }

    /**
     * Add a virtual machine icon with the given attributes to the current
     * model being built.
     */
    public void addVirtualMachines(
            final String id, final String owner, final String vmm,
            final int power, final double memory, final double disk,
            final String os) {
        this.system.appendChild(this.anElement(
                "virtualMac", new Object[][] {
                        { "id", id },
                        { "owner", owner },
                        { "vmm", vmm },
                        { "power", power },
                        { "mem_alloc", memory },
                        { "disk_alloc", disk },
                        { "op_system", os },
                }
        ));
    }

    /**
     * Add a random load to the current model being built.
     *
     * @apiNote This method just be called at most <b>once</b>> per instance,
     * and not mixed with calls to
     * {@link #setLoadTrace(String, int, String)} or
     * {@link #addLoadNo(String, String, String, Integer, Double, Double, Double, Double)}
     */
    public void setLoadRandom(
            final int taskCount, final int arrivalTime,
            final double compMax, final double compAvg,
            final double compMin, final double compProb,
            final double commMax, final double commAvg,
            final double commMin, final double commProb) {
        this.addElementToLoad(this.anElement(
                "random", new Object[][] {
                        { "tasks", taskCount },
                        { "time_arrival", arrivalTime },
                }, new Element[] {
                        this.anElement("size", new Object[][] {
                                { "type", "computing" },
                                { "maximum", compMax },
                                { "average", compAvg },
                                { "minimum", compMin },
                                { "probability", compProb },
                        }),
                        this.anElement("size", new Object[][] {
                                { "type", "communication" },
                                { "maximum", commMax },
                                { "average", commAvg },
                                { "minimum", commMin },
                                { "probability", commProb },
                        }),
                }
        ));
    }

    private void addElementToLoad(final Node elem) {
        this.createLoadIfNull();
        this.load.appendChild(elem);
    }

    private void createLoadIfNull() {
        if (this.load == null) {
            this.load = this.doc.createElement("load");
            this.system.appendChild(this.load);
        }
    }

    /**
     * Add a per-node load to the current model being built.
     *
     * @apiNote This method may be called more than once per instance,
     * however it should be mixed with calls to
     * {@link #setLoadTrace(String, int, String)} or
     * {@link #setLoadRandom(int, int, double, double, double, double, double, double, double, double)}.
     */
    public void addLoadNo(
            final String application,
            final String owner,
            final String masterId,
            final Integer taskCount,
            final Double maxComp, final Double minComp,
            final Double maxComm, final Double minComm) {
        this.addElementToLoad(this.anElement(
                "node", new Object[][] {
                        { "application", application },
                        { "owner", owner },
                        { "id_master", masterId },
                        { "tasks", taskCount },
                }, new Element[] {
                        this.anElement("size", new Object[][] {
                                { "type", "computing" },
                                { "maximum", maxComp },
                                { "minimum", minComp },
                        }),
                        this.anElement("size", new Object[][] {
                                { "type", "communication" },
                                { "maximum", maxComm },
                                { "minimum", minComm },
                        }),
                }
        ));
    }

    /**
     * Add a trace load to the current model being built.
     *
     * @apiNote This method just be called at most <b>once</b>> per instance,
     * and not mixed with calls to
     * {@link #setLoadRandom(int, int, double, double, double, double, double, double, double, double)} or
     * {@link #addLoadNo(String, String, String, Integer, Double, Double, Double, Double)}
     */
    public void setLoadTrace(
            final String file, final int tasks, final String format) {
        this.addElementToLoad(this.anElement(
                "trace", new Object[][] {
                        { "file_path", file },
                        { "tasks", tasks },
                        { "format", format },
                }
        ));
    }

    /**
     * Get {@link Document} with iconic model generated.
     */
    public Document getDescricao() {
        return this.doc.document;
    }

    private static class CloningEntityResolver implements EntityResolver {
        private final InputSource substitute = new InputSource(
                IconicoXML.class.getResourceAsStream("iSPD.dtd"));

        public InputSource resolveEntity(
                final String publicId, final String systemId) {
            return this.substitute;
        }
    }
}