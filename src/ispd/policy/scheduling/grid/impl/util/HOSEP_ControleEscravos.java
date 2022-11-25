package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.Tarefa;

import java.util.ArrayList;

//Classe para arnazenar o estado das máquinas no sistema
public class HOSEP_ControleEscravos extends SlaveControl {

    public HOSEP_ControleEscravos(final String Ident, final int ind,
                                  final ArrayList<Tarefa> F,
                                  final ArrayList<Tarefa> P) {
        super(Ident, ind, F, P);
    }

}
