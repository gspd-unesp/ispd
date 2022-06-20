package ispd.gui.auxiliar;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.awt.Desktop;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlPane extends JEditorPane implements HyperlinkListener {
    private static final String TAG_END = ">";
    private static final String CLOSE_TAG_START = "</";
    private static final int SET_CARET_POSITION_UPPER_BOUND = 20;

    public HtmlPane() {
        this.setContentType("text/html");
        this.setEditable(false);
        this.addHyperlinkListener(this);
    }

    @Override
    public void hyperlinkUpdate(final HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }

        final var desc = event.getURL().getRef();
        if (desc != null && !desc.isEmpty()) {
            this.jumpToAnchor(desc);
            return;
        }

        if (event instanceof HTMLFrameHyperlinkEvent ev) {
            final var doc = (HTMLDocument) this.getDocument();
            doc.processHTMLFrameHyperlinkEvent(ev);
            return;
        }

        HtmlPane.openDefaultBrowser(event.getURL());
    }

    private void jumpToAnchor(final String anchorDesc) {
        final int pos = this.getStringPositionInDoc(
                this.searchStringFromAnchorDesc(anchorDesc));

        if (pos <= HtmlPane.SET_CARET_POSITION_UPPER_BOUND) {
            this.setCaretPosition(0);
            return;
        }

        try {
            final var caret = (Rectangle) this.modelToView2D(pos);
            caret.y += this.getParent().getHeight() - caret.height;
            this.scrollRectToVisible(caret);
        } catch (final BadLocationException ex) {
            Logger.getLogger(HtmlPane.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Opens a link with user's default browser, if supported
     *
     * @param url address for browser to open
     */
    public static void openDefaultBrowser(final URL url) {
        final var desktop =
                Desktop.isDesktopSupported()
                        ? Desktop.getDesktop() : null;

        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
            return;
        }

        try {
            desktop.browse(url.toURI());
        } catch (final IOException | URISyntaxException ignored) {
        }
    }

    private int getStringPositionInDoc(final String s) {
        return HtmlPane.getDocText(this.getDocument()).lastIndexOf(s);
    }

    private String searchStringFromAnchorDesc(final String desc) {
        final var html = this.getText();
        final int tag = html.indexOf(("id=\"%s\"").formatted(desc));
        final int start = 1 + html.indexOf(HtmlPane.TAG_END, tag);
        final int end = html.indexOf(HtmlPane.CLOSE_TAG_START, start);

        return html.substring(start, end)
                .replaceAll("\n", "")
                .replaceAll("&#225;", "á")
                .replaceAll("&#227;", "ã")
                .replaceAll("&#231;", "ç")
                .replaceAll("&#233;", "é")
                .replaceAll("&#245;", "õ")
                .trim();
    }

    private static String getDocText(final Document doc) {
        try {
            return doc.getText(0, doc.getLength());
        } catch (final BadLocationException ex) {
            Logger.getLogger(HtmlPane.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return "";
    }
}