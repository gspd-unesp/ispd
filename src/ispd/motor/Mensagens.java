/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * Mensagens.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.motor;

import ispd.motor.filas.Mensagem;

/**
 *
 * @author denison
 */
public interface Mensagens {

    public static final int CANCELAR = 1;
    public static final int PARAR = 2;
    public static final int DEVOLVER = 3;
    public static final int DEVOLVER_COM_PREEMPCAO = 4;
    public static final int ATUALIZAR = 5;
    public static final int RESULTADO_ATUALIZAR = 6;
    public static final int FALHAR = 7;

    public void atenderCancelamento(Simulacao simulacao, Mensagem mensagem);
    public void atenderParada(Simulacao simulacao, Mensagem mensagem);
    public void atenderDevolucao(Simulacao simulacao, Mensagem mensagem);
    public void atenderDevolucaoPreemptiva(Simulacao simulacao, Mensagem mensagem);
    public void atenderAtualizacao(Simulacao simulacao, Mensagem mensagem);
    public void atenderRetornoAtualizacao(Simulacao simulacao, Mensagem mensagem);
    public void atenderFalha(Simulacao simulacao, Mensagem mensagem);
}