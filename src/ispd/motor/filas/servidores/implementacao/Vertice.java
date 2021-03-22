/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

/**
 *
 * @author denison
 */
public interface Vertice {

    public void addConexoesEntrada(CS_Link conexao);

    public void addConexoesSaida(CS_Link conexao);
}
