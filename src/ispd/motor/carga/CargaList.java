/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Descreve como gerar tarefas na forma por nó mestre
 *
 * @author denison_usuario
 */
public class CargaList extends GerarCarga {

    private int tipo;
    private List<GerarCarga> configuracaoNo;

    public CargaList(List configuracaoNo, int tipo) {
        this.tipo = tipo;
        this.configuracaoNo = configuracaoNo;
    }

    public static GerarCarga newGerarCarga(String entrada) {
        CargaList newObj = null;
        String[] linhas = entrada.split("\n");
        List<CargaForNode> nos = new ArrayList<CargaForNode>();
        for (int i = 0; i < linhas.length; i++) {
            CargaForNode newNo = (CargaForNode) CargaForNode.newGerarCarga(linhas[i]);
            if (newNo != null) {
                nos.add(newNo);
            }
        }
        if (nos.size() > 0) {
            newObj = new CargaList(nos, GerarCarga.FORNODE);
        }
        return newObj;
    }

    public List<GerarCarga> getList() {
        return configuracaoNo;
    }

    @Override
    public List<Tarefa> toTarefaList(RedeDeFilas rdf) {
        List<Tarefa> tarefas = new ArrayList<Tarefa>();
        int inicio = 0;
        if (tipo == GerarCarga.FORNODE) {
            for (GerarCarga item : this.configuracaoNo) {
                CargaForNode carga = (CargaForNode) item;
                carga.setInicioIdentificadorTarefa(inicio);
                inicio += carga.getNumeroTarefas();
                tarefas.addAll(carga.toTarefaList(rdf));
            }
        }
        return tarefas;
    }

    @Override
    public String toString() {
        StringBuilder saida = new StringBuilder();
        for (GerarCarga cargaTaskNode : configuracaoNo) {
            saida.append(cargaTaskNode.toString()).append("\n");
        }
        return saida.toString();
    }

    @Override
    public int getTipo() {
        return tipo;
    }
}
