/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.alocacaoVM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public interface ManipularArquivosAlloc {

    public ArrayList<String> listar();

    public File getDiretorio();

    public boolean escrever(String nome, String codigo);

    public String compilar(String nome);

    public String ler(String alocador);

    public boolean remover(String alocador);

    public boolean importarAlocadoresJava(File arquivo);
    
    public List listarAdicionados();
    
    public List listarRemovidos();
    
}
