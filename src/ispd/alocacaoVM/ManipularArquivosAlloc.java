/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.alocacaoVM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author denison_usuario
 */
public interface ManipularArquivosAlloc
{
    ArrayList<String> listar ();

    File getDiretorio ();

    boolean escrever (String nome, String codigo);

    String compilar (String nome);

    String ler (String alocador);

    boolean remover (String alocador);

    boolean importarAlocadoresJava (File arquivo);

    List listarAdicionados ();

    List listarRemovidos ();
}