package ispd.arquivo.exportador;

import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Utility class to convert an iSPD file to GridSim java file.
 * Construct it and call method {@link #export()}.
 */
/* package-private */ class GridSimExporter {
    private final Map<Integer, String> resources = new HashMap<>();

    private final NodeList machines;
    private final NodeList clusters;

    private final PrintWriter out;
    private final WrappedDocument doc;
    private final int userCount;

    /* package-private */ GridSimExporter(
            final Document model, final PrintWriter out) {
        this.doc = new WrappedDocument(model);
        this.out = out;

        this.userCount = (int) this.doc.owners().count();

        this.machines = model.getElementsByTagName("machine");
        this.clusters = model.getElementsByTagName("cluster");
    }

    /**
     * Export model to FilePrinter {@link #out} passed in the constructor.
     */
    public void export() {
        this.printHeader();
        this.printMain();

        this.printCreateGridUser();
        this.printCreateResource();
        this.printCreateGridlet();

        this.printFooter();
    }

    private void printHeader() {
        this.out.print("""
                    
                import java.util.*;
                import gridsim.*;
                import gridsim.net.*;
                    
                class Mestre extends GridSim {
                    
                    GridletList list;
                    private Integer ID_;
                    public Router r;
                    ArrayList Escravos_;
                    int Escal;
                    
                    
                    Mestre(String nome, Link link,GridletList list, ArrayList Escravo, int esc) throws Exception {
                        super(nome, link);
                        this.list = list;
                        this.ID_ = new Integer(getEntityId(nome));
                        this.Escravos_ = Escravo;
                        this.Escal=esc;
                    }
                    
                    @Override
                    public void body() {
                    
                        ArrayList<GridResource> resList = this.Escravos_;
                        int ids[] = new int[resList.size()];
                        double temp_ini, temp_fim;
                        
                        while (true) {
                            super.gridSimHold(2.0);
                            LinkedList recur = GridSim.getGridResourceList();
                            if (recur.size() > 0)
                                break;
                        }
                        
                        for(int j=0;j<resList.size(); j++){
                            ids[j] = resList.get(j).get_id();
                        }
                    
                        for(int i = 0; i < resList.size(); i++){
                            super.send(ids[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);
                        }
                        temp_ini = GridSim.clock();
                        if(this.Escal==1){ //O escalonador é Workqueue
                            int cont=0; int k; Gridlet gl;
                            for(k=0; k < Escravos_.size() && cont < list.size(); k++, cont++){
                                int num = resList.get(k).get_id();;
                                list.get(cont).setUserID(this.ID_);
                                super.gridletSubmit((Gridlet)list.get(cont),num , 0.0, true);
                            }
                            int res=0;
                            while(cont<list.size() || res<list.size()) {
                                 gl = super.gridletReceive();
                                res++;
                                int num = gl.getResourceID();
                                if(cont<list.size()){
                                    list.get(cont).setUserID(this.ID_);
                                    super.gridletSubmit((Gridlet)list.get(cont),num , 0.0, true);
                                    cont++;
                                }
                            }
                        }else{//É RoundRobin
                        
                        }
                        temp_fim = GridSim.clock();
                        System.out.println("TEMPO DE SIMULAÇÂO:"+(temp_fim-temp_ini));
                        super.shutdownGridStatisticsEntity();
                        super.shutdownUserEntity();
                         super.terminateIOEntities();
                         }
                    }
                """);
    }

    private void printMain() {
        this.out.print(MessageFormat.format("""
                                
                class Modelo'{'

                  	public static void main(String[] args) '{'

                		try '{'
                			Calendar calendar = Calendar.getInstance();
                			 boolean trace_flag = true;
                			String[] exclude_from_file = '{'""'}';
                			 String[] exclude_from_processing = '{'""'}';
                			GridSim.init({0},calendar, true, exclude_from_file,exclude_from_processing, null);

                			FIFOScheduler resSched = new FIFOScheduler( " GridResSched ");
                            double baud_rate = 100.0;
                            double delay =0.1;
                            int MTU = 100;""", this.userCount
        ));

        this.printResources();

        this.out.print(this.getTraceLoadString());

        this.printMasters();

        this.out.print("""

                            ResourceUserList userList = createGridUser();
                """);

        this.doc.internets().forEach(this::processNet);
        this.printNonMasterConnection();

        this.out.print("""

                            GridSim.startGridSimulation();
                                } catch (Exception e){
                              e.printStackTrace();
                             System.out.println("Unwanted ERRORS happened");
                        }
                    }
                """);
    }

    private void printCreateGridUser() {
        final var code = String.format("""

                    private static ResourceUserList createGridUser(){
                        ResourceUserList userList = new ResourceUserList();
                        %s
                        return userList;
                    }
                    
                """, this.userAdds());

        this.out.print(code);
    }

    private void printCreateResource() {
        this.out.print("""
                    
                    private static GridResource createResource(String name, double baud_rate, double delay, int MTU, int n_maq, int cap){
                    
                            MachineList mList = new MachineList();
                            for(int i = 0; i < n_maq; i++){
                                
                             mList.add( new Machine(i, 1, cap));
                        }
                    
                            String arch = "Sun Ultra";
                            String os = "Solaris";
                            double time_zone = 9.0;
                            double cost = 3.0;
                    
                        ResourceCharacteristics resConfig = new ResourceCharacteristics(arch, os, mList, ResourceCharacteristics.TIME_SHARED,time_zone, cost);
                    
                        long seed = 11L*13*17*19*23+1;
                        double peakLoad = 0.0;
                        double offPeakLoad = 0.0;
                        double holidayLoad = 0.0;
                    
                        LinkedList Weekends = new LinkedList();
                        Weekends.add(new Integer(Calendar.SATURDAY));
                        Weekends.add(new Integer(Calendar.SUNDAY));
                        LinkedList Holidays = new LinkedList();
                        GridResource gridRes=null;
                    
                        try
                         {
                             gridRes = new GridResource(name, new SimpleLink(name + "_link", baud_rate, delay, MTU),seed, resConfig, peakLoad, offPeakLoad, holidayLoad,Weekends, Holidays);
                    
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    
                        return gridRes;
                    }
                """);
    }

    private void printCreateGridlet() {
        this.out.print("""


                    private static GridletList createGridlet(){
                        double length;
                        long file_size;
                        Random random = new Random();

                        GridletList list = new GridletList();
                """);

        this.doc.loads().forEach(
                l -> LongStream.range(0, l.sizes().count())
                        .forEach(i -> this.processElementSizes(i, l))
        );

        this.out.print("""

                        return list;
                """);
    }

    private void printFooter() {
        this.out.print("""

                    }
                }
                """);
    }

    private void printResources() {
        this.printMachines();
        this.printClusters();
    }

    private String getTraceLoadString() {
        final var load = this.doc.loads().findFirst();

        if (load.isEmpty()) {
            return "";
        }

        final var traceLoad = load.get().traceLoads().findFirst();

        if (traceLoad.isEmpty()) {
            return """
                        
                                GridletList list = createGridlet();
                        
                    """;
        }

        return MessageFormat.format("""

                             String[] fileName = '{'
                                {0}

                            '}'

                             ArrayList load = new ArrayList();
                             for (i = 0; i < fileName.length; i++)'{'
                                Workload w = new Workload("Load_"+i, fileName[i], resList[], rating);
                                load.add(w);
                            '}'
                """, traceLoad.get().filePath());
    }

    private void printMasters() {
        this.out.printf("""

                Link link = new SimpleLink("link_", 100, 0.01, 1500 );
                """);

        for (int i = 0; i < this.machines.getLength(); i++)
            this.processMaster(i,
                    new WrappedElement((Element) this.machines.item(i)));
    }

    private void processNet(final WrappedElement e) {
        final var id = e.id();

        this.resources.put(e.globalIconId(), id);

        this.out.print("""
                Router r_%s = new RIPRouter(%s, trace_flag);
                """.formatted(id, id));
    }

    private void printNonMasterConnection() {
        this.out.print("""
                    
                            FIFOScheduler rSched = new FIFOScheduler("r_Sched");
                """);

        final var ls = this.doc.links().toList();

        for (int i = 0; i < ls.size(); ++i) {
            this.printLink(ls.get(i), i);
        }
    }

    private String userAdds() {
        return IntStream.range(0, this.userCount)
                .mapToObj(GridSimExporter::addUserId)
                .collect(Collectors.joining());
    }

    private void processElementSizes(final long i, final WrappedElement e) {
        final var computation = e.makeTwoStageFromInnerSizes(
                WrappedElement::isComputingType,
                WrappedElement::toTwoStageImplicitProb
        ).rangeNormalized();

        final var communication = e.makeTwoStageFromInnerSizes(
                WrappedElement::isCommunicationType,
                WrappedElement::toTwoStageImplicitProb
        ).rangeNormalized();

        final var msg = MessageFormat.format("""
                                length = GridSimRandom.real({0},{1},{2},random.nextDouble());
                                file_size = (long) GridSimRandom.real({3},{4},{5},random.nextDouble());
                                Gridlet gridlet{6} = new Gridlet({6}, length, file_size,file_size);
                                list.add(gridlet{6});

                                gridlet{6}.setUserID(0);
                        """,
                computation.intervalSplit(),
                computation.minimum(),
                computation.maximum(),
                communication.intervalSplit(),
                communication.minimum(),
                communication.maximum(),
                i
        );

        this.out.print(msg);
    }

    private void printMachines() {
        for (int i = 0; i < this.machines.getLength(); i++) {
            final var machine = (Element) this.machines.item(i);
            final var e = new WrappedElement(machine);
            if (!e.hasMasterAttribute()) {
                this.printResource(i, 1, e);
            }
        }
    }

    private void printClusters() {
        for (int j = 0, i = this.machines.getLength(); i < this.machines.getLength() + this.clusters.getLength(); i++, j++) {
            final var cluster = (Element) this.clusters.item(j);
            final var e = new WrappedElement(cluster);
            this.printResource(i, e.nodes(), e);
        }
    }

    private void processMaster(final int id, final WrappedElement e) {
        if (!e.hasMasterAttribute())
            return;

        this.resources.put(e.globalIconId(), e.id());

        final var slaves = e.master().slaves().toList();

        this.out.print(MessageFormat.format("""

                            ArrayList esc{0} = new ArrayList();
                """, id));

        slaves.stream()
                .map(WrappedElement::id)
                .map(Integer::parseInt)
                .forEach(i -> this.out.print("""
                                    esc%d.add(%s);
                        """.formatted(id, this.resources.get(i))));

        this.out.print(MessageFormat.format("""

                                    Mestre {0} = new Mestre("{0}_", link, list, esc{1}, {2});
                                    Router r_{0} = new RIPRouter( "router_{2}", trace_flag);
                                    r_{0}.attachHost( {0}, resSched);
                        """,
                e.id(),
                id,
                slaves.size()
        ));

        slaves.stream()
                .map(WrappedElement::id)
                .map(Integer::parseInt)
                .forEach(i -> this.out.print(("""

                                    r_%s.attachHost( %s, resSched);
                        """).formatted(e.id(), this.resources.get(i))));
    }

    private void printLink(final WrappedElement e, final int id) {
        this.out.print(String.format("""
                            
                                    Link %s = new SimpleLink("link_%d", %s*1000, %s*1000,1500  );
                        """,
                e.id(),
                id,
                e.bandwidth(),
                e.latency()
        ));
    }

    private static String addUserId(final int i) {
        return String.format("""
                        userList.add(%d);
                """, i);
    }

    private void printResource(
            final int index, final int nodes, final WrappedElement e) {

        this.resources.put(e.globalIconId(), e.id());

        this.out.print(MessageFormat.format("""

                                    GridResource {0} = createResource("{0}_",  baud_rate,  delay,  MTU, {1}, (int){2});
                                    Router r_{0} = new RIPRouter( "router_{3}", trace_flag);
                                    r_{0}.attachHost( {0}, resSched);
                        """,
                e.id(),
                nodes,
                e.power(),
                index
        ));
    }
}
