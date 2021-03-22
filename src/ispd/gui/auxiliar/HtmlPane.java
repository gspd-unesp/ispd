/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.auxiliar;

import ispd.gui.JResultados;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 *
 * @author denison
 */
public class HtmlPane extends JEditorPane implements HyperlinkListener {

    public HtmlPane() {
        this.setContentType("text/html");
        this.setEditable(false);
        this.addHyperlinkListener(this);
    }

    /**
     * Cria um e exibe JDialog contendo uma pagina de html
     *
     * @param pai janela que fez a chamada
     * @param page pagina html que será exibida
     */
    public static void newHTMLDialog(Window pai, String titulo, URL page) throws IOException {
        JDialog frame = new JDialog(pai, titulo, ModalityType.APPLICATION_MODAL);
        Container con = frame.getContentPane();
        JEditorPane jep = new HtmlPane();
        JScrollPane jsp = new JScrollPane(jep);
        jep.setPage(page);
        con.add(jsp);
        frame.setBounds(50, 50, 700, 500);
        frame.setLocationRelativeTo(pai);
        frame.setVisible(true);
    }

    /**
     * Abre um link com o navegador padrão
     *
     * @param uri endereço que será aberto
     */
    public static void openDefaultBrowser(URL url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent he) {
        if (HyperlinkEvent.EventType.ACTIVATED == he.getEventType()) {
            String desc = he.getURL().getRef();
            if (desc == null || desc.length() == 0) {
                HTMLDocument document = (HTMLDocument) this.getDocument();
                if (he instanceof HTMLFrameHyperlinkEvent) {
                    document.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) he);
                } else {
                    //abre nova pagina
                    HtmlPane.openDefaultBrowser(he.getURL());
                }
            } else {
                jumpToAnchor(desc);
            }
        }
    }

    private void jumpToAnchor(String desc) {
        //Busca referencia dentro do html
        HTMLDocument document = (HTMLDocument) this.getDocument();
        //Encontra identificador no html e retira o texto
        String html = this.getText();
        int inicio = html.indexOf("id=\"" + desc + "\"");
        inicio = html.indexOf(">", inicio) + 1;
        int fim = html.indexOf("</", inicio);
        String buscar = html.substring(inicio, fim);
        buscar = buscar.replaceAll("\n", "");
        inicio = 0;
        while (buscar.charAt(inicio) == ' ') {
            inicio++;
        }
        fim = buscar.length() - 1;
        while (buscar.charAt(fim) == ' ') {
            fim--;
        }
        buscar = buscar.substring(inicio, fim + 1);
        //Encontra texto na pagina
        String text = "";
        try {
            text = document.getText(0, document.getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(JResultados.class.getName()).log(Level.SEVERE, null, ex);
        }
        int posicao = text.lastIndexOf(buscar);
        if (posicao > 0) {
            try {
                Rectangle caretCoords = this.modelToView(posicao);
                caretCoords.y += this.getParent().getHeight() - caretCoords.height;
                this.scrollRectToVisible(caretCoords);
            } catch (BadLocationException ex) {
                Logger.getLogger(JResultados.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.setCaretPosition(0);
        }
    }
}
