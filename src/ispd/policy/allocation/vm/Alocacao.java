package ispd.policy.allocation.vm;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import java.util.List;

public abstract class Alocacao implements VmAllocationPolicy {
    protected List<CS_Processamento> maquinasFisicas = null;
    protected List<List> infoMaquinas = null;
    protected List<CS_VirtualMac> maquinasVirtuais = null;
    protected VmMaster vmMaster = null;
    protected List<CS_VirtualMac> VMsRejeitadas = null;
    protected List<List> caminhoMaquina = null;

    /**
     * Begin the allocation.
     */
    public abstract void iniciar();

    /**
     * Select the vm selection criterion.
     *
     * @return selected vm.
     */
    public abstract CS_VirtualMac escalonarVM();

    /**
     * Select the resource selection criterion.
     *
     * @return selected resource.
     */
    public abstract CS_Processamento escalonarRecurso();

    /**
     * Implement route to selected resource.
     *
     * @param destino resource to be routed to.
     * @return List with service centers that make up a route to the resource.
     */
    public abstract List<CentroServico> escalonarRota(CentroServico destino);

    /**
     * Actually do the scheduling.
     */
    public abstract void escalonar();

    public void addVM(final CS_VirtualMac vm) {
        this.maquinasVirtuais.add(vm);
    }

    public List<CS_Processamento> getMaquinasFisicas() {
        return this.maquinasFisicas;
    }

    public void addMaquinaFisica(final CS_Processamento maq) {
        this.maquinasFisicas.add(maq);
    }

    public List<CS_VirtualMac> getMaquinasVirtuais() {
        return this.maquinasVirtuais;
    }

    public VmMaster getVMM() {
        return this.vmMaster;
    }

    public void setVMM(final VmMaster hypervisor) {
        this.vmMaster = hypervisor;
    }

    public void setCaminhoMaquinas(final List<List> caminhoMaquinas) {
        this.caminhoMaquina = caminhoMaquinas;
    }

    public List<CS_VirtualMac> getVMsRejeitadas() {
        return this.VMsRejeitadas;
    }

    public Double getTempoAtualizar() {
        return null;
    }
}