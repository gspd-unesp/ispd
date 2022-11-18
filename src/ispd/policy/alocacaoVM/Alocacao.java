package ispd.policy.alocacaoVM;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import java.util.List;

public abstract class Alocacao {
    protected List<CS_Processamento> maquinasFisicas;
    protected List<List> infoMaquinas;
    protected List<CS_VirtualMac> maquinasVirtuais;
    protected VMM VMM;
    protected List<CS_VirtualMac> VMsRejeitadas;
    protected List<List> caminhoMaquina;

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

    public abstract void migrarVM();

    public void addVM(final CS_VirtualMac vm) {
        this.maquinasVirtuais.add(vm);
    }

    public List<CS_Processamento> getMaquinasFisicas() {
        return this.maquinasFisicas;
    }

    public void setMaquinasFisicas(final List<CS_Processamento> maquinasFisicas) {
        this.maquinasFisicas = maquinasFisicas;
    }

    public void addMaquinaFisica(final CS_Processamento maq) {
        this.maquinasFisicas.add(maq);
    }

    public List<CS_VirtualMac> getMaquinasVirtuais() {
        return this.maquinasVirtuais;
    }

    public void setMaquinasVirtuais(final List<CS_VirtualMac> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
    }

    public VMM getVMM() {
        return this.VMM;
    }

    public void setVMM(final VMM hypervisor) {
        this.VMM = hypervisor;
    }

    public List<List> getCaminhoMaquinas() {
        return this.caminhoMaquina;
    }

    public void setCaminhoMaquinas(final List<List> caminhoMaquinas) {
        this.caminhoMaquina = caminhoMaquinas;
    }

    public List<CS_VirtualMac> getVMsRejeitadas() {
        return this.VMsRejeitadas;
    }


    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar
     * atualização dos dados dos escravos
     * Retornar null para escalonadores estáticos, nos dinâmicos o método
     * deve ser reescrito
     *
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar() {
        return null;
    }
}