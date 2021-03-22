/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

/**
 * Classe de inicialização do iSPD.
 * Indetifica se deve executar comando a partir do termila ou carrega interface gráfica
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
            } catch (Exception ex) {
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