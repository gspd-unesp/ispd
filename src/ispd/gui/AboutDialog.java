package ispd.gui;

import ispd.gui.auxiliar.HtmlPane;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.Frame;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

class AboutDialog extends JDialog {
    //language=HTML
    private static final String ABOUT_TEXT = """
            <html>
            <body>
            <p style="margin-left: 20px;">
                The <strong>"iSPD : iconic Simulator of Parallel and Distributed System"</strong><br/>
                was developed and supported by<br/>
            </p>
            <p style="margin-left: 50px;">
                Instituto de Biociências, Letras e Ciências Exatas,</b></br>
            <p style="margin-left: 50px;">
                UNESP - Univ Estadual Paulista, campus de São José do Rio Preto,</b></br>
            <p style="margin-left: 50px;">
                Departamento de Ciências de Computação e Estatística (DCCE),</b></br>
            <p style="margin-left: 50px;">
                Laboratório do Grupo de Sistemas Paralelos e Distribuídos (GSPD).</b></br>
            </body>
            </html>""";

    AboutDialog(final Frame parent, final boolean modal) {
        super(parent, modal);
        this.initComponents();
    }

    private void initComponents() {

        final var jTabbedPane1 = new JTabbedPane();
        final var jScrollPane2 = new JScrollPane();
        final var developers = new HtmlPane();
        final var license = new HtmlPane();

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("About iSPD");

        final var icon = new JLabel(new ImageIcon(
                this.getResource("/ispd/gui/imagens/Logo_iSPD_128.png")));

        jTabbedPane1.setTabPlacement(SwingConstants.BOTTOM);

        jTabbedPane1.addTab("About", new JLabel(AboutDialog.ABOUT_TEXT));

        developers.setText("");

        final JScrollPane jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(developers);
        jTabbedPane1.addTab("Developers", jScrollPane1);

        jScrollPane2.setViewportView(license);

        jTabbedPane1.addTab("License", jScrollPane2);

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(icon)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTabbedPane1,
                                        GroupLayout.DEFAULT_SIZE,
                                        694, Short.MAX_VALUE)
                                .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(icon)
                                                .addGap(0, 194,
                                                        Short.MAX_VALUE))
                                        .addComponent(jTabbedPane1))
                                .addContainerGap())
        );

        this.pack();

        try {
            license.setPage(this.getResource("html/License.html"));
            developers.setPage(this.getResource("html/Developers.html"));
        } catch (final IOException ex) {
            Logger.getLogger(AboutDialog.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private URL getResource(final String name) {
        return this.getClass().getResource(name);
    }
}