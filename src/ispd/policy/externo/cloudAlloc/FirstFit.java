/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.policy.externo.cloudAlloc;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.alocacaoVM.Alocacao;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class FirstFit extends Alocacao {
    private boolean fit;
    private int maqIndex;

    public FirstFit() {
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new ArrayList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();
    }

    @Override
    public void iniciar() {
        this.fit = true;
        this.maqIndex = 0;

        if (!this.maquinasFisicas.isEmpty() && !this.maquinasVirtuais.isEmpty()) {
            this.escalonar();
        }
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return this.maquinasVirtuais.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        return this.maquinasFisicas.get(this.fit ? 0 : this.maqIndex);
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.maquinasFisicas.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoMaquina.get(index));
    }

    @Override
    public void escalonar() {
        while (!(this.maquinasVirtuais.isEmpty())) {
            var slaveCount = this.maquinasFisicas.size();
            final var auxVM = this.escalonarVM();

            do {
                if (slaveCount == 0) {
                    auxVM.setStatus(CS_VirtualMac.REJEITADA);
                    this.VMsRejeitadas.add(auxVM);
                    this.maqIndex = 0;
                    slaveCount--;
                    continue;
                }

                final var auxMaq = this.escalonarRecurso();
                this.maqIndex++;

                if (auxMaq instanceof CS_VMM) {

                    System.out.printf(
                            "%s é um VMM, a VM será redirecionada\n",
                            auxMaq.getId());
                    auxVM.setCaminho(this.escalonarRota(auxMaq));
                    System.out.printf("%s enviada para %s\n",
                            auxVM.getId(), auxMaq.getId());
                    this.VMM.enviarVM(auxVM);
                    System.out.println(
                            "---------------------------------------");
                    break;
                } else {
                    final var maq = (CS_MaquinaCloud) auxMaq;
                    final double memoriaMaq =
                            maq.getMemoriaDisponivel();
                    final double memoriaNecessaria =
                            auxVM.getMemoriaDisponivel();
                    final double discoMaq = maq.getDiscoDisponivel();
                    final double discoNecessario =
                            auxVM.getDiscoDisponivel();
                    final int maqProc =
                            maq.getProcessadoresDisponiveis();
                    final int procVM =
                            auxVM.getProcessadoresDisponiveis();

                    if ((memoriaNecessaria <= memoriaMaq && discoNecessario <= discoMaq && procVM <= maqProc)) {
                        maq.setMemoriaDisponivel(memoriaMaq - memoriaNecessaria);
                        maq.setDiscoDisponivel(discoMaq - discoNecessario);
                        maq.setProcessadoresDisponiveis(maqProc - procVM);
                        auxVM.setMaquinaHospedeira((CS_MaquinaCloud) auxMaq);
                        auxVM.setCaminho(this.escalonarRota(auxMaq));
                        this.VMM.enviarVM(auxVM);
                        this.maqIndex = 0;
                        this.fit = true;
                        break;
                    } else {
                        slaveCount--;
                        this.fit = false;
                    }
                }
            } while (slaveCount >= 0);
        }
    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
