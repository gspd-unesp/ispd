package ispd.policy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// TODO: Rename methods; document
public interface PolicyManager {
    String NO_POLICY = "---";

    ArrayList<String> listar();

    /**
     * @return Basic template for writing an allocation policy's source code
     */
    String getPolicyTemplate(String policyName);

    File directory();

    boolean escrever(String nome, String codigo);

    String compilar(String nome);

    String ler(String policy);

    boolean remover(String policy);

    boolean importJavaPolicy(File arquivo);

    List listarAdicionados();

    List listarRemovidos();
}
