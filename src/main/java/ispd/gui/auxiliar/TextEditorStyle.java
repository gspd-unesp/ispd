package ispd.gui.auxiliar;

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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text style to visualize Java code.
 * After instantiation, the method configurarTextComponent(JTextComponent)
 * must be invoked, which will configure the component appropriately.
 */
public class TextEditorStyle extends DefaultStyledDocument implements CaretListener {
    private static final char NEW_LINE = '\n';
    private static final char OPEN_BRACKET = '{';
    private static final char CLOSE_BRACKET = '}';
    private static final String TAB_AS_SPACES = "    ";
    private static final char SPACE = ' ';
    private static final String AUTOCOMPLETE_ACTION_KEY = "completar";
    private static final String[] STRING_MATCHERS = {
            "\"(.*)\"",
            "('.')",
            "('..')"
    };
    private static final String NUMBER_MATCHER = "\\b\\d+\\b";
    private static final String[] AUTOCOMPLETE_STRINGS = {
            "boolean", "break", "case", "class", "double", "else",
            "false", "final", "float", "for", "if", "instanceof", "int",
            "new", "null", "private", "protected", "public", "return",
            "static", "String", "super", "switch", "System",
            "this", "true", "try", "void", "while",
            "escravos", "filaEscravo", "tarefas", "metricaUsuarios",
            "mestre", "caminhoEscravo", "adicionarTarefa(tarefa)",
            "getTempoAtualizar()", "resultadoAtualizar(mensagem)",
            "addTarefaConcluida(tarefa)", "enviarTarefa(Tarefa tarefa)",
            "processarTarefa(Tarefa tarefa)", "executarEscalonamento()",
            "enviarMensagem(tarefa, escravo, tipo)",
            "atualizar(CS_Processamento escravo)", "criarCopia(Tarefa get)",
            "Mensagens.CANCELAR", "Mensagens.PARAR", "Mensagens.DEVOLVER",
            "Mensagens.DEVOLVER_COM_PREEMPCAO", "Mensagens.ATUALIZAR"
    };
    private final Element rootElement = this.getDefaultRootElement();
    private final String[] keywords = { // TODO: make loop
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
    private final MutableAttributeSet style = new SimpleAttributeSet();
    private final Color defaultStyle = Color.black;
    private final Color commentStyle = Color.lightGray;
    private final Color keyStyle = Color.blue;
    private final Color numberStyle = new Color(0, 150, 0);
    private final Color stringStyle = new Color(250, 125, 0);
    private final Pattern singleCommentDelim = Pattern.compile("//");
    private final Pattern multiCommentDelimStart = Pattern.compile("/\\*");
    private final Pattern multiCommentDelimEnd = Pattern.compile("\\*/");
    private final Font font = new Font(Font.MONOSPACED, Font.BOLD, 12);
    private final JTextArea lineCountBar = this.makeLineCountBar();
    private final JLabel barAfterCursor = TextEditorStyle.makeBarAfterCursor();
    private final JList<String> autocompleteList = this.makeAutoCompleteList();
    private final JPopupMenu autocompletePopup = this.makeAutocompletePopup();
    private Integer lineCount = 1;

    public TextEditorStyle() {
        this.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        this.setStyleFromConstants();
    }

    private void setStyleFromConstants() {
        StyleConstants.setFontFamily(this.style, Font.MONOSPACED);
        StyleConstants.setBold(this.style, true);
        StyleConstants.setFontSize(this.style, 12);
    }

    private static JLabel makeBarAfterCursor() {
        final JLabel bac = new JLabel("Linha: 0 | Coluna: 0 ");
        bac.setBackground(Color.lightGray);
        bac.setHorizontalAlignment(SwingConstants.RIGHT);
        return bac;
    }

    private static int countMatches(final String input) {
        final var pa = Pattern.compile("\n");
        final var ma = pa.matcher(input);

        int total = 0;
        while (ma.find()) {
            total++;
        }
        return total;
    }

    private JPopupMenu makeAutocompletePopup() {
        final JScrollPane popupBar = new JScrollPane();
        popupBar.setBorder(null);
        popupBar.setViewportView(this.autocompleteList);
        final JPopupMenu acp = new JPopupMenu();
        acp.add(popupBar);
        return acp;
    }

    private JList<String> makeAutoCompleteList() {
        final var acl = new JList<>(TextEditorStyle.AUTOCOMPLETE_STRINGS);
        acl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        acl.addMouseListener(new AutoCompleteMouseAdapter());
        acl.addKeyListener(new AutoCompleteKeyAdapter());
        return acl;
    }

    private void insertAutocomplete() {
        try {
            final int dot =
                    ((JTextComponent) this.autocompletePopup.getInvoker()).getCaret().getDot();
            final var autocomplete = this.autocompleteList.getSelectedValue();
            final int insertPos = this.ignoreSpace(dot, autocomplete);
            this.insertString(insertPos, autocomplete, null);
        } catch (final BadLocationException ex) {
            Logger.getLogger(TextEditorStyle.class.getName()).log(Level.SEVERE,
                    null, ex);
        }

        this.autocompletePopup.setVisible(false);
    }

    private int ignoreSpace(final int dot, final String autocomplete) throws BadLocationException {
        if (dot <= 0) {
            return dot;
        }

        if (!this.getText(dot - 2, 2).equals(
                " " + autocomplete.charAt(0))) {
            return dot;
        }

        this.remove(dot - 1, 1);
        return dot - 1;
    }

    @Override
    public void remove(final int offset, final int length) throws BadLocationException {
        final int total = TextEditorStyle.countMatches(this.getText(offset,
                length));

        if (total > 0) {
            this.updateLineCountBar(total);
            this.updateColumns();
        }

        this.removeAndProcessChanges(offset, length);
    }

    @Override
    public void insertString(
            final int offset,
            final String text,
            final AttributeSet attr) throws BadLocationException {
        if (text.length() != 1) {
            this.insertPastedText(offset, text, attr);
            this.processChangedLines();
            return;
        }

        switch (text) {
            case "\n" -> {
                final var tabs = TextEditorStyle.TAB_AS_SPACES.repeat(
                        this.calculateScopeDepthUntil(offset));
                super.insertString(
                        offset, "\n" + tabs, this.style);
                this.insertLines(1);
            }
            case "\t" -> {
                super.insertString(
                        offset, TextEditorStyle.TAB_AS_SPACES, this.style);
            }
            case "}" -> {
                this.removeAdditionalSpaces(offset, text);
                this.processChangedLines();
            }
            default -> {
                super.insertString(offset, text, this.style);
                this.processChangedLines();
            }
        }
    }

    private void insertPastedText(final int offset, final String str,
                                  final AttributeSet attr) throws BadLocationException {
        final var spaces = str.replaceAll("\t", TextEditorStyle.TAB_AS_SPACES);
        final int total = TextEditorStyle.countMatches(spaces);
        this.insertLines(total);
        super.insertString(offset, spaces, attr);
    }

    private int calculateScopeDepthUntil(final int offset) throws BadLocationException {
        final var textBefore = this.getText(0, offset);
        int depth = 0;

        // TODO: { total - } total

        for (int i = 0; i < textBefore.length(); i++) {
            if (textBefore.charAt(i) == TextEditorStyle.OPEN_BRACKET) {
                depth++;
            } else if (textBefore.charAt(i) == TextEditorStyle.CLOSE_BRACKET) {
                depth--;
            }
        }

        return depth;
    }

    private void insertLines(final int total) throws BadLocationException {
        this.updateText(total);
        this.updateColumns();
    }

    private void removeAdditionalSpaces(final int offset, final String str) throws BadLocationException {
        final String text = this.getText(0, offset);
        super.insertString(offset, str, this.style);
        if (text.substring(offset - 4).equals(TextEditorStyle.TAB_AS_SPACES)) {
            super.remove(offset - 4, 4);
        }
    }

    private void updateLineCountBar(final int total) throws BadLocationException {
        this.lineCount -= total;
        for (int i = 0; i < total; i++) {
            final int end = this.lineCountBar.getText().length();
            final int pos =
                    this.lineCountBar.getText().lastIndexOf(TextEditorStyle.NEW_LINE, end);
            this.lineCountBar.getDocument().remove(pos, end - pos);
        }
    }

    private void updateColumns() {
        if (this.lineCount.toString().length() != this.lineCountBar.getColumns()) {
            this.lineCountBar.setColumns(this.lineCount.toString().length());
        }
    }

    private void removeAndProcessChanges(final int offset, final int length) throws BadLocationException {
        super.remove(offset, length);
        this.processChangedLines();
    }

    private void updateText(final int total) throws BadLocationException {
        final Document doc = this.lineCountBar.getDocument();
        for (int i = 0; i < total; i++) {
            this.lineCount++;
            doc.insertString(doc.getLength(), "\n" + this.lineCount, null);
        }
    }

    private JTextArea makeLineCountBar() {
        final var lcb = new JTextArea();
        lcb.setDisabledTextColor(Color.BLACK);
        lcb.setEnabled(false);
        lcb.setMargin(new Insets(-2, 0, 0, 0));
        lcb.setColumns(1);
        lcb.setFont(this.font);
        lcb.setText("1");
        lcb.setBackground(Color.lightGray);
        return lcb;
    }

    public Font getFont() {
        return this.font;
    }

    public Component getCursor() {
        return this.barAfterCursor;
    }

    public Component getLinhas() {
        return this.lineCountBar;
    }

    @Override
    public void caretUpdate(final CaretEvent ce) {
        final int start = ce.getDot();
        final int end = ce.getMark();
        final var text = (JTextComponent) ce.getSource();

        if (start == end) {
            this.caretUpdateWithoutSelection(text, start);
            return;
        }

        if (start < end) {
            this.caretUpdateWithSelection(text.getText(), start, end);
            return;
        }

        this.caretUpdateWithSelection(text.getText(), end, start);
    }

    private void caretUpdateWithoutSelection(
            final JTextComponent text,
            final int start) {
        try {
            final var caretCoords = (Rectangle) text.modelToView2D(start);
            this.barAfterCursor.setText("Linha: %d | Coluna: %d ".formatted(
                    (caretCoords.y - 4) / 15 + 1, (caretCoords.x - 6) / 7));
        } catch (final BadLocationException ignored) {
        }
    }

    private void caretUpdateWithSelection(
            final String text, final int start, final int end) {
        final int length = end - start;
        if (length > 1 && length < 50) {
            try {
                final var selected =
                        "\\b%s\\b".formatted(this.getText(start, length));
                this.processChangedLines();
                this.markOnTextAllEqualToSelected(text, selected);
            } catch (final Exception ex) {
                StyleConstants.setBackground(this.style, Color.WHITE);
            }
        }
        this.barAfterCursor.setText("selection from: %d to %d ".formatted(start, end));
    }

    private void processChangedLines() throws BadLocationException {
        // Normal Text
        final String text = this.getText(0, this.getLength());
        StyleConstants.setForeground(this.style, this.defaultStyle);
        StyleConstants.setBold(this.style, false);
        //StyleConstants.setItalic(style, false);
        this.setCharacterAttributes(0, this.getLength(), this.style, true);

        // Keywords
        StyleConstants.setBold(this.style, true);
        StyleConstants.setForeground(this.style, this.keyStyle);
        for (final String keyword : this.keywords) {
            final Pattern p = Pattern.compile(keyword);
            final Matcher m = p.matcher(text);

            while (m.find()) {
                this.setCharacterAttributes(m.start(), m.end() - m.start(),
                        this.style,
                        true);
            }
        }

        //numbers
        StyleConstants.setForeground(this.style, this.numberStyle);
        {
            final Pattern p = Pattern.compile(TextEditorStyle.NUMBER_MATCHER);
            final Matcher m = p.matcher(text);
            while (m.find()) {
                this.setCharacterAttributes(m.start(), m.end() - m.start(),
                        this.style,
                        true);
            }
        }

        StyleConstants.setForeground(this.style, this.stringStyle);
        for (final String keyword : TextEditorStyle.STRING_MATCHERS) {
            final Pattern p = Pattern.compile(keyword);
            final Matcher m = p.matcher(text);

            while (m.find()) {
                this.setCharacterAttributes(m.start(), m.end() - m.start(),
                        this.style,
                        true);
            }
        }

        // Comments
        StyleConstants.setForeground(this.style, this.commentStyle);
        final Matcher mlcStart = this.multiCommentDelimStart.matcher(text);
        final Matcher mlcEnd = this.multiCommentDelimEnd.matcher(text);
        while (mlcStart.find()) {
            if (mlcEnd.find(mlcStart.end())) {
                this.setCharacterAttributes(mlcStart.start(),
                        (mlcEnd.end() - mlcStart.start()), this.style, true);
            } else {
                this.setCharacterAttributes(mlcStart.start(),
                        this.getLength(), this.style,
                        true);
            }
        }

        final Matcher slc = this.singleCommentDelim.matcher(text);

        while (slc.find()) {
            final int line = this.rootElement.getElementIndex(slc.start());
            final int endOffset =
                    this.rootElement.getElement(line).getEndOffset() - 1;
            this.setCharacterAttributes(slc.start(), (endOffset - slc.start()),
                    this.style, true);
        }
    }

    private void markOnTextAllEqualToSelected(
            final String text, final String selectedText) {
        StyleConstants.setForeground(this.style, Color.BLACK);
        StyleConstants.setBackground(this.style, Color.YELLOW);
        final Pattern p = Pattern.compile(selectedText);
        final Matcher m = p.matcher(text);
        while (m.find()) {
            this.setCharacterAttributes(
                    m.start(), m.end() - m.start(), this.style, true);
        }
        StyleConstants.setBackground(this.style, Color.WHITE);
    }

    public void close() {
        this.lineCount = 1;
        this.lineCountBar.setText("1");
        this.barAfterCursor.setText("Linha: 0 | Coluna: 0 ");
    }

    public void configurarTextComponent(final JTextComponent component) {
        component.setDocument(this);
        component.addCaretListener(this);
        component.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
                        InputEvent.CTRL_DOWN_MASK),
                TextEditorStyle.AUTOCOMPLETE_ACTION_KEY
        );
        component.getActionMap().put(
                TextEditorStyle.AUTOCOMPLETE_ACTION_KEY,
                new TextPaneAction(component)
        );
    }

    private void displayAutocomplete(
            final int dot,
            final Rectangle caretCoords,
            final Component component) throws BadLocationException {
        this.autocompletePopup.show(component, caretCoords.x, caretCoords.y);
        this.autocompleteList.setSelectedIndex(this.autocompleteListIndex(dot));
        this.autocompleteList.repaint();
        this.autocompleteList.requestFocus();
    }

    private int autocompleteListIndex(final int dotPosition) throws BadLocationException {
        if (dotPosition <= 0) {
            return 0;
        }

        final var text = this.getText(dotPosition - 1, 1);
        return (int) Arrays
                .stream(TextEditorStyle.AUTOCOMPLETE_STRINGS)
                .takeWhile(s -> !s.startsWith(text)).count();
    }

    private class AutoCompleteMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent me) {
            if (me.getClickCount() == 2) {
                TextEditorStyle.this.insertAutocomplete();
            }
        }
    }

    private class AutoCompleteKeyAdapter extends KeyAdapter {
        @Override
        public void keyReleased(final KeyEvent ke) {
            if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                TextEditorStyle.this.insertAutocomplete();
            }
        }
    }

    private class TextPaneAction extends AbstractAction {
        private final JTextComponent area;

        private TextPaneAction(final JTextComponent area) {
            this.area = area;
        }

        @Override
        public void actionPerformed(final ActionEvent ae) {
            try {
                final int dot = this.area.getCaret().getDot();
                final var caretPos = (Rectangle) this.area.modelToView2D(dot);
                TextEditorStyle.this.displayAutocomplete(dot, caretPos,
                        this.area);
            } catch (final BadLocationException ex) {
                Logger.getLogger(TextEditorStyle.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}