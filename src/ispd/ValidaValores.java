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
 * ValidaValores.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Aldo Ianelo Guerra;
 * Contributor(s):   Denison Menezes;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Classe para validação e controle de nomes e valores da interface gráfica do iSPD
 * @author Aldo
 */
public class ValidaValores {

    /**
     * Lista com os nomes dos icones
     */
    private static HashSet<String> listaNos = new HashSet<String>();

    private static List<String> palavrasReservadasJava =
    Arrays.asList(
     "abstract","assert","boolean","break","byte","case","catch","char","class","const"
     ,"continue","default","do","double","else","enum","extends","false","final","finally"
     ,"float","for","goto","if","implements","import","instanceof","int","interface"
     ,"long","native","new","null","package","private","protected","public","return"
     ,"short","static","strictfp","super","switch","synchronized","this","throw","throws"
     ,"transient","true","try","void","volatile","while"
    );

    public static void setListaNos(HashSet<String> listaNos) {
        ValidaValores.listaNos = listaNos;
    }
    /**
     * Adiciona um nome a lista com os nomes dos icones exitentes
     * @param temp valor a ser adicionado
     */
    public static void addNomeIcone(String temp) {
        listaNos.add(temp);
    }
    /**
     * Remove um nome a lista com os nomes dos icones exitentes
     * @param temp valor a ser removido
     */
    public static void removeNomeIcone(String temp) {
        listaNos.remove(temp);
    }
    /**
     * Inicia lista sem valores para os nomes dos icones
     */
    public static void removeTodosNomeIcone() {
        listaNos = new HashSet<String>();
    }
    /**
     * Verifica se já existe um icone com o nome informado
     * @param temp
     * @return true se não exister e false se existir
     */
    public static boolean NomeIconeNaoExiste(String temp) {
        if (!listaNos.contains(temp)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "There's already an icon named \'" + temp + "\'.\nPlease enter a different name.", "WARNING", JOptionPane.PLAIN_MESSAGE);
            return false;
        }
    }

    /**
     * Metodo para validar se o valor fornecido pelo usuário corresponde a um inteiro.
     */
    public static boolean validaInteiro(String temp) {
        if (temp.matches("\\d*")) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Please enter an integer number.", "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    /**
     * Metodo para validar se o nome fornecido pelo usuário corresponde a a um nome valido para um icone.
     */
    public static boolean validaNomeIcone(String temp) {
        if (temp.matches("[a-zA-Z](.)*")) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Please enter a valid name.\nThe name must begin with a letter.", "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    /**
     * Metodo para validar se o nome fornecido pelo usuário corresponde a um nome valido para uma classe Java.
     */
    public static boolean validaNomeClasse(String temp) {
        if (temp.matches("[a-zA-Z$_][a-zA-Z0-9$_]*")) {
            if(palavrasReservadasJava.contains(temp)){
                return false;
            }else{
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Metodo para validar se o valor fornecido pelo usuário corresponde a um double valido.
     */
    public static boolean validaDouble(String temp) {
        if (temp.matches("[0-9]+,[0-9]+")) {
            JOptionPane.showMessageDialog(null, "Please use \'.\' to specify floating point numbers.", "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (temp.matches("[0-9]+.[0-9]+") || temp.matches("")) {
            return true;
        } else if (temp.matches("[0-9]+")) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Please enter a floating point number.", "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
}
