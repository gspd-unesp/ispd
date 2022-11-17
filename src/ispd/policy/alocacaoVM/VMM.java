package ispd.policy.alocacaoVM;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.PolicyMaster;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
 */
public interface VMM extends PolicyMaster {
    void enviarVM(CS_VirtualMac vm);
}