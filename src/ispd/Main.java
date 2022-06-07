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
 * Main.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 09-Set-2014 : Version 2.0;
 * 16-Out-2014 : change the location of the iSPD base directory;
 *
 */
package ispd;

import ispd.gui.JPrincipal;
import ispd.gui.LogExceptions;
import ispd.gui.SplashWindow;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Classe de inicialização do iSPD. Indetifica se deve executar comando a partir
 * do termila ou carrega interface gráfica
 *
 * @author denison
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "US"));
        if (args.length > 0) {
            Terminal tel = new Terminal(args);
            tel.executar();
            System.exit(0);
        } else {
            ImageIcon image = new ImageIcon(Main.class.getResource("gui/imagens/Splash.gif"));
            SplashWindow window = new SplashWindow(image);
            window.setText("Copyright (c) 2010 - 2014 GSPD.  All rights reserved.");
            window.setVisible(true);
            //Exibir e armazenar erros durante execução:
            LogExceptions logExceptions = new LogExceptions(null);
            Thread.setDefaultUncaughtExceptionHandler(logExceptions);
            // cria os novos fluxos de saida para arquivo
            FileOutputStream fosErr = null;
            FileOutputStream fosOut = null;
            try {
                fosErr = new FileOutputStream("Erros/Erros_Simulador");
                fosOut = new FileOutputStream("Erros/Saida_Simulador");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            // define a impresso sobre os fluxos acima
            PrintStream psErr = new PrintStream(fosErr);
            PrintStream psOut = new PrintStream(fosOut);
            // redefine os fluxos na classe System
            //System.setErr(psErr);
            //System.setOut(psOut);
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            JPrincipal gui = new JPrincipal();
            gui.setLocationRelativeTo(null);
            logExceptions.setParentComponent(gui);
            window.dispose();
            gui.setVisible(true);
        }
    }
}
