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
 * Carregar.java
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
 * 16-Out-2014 : change the location of the iSPD base directory;
 *
 */
package ispd.escalonador;

import ispd.gui.LogExceptions;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Carrega as classes dos escalonadores dinamicamente
 *
 * @author denison
 *
 */
public class Carregar {

    public static final File DIRETORIO_ISPD = carregarDirISPD();
    private static final String CAMINHO_CLASSE = "ispd.externo.";
    private static final URLClassLoader loader = carregarLoader();
    private static final String DIRETORIO = ".";

    /**
     * Recebe o nome de um algoritmo de escalonamento e retorna uma nova
     * instancia de um objeto com este nome ou null caso não encontre ou ocorra
     * um erro.
     *
     * @param nome
     * @return Nova instancia do objeto Escalonador
     */
    public static Escalonador getNewEscalonador(String nome) {
        try {
            Class cl = loader.loadClass(CAMINHO_CLASSE + nome);
            Escalonador escalonador = (Escalonador) cl.newInstance();
            //Escalonador escalonador = (Escalonador) Class.forName("novoescalonador."+nome, true, loader).newInstance();
            return escalonador;
        } catch (Exception ex) {
            Logger.getLogger(Carregar.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Localiza arquivo do programa iSPD
     *
     * @return diretório que fica o iSPD
     */
    private static File carregarDirISPD() {
        File diretorio;
        try {
            diretorio = new File(LogExceptions.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            diretorio = new File(LogExceptions.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        }
        if (!diretorio.getName().endsWith(".jar")) {
            return new File(".");
        } else {
            return diretorio.getParentFile();
        }
    }

    private static URLClassLoader carregarLoader() {
        try {
            URL[] aux = new URL[1];
            aux[0] = DIRETORIO_ISPD.toURI().toURL();
            return URLClassLoader.newInstance(aux, Carregar.class.getClassLoader());
        } catch (MalformedURLException ex) {
            Logger.getLogger(Carregar.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new ExceptionInInitializerError("Can't create the Loader!");
    }
}
