package ispd.externo;

import java.util.ListIterator;
import ispd.escalonador.Escalonador;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.List;
import java.util.ArrayList;

public class NewScheduler extends Escalonador{

private Tarefa tarefaSelecionada = null;
private ListIterator<CS_Processamento> recursos;

public NewScheduler() {
    this.tarefas = new ArrayList<Tarefa>();
    this.escravos = new ArrayList<CS_Processamento>();
}

@Override
public void iniciar() {
    recursos = escravos.listIterator(0);
}

@Override
public Tarefa escalonarTarefa() {
  if (!tarefas.isEmpty()) {
      return tarefas.remove(0);
  }
  return null;
}

@Override
public CS_Processamento escalonarRecurso() {
  if(!escravos.isEmpty()){
      if (recursos.hasNext()) {
          return recursos.next();
      }else{
          recursos = escravos.listIterator(0);
          return recursos.next();
      }
  }
  return null;
}

@Override
public void escalonar() {
    tarefaSelecionada = escalonarTarefa();
    if(tarefaSelecionada != null){
        CentroServico rec = escalonarRecurso();
        tarefaSelecionada.setLocalProcessamento(rec);
        tarefaSelecionada.setCaminho(escalonarRota(rec));
        mestre.enviarTarefa(tarefaSelecionada);
    }
}

@Override
public List<CentroServico> escalonarRota(CentroServico destino) {
    int index = escravos.indexOf(destino);
    return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
}

}