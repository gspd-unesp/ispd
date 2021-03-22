/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.CS_Processamento;
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
public class RedeDeFilas {
    /**
     * Todos os mestres existentes no sistema incluindo o front-node dos
     * clusters
     */
    private List<CS_Processamento> mestres;
    /**
     * Todas as máquinas que não são mestres
     */
    private List<CS_Maquina> maquinas;
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
     * Armazena listas com a arquitetura de todo o sistema modelado, utilizado
     * para buscas das métricas e pelo motor de simulação
     *
     * @param mestres
     * @param maquinas
     * @param links
     * @param internets
     */
    public RedeDeFilas(List<CS_Processamento> mestres, List<CS_Maquina> maquinas, List<CS_Comunicacao> links, List<CS_Internet> internets) {
        this.mestres = mestres;
        this.maquinas = maquinas;
        this.links = links;
        this.internets = internets;
    }

    public List<CS_Internet> getInternets() {
        return internets;
    }

    public void setInternets(List<CS_Internet> internets) {
        this.internets = internets;
    }

    public List<CS_Comunicacao> getLinks() {
        return links;
    }

    public void setLinks(List<CS_Comunicacao> links) {
        this.links = links;
    }

    public List<CS_Maquina> getMaquinas() {
        return maquinas;
    }

    public void setMaquinas(List<CS_Maquina> maquinas) {
        this.maquinas = maquinas;
    }

    public List<CS_Processamento> getMestres() {
        return mestres;
    }

    public void setMestres(List<CS_Processamento> mestres) {
        this.mestres = mestres;
    }

    public void setUsuarios(List<String> usuarios) {
        this.usuarios = usuarios;
    }

    public List<String> getUsuarios() {
        return this.usuarios;
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
    public void setFalhas(int min, int max, double scale, double shape, double recMin, double recMax, boolean recuperavel) {
        Random rd = new Random();
        int numFalhas = min + rd.nextInt(max - min);
        List<Double> falhas = new ArrayList<Double>();
        for (int i = 0; i < numFalhas; i++) {
            falhas.add(scale * Math.pow(-Math.log(1 - rd.nextDouble()), 1 / shape));
        }
        Collections.sort(falhas);
        while(!falhas.isEmpty()){
            int next = rd.nextInt(maquinas.size());
            System.out.println("Falha "+falhas.get(0)+" no "+maquinas.get(next).getId());
            maquinas.get(next).addFalha(falhas.remove(0), recMin, recuperavel);
        }
    }
}
