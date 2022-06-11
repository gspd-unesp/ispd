/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.escalonadorCloud;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author denison_usuario
 */
public interface ManipularArquivosCloud
{
    ArrayList<String> listar();

    File getDiretorio();

    boolean escrever(String nome, String codigo);

    String compilar(String nome);

    String ler(String escalonador);

    boolean remover(String escalonador);

    boolean importarEscalonadorJava(File arquivo);
    
    List listarAdicionados();
    
    List listarRemovidos();
}