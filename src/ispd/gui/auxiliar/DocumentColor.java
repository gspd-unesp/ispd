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
 * DocumentColor.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Estilo de texto para visualizar código Java
 * Após ser instanciado deve chamar o método:
 *      configurarTextComponent(JTextComponent)
 * Com esse método o componente de texto será configurado
 * @author Denison
 */
public class DocumentColor extends DefaultStyledDocument implements CaretListener {

    private final Element rootElement;
    private final String[] keywords = {
        "\\bfor\\b",
        "\\bif\\b",
        "\\belse\\b",
        "\\bwhile\\b",
        "\\bint\\b",
        "\\bboolean\\b",
        "\\bnew\\b",
        "\\bdouble\\b",
        "\\bpublic\\b",
        "\\bprivate\\b",
        "\\bprotected\\b",
        "\\breturn\\b",
        "\\bthis\\b",
        "\\bstatic\\b",
        "\\bvoid\\b",
        "\\btry\\b",
        "\\bcatch\\b",
        "\\bbreak\\b",
        "\\bthrow\\b",
        "\\bpackage\\b",
        "\\bimport\\b",
        "\\bclass\\b",
        "\\bextends\\b",
        "\\btrue\\b",
        "\\bfalse\\b"
    };
    private final String[] keyStrings = {
        "\"(.*)\"",
        "(\'.\')",
        "(\'..\')"};
    private final String keyNumbers = "\\b[0-9]+\\b";
    private final MutableAttributeSet style;
    private final Color defaultStyle = Color.black;
    private final Color commentStyle = Color.lightGray;
    private final Color keyStyle = Color.blue;
    private final Color numberStyle = new Color(0, 150, 0);
    private final Color stringStyle = new Color(250, 125, 0);
    private final Pattern singleCommentDelim = Pattern.compile("//");
    private final Pattern multiCommentDelimStart = Pattern.compile("/\\*");
    private final Pattern multiCommentDelimEnd = Pattern.compile("\\*/");
    private final Font font;
    //Desenha linhas
    private final JTextArea BarraNumLinhas;
    private final JLabel BarraPosCursor;
    private Integer numeroLinhas;
    //Popup para auto completar
    private JPopupMenu popup;
    private JList ListAutoCompletar;
    private String[] autoCompletar = {
        "boolean", "break", "case", "class", "double", "else", "false", "final", "float", "for", "if", "instanceof", "int", "new", "null", "private", "protected", "public", "return", "static", "String", "super", "switch", "System", "this", "true", "try", "void", "while",
        "escravos", "filaEscravo", "tarefas", "metricaUsuarios", "mestre", "caminhoEscravo", "adicionarTarefa(tarefa)", "getTempoAtualizar()", "resultadoAtualizar(mensagem)", "addTarefaConcluida(tarefa)",
        "enviarTarefa(Tarefa tarefa)", "processarTarefa(Tarefa tarefa)", "executarEscalonamento()", "enviarMensagem(tarefa, escravo, tipo)", "atualizar(CS_Processamento escravo)", "criarCopia(Tarefa get)",
        "Mensagens.CANCELAR", "Mensagens.PARAR", "Mensagens.DEVOLVER", "Mensagens.DEVOLVER_COM_PREEMPCAO", "Mensagens.ATUALIZAR"};

    public DocumentColor() {
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        rootElement = getDefaultRootElement();
        style = new SimpleAttributeSet();
        //Define estilo de fonte
        font = new Font(Font.MONOSPACED, Font.BOLD, 12);
        StyleConstants.setFontFamily(style, Font.MONOSPACED);
        StyleConstants.setBold(style, true);
        StyleConstants.setFontSize(style, 12);
        //Desenha linhas
        numeroLinhas = 1;
        BarraNumLinhas = new JTextArea();
        BarraNumLinhas.setDisabledTextColor(Color.BLACK);
        BarraNumLinhas.setEnabled(false);
        BarraNumLinhas.setMargin(new Insets(-2, 0, 0, 0));
        BarraNumLinhas.setColumns(1);
        BarraNumLinhas.setFont(font);
        BarraNumLinhas.setText("1");
        BarraNumLinhas.setBackground(Color.lightGray);
        //Indicar posição do curso(teclado)
        BarraPosCursor = new JLabel("Linha: 0 | Coluna: 0 ");
        BarraPosCursor.setBackground(Color.lightGray);
        BarraPosCursor.setHorizontalAlignment(SwingConstants.RIGHT);
        //auto completar
        criarPopup();
    }

