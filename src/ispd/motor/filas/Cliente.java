/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CentroServico;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public interface Cliente {
    public double getTamComunicacao();
    public double getTamProcessamento();
    public double getTimeCriacao();
    public CentroServico getOrigem();
    public List<CentroServico> getCaminho();
    public void setCaminho(List<CentroServico> caminho);
}
