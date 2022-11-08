package ispd.policy.escalonadorCloud;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface ManipularArquivosCloud {
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