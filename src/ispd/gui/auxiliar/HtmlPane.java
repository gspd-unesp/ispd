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
 * HtmlPane.java
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
     * @param titulo Titulo da janela que será exibida
     * @param page pagina html que será exibida
     * @throws java.io.IOException para URL nulo ou inválido
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
     * @param url endereço que será aberto
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
        buscar = buscar.replaceAll("&#225;","á");
        buscar = buscar.replaceAll("&#227;","ã");
        buscar = buscar.replaceAll("&#231;","ç");
        buscar = buscar.replaceAll("&#233;","é");
        buscar = buscar.replaceAll("&#245;","õ");
        //Removendo espaço em branco
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
            Logger.getLogger(HtmlPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        int posicao = text.lastIndexOf(buscar);
        if (posicao > 20) { // posicao > 0
            try {
                Rectangle caretCoords = this.modelToView(posicao);
                caretCoords.y += this.getParent().getHeight() - caretCoords.height;
                this.scrollRectToVisible(caretCoords);
            } catch (BadLocationException ex) {
                Logger.getLogger(HtmlPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.setCaretPosition(0);
        }
    }
}
