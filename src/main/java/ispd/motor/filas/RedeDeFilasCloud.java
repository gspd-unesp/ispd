/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Possui listas de todos os icones presentes no modelo utilizado para buscas e
 * para o motor de simulação
 *
 * @author denison_usuario
 */
public class RedeDeFilasCloud extends RedeDeFilas{
    /**
     * Todos os mestres existentes no sistema incluindo o front-node dos
     * clusters
     */
    private List<CS_Processamento> mestres;
    /**
     * Todas as máquinas que não são mestres
     */
    private List<CS_MaquinaCloud> maquinasCloud;
    /**
     * Todas as conexões
     */
    private List<CS_Comunicacao> links;
    /**
     * Todos icones de internet do modelo
     */
    private List<CS_Internet> internets;
    /**
     * Mantem lista dos usuarios da rede de filas
     */
    private List<String> usuarios;
    /**
     * Mantem a lista de máquinas virtuais
     */
    private List<CS_VirtualMac> VMs;
    public int length;
    /**
     * Armazena listas com a arquitetura de todo o sistema modelado, utilizado
     * para buscas das métricas e pelo motor de simulação
     *
     * @param mestres
     * @param maquinas
     * @param vms
     * @param links
     * @param internets
     */
    public RedeDeFilasCloud(List<CS_Processamento> mestres, List<CS_MaquinaCloud> maquinas, List<CS_VirtualMac> vms, List<CS_Comunicacao> links, List<CS_Internet> internets) {
        super(mestres,null,links, internets);
        this.maquinasCloud = maquinas;
        this.VMs = vms;
    }

    public List<CS_MaquinaCloud> getMaquinasCloud() {
        return maquinasCloud;
    }

    public void setMaquinasCloud(List<CS_MaquinaCloud> maquinasCloud) {
        this.maquinasCloud = maquinasCloud;
    }
    
    public List<CS_VirtualMac> getVMs() {
        return VMs;
    }

    public void setVMs(List<CS_VirtualMac> VMs) {
        this.VMs = VMs;
    }
    
    
    
    /**
     * Cria falhas para ocorrer durante a simulação usando a distribuição de Weibull.
     * A distribuição de Weibull indica o momento que ocorre a falha, 
     * enquanto a uniforme indica o tempo de recuperação do recurso
     * @param min número mínimo de falhas que ocorrerão
     * @param max número máximo do falahas que ocorrerão
     * @param scale parâmetro de escala da distribuição de Weibull
     * @param shape parâmetro de forma da distribuição de Weibull
     * @param recMin tempo mínimo para recuperação do recurso que falhou
     * @param recMax tempo máximo para recuperação do recurso que falhou
     * @param recuperavel indica se a falha tem recuperação automática
     */
    @Override
    public void setFalhas(int min, int max, double scale, double shape, double recMin, double recMax, boolean recuperavel) {
        Random rd = new Random();
        int numFalhas = min + rd.nextInt(max - min);
        List<Double> falhas = new ArrayList<Double>();
        for (int i = 0; i < numFalhas; i++) {
            falhas.add(scale * Math.pow(-Math.log(1 - rd.nextDouble()), 1 / shape));
        }
        Collections.sort(falhas);
        while(!falhas.isEmpty()){
            int next = rd.nextInt(maquinasCloud.size());
            System.out.println("Falha "+falhas.get(0)+" no "+maquinasCloud.get(next).getId());
            maquinasCloud.get(next).addFalha(falhas.remove(0), recMin, recuperavel);
        }
    }

    public int length() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
