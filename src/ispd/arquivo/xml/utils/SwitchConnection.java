package ispd.arquivo.xml.utils;

import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;

/**
 * Class with utility methods to interconnect service centers and switches.
 *
 * @see ispd.arquivo.xml.models.builders.QueueNetworkBuilder
 * @see ispd.arquivo.xml.models.builders.CloudQueueNetworkBuilder
 */
public class SwitchConnection {
    /**
     * Connect switch to master
     */
    public static void toMaster(
            final CS_Switch theSwitch, final CS_Mestre master) {
        master.addConexoesEntrada(theSwitch);
        master.addConexoesSaida(theSwitch);
        SwitchConnection.connectSwitchToServiceCenter(theSwitch, master);
    }

    private static void connectSwitchToServiceCenter(
            final CS_Switch theSwitch, final CentroServico serviceCenter) {
        theSwitch.addConexoesEntrada(serviceCenter);
        theSwitch.addConexoesSaida(serviceCenter);
    }

    /**
     * Connect switch to machine.
     */
    public static void toMachine(
            final CS_Switch theSwitch, final CS_Maquina machine) {
        machine.addConexoesSaida(theSwitch);
        machine.addConexoesEntrada(theSwitch);
        SwitchConnection.connectSwitchToServiceCenter(theSwitch, machine);
    }

    /**
     * Connect switch to cloud machine.
     */
    public static void toCloudMachine(
            final CS_Switch theSwitch, final CS_MaquinaCloud maq) {
        maq.addConexoesSaida(theSwitch);
        maq.addConexoesEntrada(theSwitch);
        theSwitch.addConexoesEntrada(maq);
        theSwitch.addConexoesSaida(maq);
    }

    /**
     * Connect switch to a vmm.
     */
    public static void toVirtualMachineMaster(
            final CS_Switch theSwitch, final CS_VMM vmm) {
        vmm.addConexoesEntrada(theSwitch);
        vmm.addConexoesSaida(theSwitch);
        theSwitch.addConexoesEntrada(vmm);
        theSwitch.addConexoesSaida(vmm);
    }
}