package ispd.alocacaoVM;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import java.util.List;

public abstract class Alocacao {
    protected List<CS_Processamento> maquinasFisicas; //lista de "escravos"
    protected List<List> infoMaquinas; // lista de informações armazenada
    // sobre cada máquina física
    protected List<CS_VirtualMac> maquinasVirtuais; //lista de vms "tarefas"
    protected VMM VMM; //vmm responsável por implementar a política de alocação
    protected List<CS_VirtualMac> VMsRejeitadas;
    protected List<List> caminhoMaquina;

    //iniciar a alocação
    public abstract void iniciar();

    //selecionar o critério de seleção da vm
    public abstract CS_VirtualMac escalonarVM();

    //selecionar o critério de seleção do recurso
    public abstract CS_Processamento escalonarRecurso();

    //implementar a rota até o recurso selecionado
    public abstract List<CentroServico> escalonarRota(CentroServico destino);

    //realiza o escalonamento de fato
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

    public void setVMM(final CS_VMM hypervisor) {
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


