package ispd.policy.allocation.vm;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.allocation.AllocationMaster;

public interface VmMaster extends AllocationMaster {
    void sendVm(CS_VirtualMac vm);
}