    public Font getFont() {
        return font;
    }

    public Component getLinhas() {
        return BarraNumLinhas;
    }

    public Component getCursor() {
        return BarraPosCursor;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str.length() == 1) {
            if (str.equals("\n")) {
                String text = getText(0, offset);
                int nivel = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '{') {
                        nivel++;
                    } else if (text.charAt(i) == '}') {
                        nivel--;
                    }
                }
                StringBuilder sb = new StringBuilder("\n");
                for (int i = 0; i < nivel; i++) {
                    sb.append("    ");
                }
                super.insertString(offset, sb.toString(), style);
                inserirLinhas(1);
            } else if (str.equals("\t")) {
                super.insertString(offset, "    ", style);
            } else if (str.equals(" ")) {
                super.insertString(offset, str, style);
                processChangedLines(offset, str.length());
            } else if (str.equals("}")) {
                //Busca remover espaços adicionados
                String text = getText(0, offset);
                super.insertString(offset, str, style);
                if (text.substring(offset - 4).equals("    ")) {
                    super.remove(offset - 4, 4);
                }
                processChangedLines(offset, str.length());
            } else {
                super.insertString(offset, str, style);
                processChangedLines(offset, str.length());
            }
        } else {
            //Substituir tabs
            str = str.replaceAll("\t", "    ");
            //insere modificações
            Pattern p = Pattern.compile("\n");
            Matcher m = p.matcher(str);
            int total = 0;
            while (m.find()) {
                total++;
            }
            inserirLinhas(total);
            super.insertString(offset, str, attr);
            processChangedLines(offset, str.length());
        }
    }

    @Override
    public void remove(int offset, int length) throws BadLocationException {
        Pattern p = Pattern.compile("\n");
        Matcher m = p.matcher(getText(offset, length));
        int total = 0;
        while (m.find()) {
            total++;
        }
        if (total > 0) {
            numeroLinhas -= total;
            for (int i = 0; i < total; i++) {
                int fim = BarraNumLinhas.getText().length();
                int pos = BarraNumLinhas.getText().lastIndexOf('\n', fim);
                BarraNumLinhas.getDocument().remove(pos, fim - pos);
            }
            if (numeroLinhas.toString().length() != BarraNumLinhas.getColumns()) {
                BarraNumLinhas.setColumns(numeroLinhas.toString().length());
            }
        }
        super.remove(offset, length);
        processChangedLines(offset, length);
    }

    public void processChangedLines(int offset, int length) throws BadLocationException {
        // Normal Text
        String text = getText(0, getLength());
        StyleConstants.setForeground(style, defaultStyle);
        StyleConstants.setBold(style, false);
        //StyleConstants.setItalic(style, false);
        setCharacterAttributes(0, getLength(), style, true);

        // Keywords
        StyleConstants.setBold(style, true);
        StyleConstants.setForeground(style, keyStyle);
        for (String keyword : keywords) {
            Pattern p = Pattern.compile(keyword);
            Matcher m = p.matcher(text);

            while (m.find()) {
                setCharacterAttributes(m.start(), m.end() - m.start(), style, true);
            }
        }

        //numbers
        StyleConstants.setForeground(style, numberStyle);
        {
            Pattern p = Pattern.compile(keyNumbers);
            Matcher m = p.matcher(text);
            while (m.find()) {
                setCharacterAttributes(m.start(), m.end() - m.start(), style, true);
            }
        }

        StyleConstants.setForeground(style, stringStyle);
        for (String keyword : keyStrings) {
            Pattern p = Pattern.compile(keyword);
            Matcher m = p.matcher(text);

            while (m.find()) {
                setCharacterAttributes(m.start(), m.end() - m.start(), style, true);
            }
        }

        // Comments
        StyleConstants.setForeground(style, commentStyle);
        Matcher mlcStart = multiCommentDelimStart.matcher(text);
        Matcher mlcEnd = multiCommentDelimEnd.matcher(text);
        while (mlcStart.find()) {
            if (mlcEnd.find(mlcStart.end())) {
                setCharacterAttributes(mlcStart.start(), (mlcEnd.end() - mlcStart.start()), style, true);
            } else {
                setCharacterAttributes(mlcStart.start(), getLength(), style, true);
            }
        }

        Matcher slc = singleCommentDelim.matcher(text);

        while (slc.find()) {
            int line = rootElement.getElementIndex(slc.start());
            int endOffset = rootElement.getElement(line).getEndOffset() - 1;
            setCharacterAttributes(slc.start(), (endOffset - slc.start()), style, true);
        }
    }

    private void inserirLinhas(int total) throws BadLocationException {
        Document doc = BarraNumLinhas.getDocument();
        for (int i = 0; i < total; i++) {
            numeroLinhas++;
            doc.insertString(doc.getLength(), "\n" + numeroLinhas, null);
        }
        if (numeroLinhas.toString().length() != BarraNumLinhas.getColumns()) {
            BarraNumLinhas.setColumns(numeroLinhas.toString().length());
        }
    }

    @Override
    public void caretUpdate(CaretEvent ce) {
        //System.out.println("Mover");
        int inicio = ce.getDot();
        int fim = ce.getMark();
        JTextComponent jTexto = (JTextComponent) ce.getSource();
        String textoSelecionado = "";

        if (inicio == fim) {// no selection
            try {
                Rectangle caretCoords = jTexto.modelToView(inicio);
                BarraPosCursor.setText("Linha: " + ((caretCoords.y - 4) / 15 + 1) + " | Coluna: " + (caretCoords.x - 6) / 7 + " ");
            } catch (Exception ex) {
                //Logger.getLogger(DocumentColor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (inicio > fim) {
                inicio = ce.getMark();
                fim = ce.getDot();
            }
            if(fim-inicio > 1 && fim-inicio < 50) {
                try {
                    textoSelecionado = "\\b" + this.getText(inicio, fim - inicio) + "\\b";
                    processChangedLines(0, 0);
                    //Marcar texto igual ao texto selecionado
                    StyleConstants.setForeground(style, Color.BLACK);
                    StyleConstants.setBackground(style, Color.YELLOW);
                    Pattern p = Pattern.compile(textoSelecionado);
                    Matcher m = p.matcher(jTexto.getText());
                    while (m.find()) {
                        setCharacterAttributes(m.start(), m.end() - m.start(), style, true);
                    }
                    StyleConstants.setBackground(style, Color.WHITE);
                } catch (Exception ex) {
                    StyleConstants.setBackground(style, Color.WHITE);
                }
            }
            BarraPosCursor.setText("selection from: " + inicio + " to " + fim + " ");
        }
    }

    public void close() {
        numeroLinhas = 1;
        BarraNumLinhas.setText("1");
        BarraPosCursor.setText("Linha: 0 | Coluna: 0 ");
    }

    private void criarPopup() {
        popup = new JPopupMenu();
        ListAutoCompletar = new JList(autoCompletar);
        ListAutoCompletar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListAutoCompletar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    inserirAutoCompletar();
                }
            }
        });

        ListAutoCompletar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    inserirAutoCompletar();
                }
            }
        });

        JScrollPane popupBarra = new JScrollPane();
        popupBarra.setBorder(null);
        popupBarra.setViewportView(ListAutoCompletar);
        popup.add(popupBarra);
    }

    private void inserirAutoCompletar() {
        try {
            JTextComponent jTextPane = (JTextComponent) popup.getInvoker();
            int dot = jTextPane.getCaret().getDot();
            String inserir = ListAutoCompletar.getSelectedValue().toString();
            if (dot > 0) {
                String text = getText(dot - 2, 2);
                if (text.charAt(0) == ' ' && text.charAt(1) == inserir.charAt(0)) {
                    dot--;
                    remove(dot, 1);
                }
            }
            insertString(dot, inserir, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(DocumentColor.class.getName()).log(Level.SEVERE, null, ex);
        }
        popup.setVisible(false);
    }

    public void configurarTextComponent(final JTextComponent jTextPane) {
        jTextPane.setDocument(this);
        jTextPane.addCaretListener(this);
        jTextPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), "completar");
        jTextPane.getActionMap().put("completar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    int dot = jTextPane.getCaret().getDot();
                    Rectangle caretCoords = jTextPane.modelToView(dot);
                    popup.show(jTextPane, caretCoords.x, caretCoords.y);
                    int index = 0;
                    if (dot > 0) {
                        String text = getText(dot - 1, 1);
                        for (String string : autoCompletar) {
                            if (string.startsWith(text)) {
                                break;
                            } else {
                                index++;
                            }
                        }
                    }
                    ListAutoCompletar.setSelectedIndex(index);
                    ListAutoCompletar.repaint();
                    ListAutoCompletar.requestFocus();
                } catch (BadLocationException ex) {
                    Logger.getLogger(DocumentColor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }
}
