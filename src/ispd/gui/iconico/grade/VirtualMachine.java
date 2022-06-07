/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.iconico.grade;

/**
 *
 * @author Diogo Tavares
 */
public class VirtualMachine {
    
    //lista de Atributos
    private String nome;
    private String proprietario;
    private String VMM;
    private int numProcessadores;
    private double memoriaAlocada;
    private double discoAlocado;
    private String OS;
    
    /**
     *
     * @param id
     * @param proprietario
     * @param VMM
     * @param poderComputacional
     * @param memoriaAlocada
     * @param discoAlocado
     * @param OS
     
     */
    public VirtualMachine(String id, String proprietario, String VMM, int poderComputacional, double memoriaAlocada, double discoAlocado, String OS){
        this.nome = id;
        this.proprietario = proprietario;
        this.VMM = VMM;
        this.numProcessadores = poderComputacional;
        this.memoriaAlocada = memoriaAlocada;
        this.discoAlocado = discoAlocado;
        this.OS = OS;
        }

    public String getProprietario() {
        return proprietario;
    }

    public void setProprietario(String proprietario) {
        this.proprietario = proprietario;
    }

    public String getVMM() {
        return VMM;
    }

    public void setVMM(String VMM) {
        this.VMM = VMM;
    }

    public int getPoderComputacional() {
        return numProcessadores;
    }

    public void setPoderComputacional(int poderComputacional) {
        this.numProcessadores = poderComputacional;
    }

    public double getMemoriaAlocada() {
        return memoriaAlocada;
    }

    public void setMemoriaAlocada(double memoriaAlocada) {
        this.memoriaAlocada = memoriaAlocada;
    }

    public double getDiscoAlocado() {
        return discoAlocado;
    }

    public void setDiscoAlocado(double discoAlocado) {
        this.discoAlocado = discoAlocado;
    }

    public String getOS() {
        return OS;
    }

    public void setOS(String OS) {
        this.OS = OS;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
   
    
}
