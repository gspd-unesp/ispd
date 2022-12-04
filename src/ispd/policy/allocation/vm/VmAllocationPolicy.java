package ispd.policy.allocation.vm;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.allocation.AllocationPolicy;

import java.util.List;

public abstract class VmAllocationPolicy extends AllocationPolicy<VmMaster> {
    protected List<CS_VirtualMac> maquinasVirtuais = null;
    protected List<CS_VirtualMac> VMsRejeitadas = null;
    protected List<List> infoMaquinas = null;

    public abstract CS_VirtualMac escalonarVM();

    public void addVM(final CS_VirtualMac vm) {
        this.maquinasVirtuais.add(vm);
    }

    public List<CS_VirtualMac> getMaquinasVirtuais() {
        return this.maquinasVirtuais;
    }

    public List<CS_VirtualMac> getVMsRejeitadas() {
        return this.VMsRejeitadas;
    }
}
