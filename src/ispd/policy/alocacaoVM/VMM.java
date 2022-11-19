package ispd.policy.alocacaoVM;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.allocation.AllocationMaster;

public interface VMM extends AllocationMaster {
    void sendVm(CS_VirtualMac vm);
}
