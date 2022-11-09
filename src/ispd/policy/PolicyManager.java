package ispd.policy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface PolicyManager {
    ArrayList<String> listar();

    File getDiretorio();

    boolean escrever(String nome, String codigo);

    String compilar(String nome);

    String ler(String policy);

    boolean remover(String policy);

    boolean importJavaPolicy(File arquivo);

    List listarAdicionados();

    List listarRemovidos();
}
