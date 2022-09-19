package ispd.alocacaoVM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface ManipularArquivosAlloc {
    ArrayList<String> listar();

    File getDiretorio();

    boolean escrever(String nome, String codigo);

    String compilar(String nome);

    String ler(String alocador);

    boolean remover(String alocador);

    boolean importarAlocadoresJava(File arquivo);

    List listarAdicionados();

    List listarRemovidos();
}