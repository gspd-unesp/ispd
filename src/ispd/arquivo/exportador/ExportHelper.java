package ispd.arquivo.exportador;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.util.HashMap;

class ExportHelper
{
    private final HashMap<? super Integer, ? super String> resources;
    private final NodeList users;
    private final NodeList machines;
    private final NodeList clusters;
    private final NodeList internet;
    private final NodeList links;
    private final NodeList loads;

    ExportHelper (final Document model)
    {
        this.resources = new HashMap<>(0);
        this.users = model.getElementsByTagName("owner");
        this.machines = model.getElementsByTagName("machine");
        this.clusters = model.getElementsByTagName("cluster");
        this.internet = model.getElementsByTagName("internet");
        this.links = model.getElementsByTagName("link");
        this.loads = model.getElementsByTagName("load");
    }

    void printCodeToFile (final PrintWriter pw)
    {
        ExportHelper.printHeader(pw);

        this.printMain(pw);
        this.printCreateGridUser(pw);

        ExportHelper.printCreateResource(pw);

        this.printCreateGridlet(pw);
    }

    private static void printHeader (final PrintWriter pw)
    {
        pw.print("""
                    
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

    private void printMain (final PrintWriter pw)
    {
        pw.println(" \nclass Modelo{ \n\n  \tpublic static void main(String[] args) {\n");
        pw.println("\t\ttry {");
        pw.println("\t\t\tCalendar calendar = Calendar.getInstance(); \n\t\t\t boolean trace_flag = true;");
        pw.println("\t\t\tString[] exclude_from_file = {\"\"}; \n\t\t\t String[] exclude_from_processing = {\"\"};");
        pw.println("\t\t\tGridSim.init(" + this.users.getLength() + ",calendar, true, exclude_from_file,exclude_from_processing, null);");
        pw.println("\n\t\t\tFIFOScheduler resSched = new FIFOScheduler( \" GridResSched \");");

        pw.print("""
                            double baud_rate = 100.0;
                            double delay =0.1;
                            int MTU = 100;
                """);

        this.printResources(pw);

        pw.print(this.getLoadTraceString());

        this.printMasters(pw);

        pw.println("\n\t\t\tResourceUserList userList = createGridUser();");

        this.printInternet(pw);
        this.printNonMasterConnection(pw);

        pw.println("\n\t\t\tGridSim.startGridSimulation();");

        pw.println("\t\t} \t\tcatch (Exception e){ ");
        pw.println("\t\t\t  e.printStackTrace();\n \t\t\tSystem.out.println(\"Unwanted ERRORS happened\"); \n\t\t} \n\t} ");
    }

    private void printCreateGridUser (final PrintWriter pw)
    {
        pw.print(String.format("""

                    private static ResourceUserList createGridUser(){
                        ResourceUserList userList = new ResourceUserList();
                        %s
                        return userList;
                    }
                    
                """, this.userAdds())
        );
    }

    private static void printCreateResource (final PrintWriter pw)
    {
        pw.print("""
                    
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

    private void printCreateGridlet (final PrintWriter pw)
    {
        pw.println("\n\n\tprivate static GridletList createGridlet(){ \n\t\tdouble length; \n\t\tlong file_size;\n\t\tRandom random = new Random();");
        pw.println("\n\t\tGridletList list = new GridletList();");
        for (int i = 0; i < this.loads.getLength(); i++)
        {
            final var sizes = ((Element) this.loads.item(i)).getElementsByTagName("size");
            ExportHelper.processLoadValues(sizes, pw);
        }
        pw.println("\n\t} \n}");
    }

    private void printResources (final PrintWriter pw)
    {
        this.printMachines(pw);
        this.printClusters(pw);
    }

    private String getLoadTraceString ()
    {
        if (0 == this.loads.getLength())
            return "";

        final var trace = ((Element) this.loads.item(0)).getElementsByTagName("trace");

        if (0 == trace.getLength())
            return """
                        
                                GridletList list = createGridlet();
                        
                    """;

        return String.format("""
                    
                             String[] fileName = {
                                %s
                    
                            }
                    
                             ArrayList load = new ArrayList();
                             for (i = 0; i < fileName.length; i++){
                                Workload w = new Workload("Load_"+i, fileName[i], resList[], rating);
                                load.add(w);
                            }
                """, ((Element) trace.item(0)).getAttribute("file_path"));
    }

    private void printMasters (final PrintWriter pw)
    {
        pw.println("\n\t\t\tLink link = new SimpleLink(\"link_\", 100, 0.01, 1500 );");
        for (int i = 0; i < this.machines.getLength(); i++)
            this.printMaster((Element) this.machines.item(i), i, pw);
    }

    private void printInternet (final PrintWriter pw)
    {
        for (int i = 0; i < this.internet.getLength(); i++)
        {
            final var net = (Element) this.internet.item(i);
            this.resources.put(Integer.parseInt(((Element) net.getElementsByTagName("icon_id").item(0)).getAttribute("global")), net.getAttribute("id"));
            pw.println("\t\t\tRouter r_" + net.getAttribute("id") + " = new RIPRouter(" + net.getAttribute("id") + ",trace_flag);");
        }
    }

    private void printNonMasterConnection (final PrintWriter pw)
    {
        pw.print("""
                    
                \t\t\tFIFOScheduler rSched = new FIFOScheduler("r_Sched");
                """);

        for (int i = 0; i < this.links.getLength(); i++)
        {//Pega cada conexão que nao seja mestre
            final var link = (Element) this.links.item(i);
            final var connect = (Element) link.getElementsByTagName("connect").item(0);
            Integer.parseInt(connect.getAttribute("origination"));
            Integer.parseInt(connect.getAttribute("destination"));
            pw.print(String.format("""
                                
                                        Link %s = new SimpleLink("link_%d", %s*1000, %s*1000,1500  );
                            """,
                    link.getAttribute("id"),
                    i,
                    link.getAttribute("bandwidth"),
                    link.getAttribute("latency"))
            );
        }
    }

    private String userAdds ()
    {
        final StringBuilder sb = new StringBuilder(0);

        for (int i = 0; i < this.users.getLength(); i++)
            sb.append(String.format("""
                            userList.add(%d);
                    """, i));

        return sb.toString();
    }

    private static void processLoadValues (final NodeList sizes, final PrintWriter pw)
    {
        double minComputation = 0;
        double maxComputation = 0;
        double computationValue = 0;
        double communicationValue = 0;
        double mincp = 0;
        double maxcp = 0;
        double mincm = 0;
        double maxcm = 0;
        for (int k = 0; k < sizes.getLength(); k++)
        {
            final Element size = (Element) sizes.item(k);

            if ("computing".equals(size.getAttribute("type")))
            {
                minComputation = Double.parseDouble(size.getAttribute("minimum"));
                maxComputation = Double.parseDouble(size.getAttribute("maximum"));
                computationValue = Double.parseDouble(size.getAttribute("average"));
                mincp = (computationValue - minComputation) / computationValue;
                mincp = Math.min(1.0, mincp);
                maxcp = (maxComputation - computationValue) / computationValue;
                maxcp = Math.min(1.0, maxcp);
            } else if ("communication".equals(size.getAttribute("type")))
            {
                Double.parseDouble(size.getAttribute("minimum"));
                Double.parseDouble(size.getAttribute("maximum"));
                communicationValue = Double.parseDouble(size.getAttribute("average"));
                mincm = (communicationValue - minComputation) / communicationValue;
                mincp = Math.min(1.0, mincm);
                maxcm = (maxComputation - communicationValue) / communicationValue;
                maxcp = Math.min(1.0, maxcm);
            }
            pw.println("\t\tlength = GridSimRandom.real(" + computationValue + "," + mincp + "," + maxcp + ",random.nextDouble());");
            pw.println("\t\tfile_size = (long) GridSimRandom.real(" + communicationValue + "," + mincm + "," + maxcm + ",random.nextDouble());");
            pw.println("\t\tGridlet gridlet" + k + " = new Gridlet(" + k + ", length, file_size,file_size);");
            pw.println("\t\tlist.add(gridlet" + k + ");");
            pw.println("\n\t\tgridlet" + k + ".setUserID(0);");
        }
        pw.println("\n\t\treturn list;");
    }

    private void printMachines (final PrintWriter pw)
    {
        for (int i = 0; i < this.machines.getLength(); i++)
        {
            final var machine = (Element) this.machines.item(i);
            if (0 == machine.getElementsByTagName("master").getLength())
                this.printResource(machine, i, 1, pw);
        }
    }

    private void printClusters (final PrintWriter pw)
    {
        for (int j = 0, i = this.machines.getLength(); i < this.machines.getLength() + this.clusters.getLength(); i++, j++)
        {
            final var cluster = (Element) this.clusters.item(j);
            final int nodes = Integer.parseInt(cluster.getAttribute("nodes"));
            this.printResource(cluster, i, nodes, pw);
        }
    }

    private void printMaster (final Element machine, final int id, final PrintWriter pw)
    {
        if (1 != machine.getElementsByTagName("master").getLength())
            return;

        this.resources.put(Integer.parseInt(((Element) machine.getElementsByTagName("icon_id").item(0)).getAttribute("global")), machine.getAttribute("id"));

        final var slaves = ((Element) machine.getElementsByTagName("master").item(0)).getElementsByTagName("slave");
        pw.println("\n\t\t\tArrayList esc" + id + " = new ArrayList();");

        for (int i = 0; i < slaves.getLength(); i++)
            pw.println("\t\t\tesc" + id + ".add(" + this.resources.get(Integer.parseInt(((Element) slaves.item(i)).getAttribute("id"))) + ");");

        pw.println("\n\t\t\tMestre " + machine.getAttribute("id") + " = new Mestre(\"" + machine.getAttribute("id") + "_\", link, list, esc" + id + ", " + slaves.getLength() + ");");
        pw.println("\t\t\tRouter r_" + machine.getAttribute("id") + " = new RIPRouter( \"router_" + id + "\", trace_flag);");
        pw.println("\t\t\tr_" + machine.getAttribute("id") + ".attachHost( " + machine.getAttribute("id") + ", resSched); ");

        for (int i = 0; i < slaves.getLength(); i++)
            pw.println("\n\t\t\tr_" + machine.getAttribute("id") + ".attachHost( " + this.resources.get(Integer.parseInt(((Element) slaves.item(i)).getAttribute("id"))) + ", resSched); ");
    }

    private void printResource (
            final Element machine,
            final int index,
            final int nodes,
            final PrintWriter pw)
    {
        this.resources.put(Integer.parseInt(((Element) machine.getElementsByTagName("icon_id").item(0)).getAttribute("global")), machine.getAttribute("id"));

        pw.println("\n\t\t\tGridResource %s = createResource(\"%s_\",  baud_rate,  delay,  MTU, %d, (int)%s);".formatted(machine.getAttribute("id"), machine.getAttribute("id"), nodes, machine.getAttribute("power")));
        pw.println("\t\t\tRouter r_" + machine.getAttribute("id") + " = new RIPRouter( \"router_" + index + "\", trace_flag);");
        pw.println("\t\t\tr_" + machine.getAttribute("id") + ".attachHost( " + machine.getAttribute("id") + ", resSched); ");
    }
}