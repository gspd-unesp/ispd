package ispd.policy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface PolicyManager {
    String NO_POLICY = "---";

    ArrayList<String> listar();

    File directory();

    boolean escrever(String nome, String codigo);

    String compilar(String nome);

    String ler(String policy);

    boolean remover(String policy);

    boolean importJavaPolicy(File arquivo);

    List listarAdicionados();

    List listarRemovidos();
}
