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
 * JPrincipal.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Created on 11/04/2011
 * 09-Set-2014 : Version 2.0;
 * 14-Out-2014 : adicionado configurações para simulação e resultados
 *
 */
package ispd.gui;

import DescreveSistema.DescreveSistema; // TODO: Remove this dependency
import ispd.arquivo.exportador.Exportador;
import ispd.arquivo.interpretador.gridsim.InterpretadorGridSim;
import ispd.arquivo.interpretador.simgrid.InterpretadorSimGrid;
import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.arquivo.xml.IconicoXML;
import ispd.gui.auxiliar.Corner;
import ispd.gui.auxiliar.FiltroDeArquivos;
import ispd.gui.auxiliar.HtmlPane;
import ispd.gui.auxiliar.Stalemate;
import ispd.gui.configuracao.JPanelConfigIcon;
import ispd.gui.configuracao.JPanelSimples;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.DesenhoGrade;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.iconico.grade.Machine;
import ispd.gui.iconico.grade.VirtualMachine;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileView;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author denison
 */
public class JPrincipal extends JFrame implements KeyListener
{
    public static final String[] ISPD_FILE_EXTENSIONS = { ".ims", ".imsx" };
    public static final String[] ALL_FILE_EXTENSIONS = { ".ims", ".imsx", ".wmsx" };
    public static final Locale LOCALE_EN_US = new Locale("en", "US");
    public static final Locale LOCALE_PT_BR = new Locale("pt", "BR");
    public static final String ISPD_LOGO_FILE_PATH = "imagens/Logo_iSPD_25.png";
    private final JSobre jAbout = new JSobre(this, true);
    private final ConfiguracaoISPD configure = new ConfiguracaoISPD();
    private final JFileChooser jFileChooser = new JFileChooser();
    private final GerenciarEscalonador jFrameManager = new GerenciarEscalonador();
    private final GerenciarAlocadores jFrameAllocManager = new GerenciarAlocadores();
    private final GerenciarEscalonadorCloud jFrameCloudManager = new GerenciarEscalonadorCloud();
    private final JPanelConfigIcon jPanelSettings = new JPanelConfigIcon();
    private final JPanelSimples jPanelSimple = new JPanelSimples();
    private final JScrollPane jScrollPaneDrawingArea = new JScrollPane();
    private final JScrollPane jScrollPaneSideBar = new JScrollPane();
    private final JScrollPane jScrollPaneNotificationBar = new JScrollPane();
    private final JTextArea jTextAreaNotification = new JTextArea();
    private final JToggleButton jToggleButtonCluster = new JToggleButton();
    private final JToggleButton jToggleButtonInternet = new JToggleButton();
    private final JToggleButton jToggleButtonMachine = new JToggleButton();
    private final JToggleButton jToggleButtonNetwork = new JToggleButton();
    private final JToggleButton[] iconButtons = new JToggleButton[] {
            this.jToggleButtonMachine,
            this.jToggleButtonNetwork,
            this.jToggleButtonCluster,
            this.jToggleButtonInternet,
    };
    private final JToolBar jToolBar = new JToolBar();
    private final JButton jButtonConfigVM = new JButton();
    private final JButton jButtonInjectFaults = new JButton();
    private final JButton jButtonSimulate = new JButton();
    private final JButton jButtonTasks = new JButton();
    private final JButton jButtonUsers = new JButton();
    private final JCheckBoxMenuItem jCheckBoxMenuConnectedItem = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem jCheckBoxMenuSchedulableItem = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem jCheckBoxMenuGridItem = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem jCheckBoxIndirectMenuItem = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem jCheckBoxRulerMenuItem = new JCheckBoxMenuItem();
    private final JMenu jMenuHelp = new JMenu();
    private final JMenu jMenuFile = new JMenu();
    private final JMenu jMenuEdit = new JMenu();
    private final JMenu jMenuShow = new JMenu();
    private final JMenu jMenuExport = new JMenu();
    private final JMenu jMenuTools = new JMenu();
    private final JMenu jMenuLanguage = new JMenu();
    private final JMenu jMenuImport = new JMenu();
    private final JMenuItem jMenuItemOpen = new JMenuItem();
    private final JMenuItem jMenuItemHelp = new JMenuItem();
    private final JMenuItem jMenuItemCopy = new JMenuItem();
    private final JMenuItem jMenuItemDelete = new JMenuItem();
    private final JMenuItem jMenuItemCompare = new JMenuItem();
    private final JMenuItem jMenuItemClose = new JMenuItem();
    private final JMenuItem jMenuItemGenerate = new JMenuItem();
    private final JMenuItem jMenuItemManage = new JMenuItem();
    private final JMenuItem jMenuItemEnglish = new JMenuItem();
    private final JMenuItem jMenuItemNew = new JMenuItem();
    private final JMenuItem jMenuItemSort = new JMenuItem();
    private final JMenuItem jMenuItemPaste = new JMenuItem();
    private final JMenuItem jMenuItemPortuguese = new JMenuItem();
    private final JMenuItem jMenuItemExit = new JMenuItem();
    private final JMenuItem jMenuItemSave = new JMenuItem();
    private final JMenuItem jMenuItemSaveAs = new JMenuItem();
    private final JMenuItem jMenuItemSimGrid = new JMenuItem();
    private final JMenuItem jMenuItemAbout = new JMenuItem();
    private final JMenuItem jMenuItemToGridSim = new JMenuItem();
    private final JMenuItem jMenuItemToJPG = new JMenuItem();
    private final JMenuItem jMenuItemToSimGrid = new JMenuItem();
    private final JMenuItem jMenuItemToTxt = new JMenuItem();
    private final JComponent[] interactables = new JComponent[] {
            this.jToggleButtonCluster,
            this.jToggleButtonInternet,
            this.jToggleButtonMachine,
            this.jToggleButtonNetwork,
            this.jButtonSimulate,
            this.jButtonInjectFaults,
            this.jButtonTasks,
            this.jButtonUsers,
            this.jButtonConfigVM,
            this.jMenuItemSave,
            this.jMenuItemSaveAs,
            this.jMenuItemClose,
            this.jMenuItemToJPG,
            this.jMenuItemToTxt,
            this.jMenuItemToSimGrid,
            this.jMenuItemToGridSim,
            this.jMenuItemCompare,
            this.jMenuItemSort,
            this.jMenuItemCopy,
            this.jMenuItemPaste,
            this.jMenuItemDelete,
            this.jCheckBoxIndirectMenuItem,
            this.jCheckBoxMenuConnectedItem,
            this.jCheckBoxMenuSchedulableItem,
    };
    private final JPanelSimples jPanelProperties = new JPanelSimples();
    private final JScrollPane jScrollPaneProperties = new JScrollPane();
    private final JMenuItem jMenuItemOpenResult = new JMenuItem();
    private final JMenuItem jMenuItemGridSim = new JMenuItem();
    private final JMenuItem jMenuItemPreferences = new JMenuItem();
    private final JMenuItem jMenuItemManageCloud = new JMenuItem();
    private final JMenuItem jMenuItemManageAllocation = new JMenuItem();
    private int modelType; //define se o modelo é GRID, IAAS ou PAAS;
    private ResourceBundle words = ResourceBundle.getBundle("ispd.idioma.Idioma", Locale.getDefault());
    private final FiltroDeArquivos fileFilter =
            new FiltroDeArquivos(__("Iconic Model of Simulation"), JPrincipal.ALL_FILE_EXTENSIONS, true);
    private boolean currentFileHasUnsavedChanges = false;
    private File openFile = null;
    private DesenhoGrade drawingArea = null;
    private HashSet<VirtualMachine> virtualMachines = null;

    public JPrincipal ()
    {
        this.initComponents();
        this.addKeyListeners();
        this.buildLayoutAndPack();
    }

    private static ImageIcon getIconForFileExtension (final File file)
    {
        final var ext = JPrincipal.getFileExtension(file);

        if (ext.equals("") || !isIspdFileExtension(ext))
            return null;

        final var imgURL = JPrincipal.class.getResource(ISPD_LOGO_FILE_PATH);

        if (imgURL == null)
            return null;

        return new ImageIcon(imgURL);
    }

    private static boolean isIspdFileExtension (final String ext)
    {
        return ext.equals("ims") || ext.equals("imsx");
    }

    private static String getFileExtension (final File f)
    {
        final var fileName = f.getName();
        int i = fileName.lastIndexOf('.');

        /* Must avoid 'extension only' files such as .gitignore
         * Also invalid are files that end with a '.'
         */
        if ((i <= 0) || (i >= (fileName.length() - 1)))
            return "";

        return fileName.substring(i + 1).toLowerCase();
    }

    private static JProgressBar newIndeterminateProgressBar ()
    {
        final var progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        return progressBar;
    }

    private static Thread threadForLoadingScreen (final JDialog window)
    {
        return new Thread(() -> {
            window.setSize(200, 100);
            window.add(
                    new JLabel("Carregando..."),
                    BorderLayout.CENTER
            );
            window.add(
                    JPrincipal.newIndeterminateProgressBar(),
                    BorderLayout.SOUTH
            );
            window.setVisible(true);
        });
    }

    private static URL getResourceOrThrow (final String resourcePath)
    {
        final var url = Optional.ofNullable(
                JPrincipal.class.getResource(resourcePath));

        return url.orElseThrow(
                () -> new MissingResourceException(
                        "Missing resource from " + resourcePath,
                        JPrincipal.class.getName(),
                        resourcePath
                )
        );
    }

    private static ImageIcon getImage (final String imagePath)
    {
        return new ImageIcon(JPrincipal.getResourceOrThrow(imagePath));
    }

    private static boolean isValidDirectory (final File dir)
    {
        return dir.isDirectory() && dir.exists();
    }

    private String __ (final String s)
    {
        return this.words.getString(s);
    }

    private void initComponents ()
    {
        this.initWindowProperties();

        this.initFileChooser();
        this.initNotificationArea();
        this.initToolBarAndButtons();
        this.initMenus();
        this.initPanels();
    }

    private void initMenus ()
    {
        this.initMenuBar();
        this.initMenuFile();
        this.initMenuEdit();
        this.initMenuShow();
        this.initMenuTools();
        this.initMenuHelp();
    }

    private void initPanels ()
    {
        this.jPanelSettings.setEscalonadores(this.jFrameManager.getEscalonadores());
        this.jPanelSettings.setEscalonadoresCloud(this.jFrameCloudManager.getEscalonadores());
        this.jPanelSettings.setAlocadores(this.jFrameAllocManager.getAlocadores());
        this.jPanelSimple.setjLabelTexto(__("No icon selected."));

        this.jScrollPaneSideBar.setBorder(BorderFactory.createTitledBorder("Settings"));
        this.jScrollPaneNotificationBar.setBorder(BorderFactory.createTitledBorder(__("Notifications")));
        this.jScrollPaneNotificationBar.setViewportView(this.jTextAreaNotification);

        this.jScrollPaneProperties.setBorder(BorderFactory.createTitledBorder(__("Properties")));
        this.jScrollPaneProperties.setViewportView(this.jPanelProperties);
    }

    private void initMenuHelp ()
    {
        this.jMenuHelp.setText(__("Help"));
        this.jMenuItemHelp.setIcon(JPrincipal.getImage("/ispd/gui/imagens/help-faq.png"));
        this.jMenuItemHelp.setText(__("Help"));
        this.jMenuItemHelp.setToolTipText(__("Help"));
        this.jMenuItemHelp.addActionListener(this::jMenuItemHelpActionPerformed);
        this.jMenuHelp.add(this.jMenuItemHelp);
        this.jMenuHelp.add(new Separator());
        this.jMenuItemAbout.setIcon(JPrincipal.getImage("/ispd/gui/imagens/help-about.png"));
        final var aboutProgramText = String.format("%s %s", __("About"), __("nomePrograma"));
        this.jMenuItemAbout.setText(aboutProgramText);
        this.jMenuItemAbout.setToolTipText(aboutProgramText);
        this.jMenuItemAbout.addActionListener(this::jMenuItemAboutActionPerformed);
        this.jMenuHelp.add(this.jMenuItemAbout);
    }

    private void initMenuTools ()
    {
        this.jMenuTools.setText(__("Tools"));
        this.jMenuTools.addActionListener(this::jMenuToolsActionPerformed);
        this.jMenuItemManage.setText(__("Manage Schedulers"));
        this.jMenuItemManage.addActionListener(this::jMenuItemManageActionPerformed);
        this.jMenuTools.add(this.jMenuItemManage);
        this.jMenuItemGenerate.setText(__("Generate Scheduler"));
        this.jMenuItemGenerate.addActionListener(this::jMenuItemGenerateActionPerformed);
        this.jMenuTools.add(this.jMenuItemGenerate);
        this.jMenuItemManageCloud.setText("Manage Cloud Schedulers");
        this.jMenuItemManageCloud.addActionListener(this::jMenuItemManageCloudActionPerformed);
        this.jMenuTools.add(this.jMenuItemManageCloud);
        this.jMenuItemManageAllocation.setText("Manage Allocation Policies");
        this.jMenuItemManageAllocation.addActionListener(this::jMenuItemManageAllocationActionPerformed);
        this.jMenuTools.add(this.jMenuItemManageAllocation);
    }

    private void initMenuShow ()
    {
        this.jMenuShow.setText(__("View"));
        this.jCheckBoxMenuConnectedItem.setText(__("Show Connected Nodes"));
        this.jCheckBoxMenuConnectedItem.setToolTipText(__("Displays in the settings area, the list of nodes connected for the selected icon"));
        this.jCheckBoxMenuConnectedItem.setEnabled(false);
        this.jCheckBoxMenuConnectedItem.addActionListener(this::jCheckBoxMenuItemConnectedActionPerformed);
        this.jMenuShow.add(this.jCheckBoxMenuConnectedItem);
        this.jCheckBoxIndirectMenuItem.setText(__("Show Indirectly Connected Nodes"));
        this.jCheckBoxIndirectMenuItem.setToolTipText(__("Displays in the settings area, the list of nodes connected through the internet icon, to the icon selected"));
        this.jCheckBoxIndirectMenuItem.setEnabled(false);
        this.jCheckBoxIndirectMenuItem.addActionListener(this::jCheckBoxMenuItemIndirectActionPerformed);
        this.jMenuShow.add(this.jCheckBoxIndirectMenuItem);
        this.jCheckBoxMenuSchedulableItem.setSelected(true);
        this.jCheckBoxMenuSchedulableItem.setText(__("Show Schedulable Nodes"));
        this.jCheckBoxMenuSchedulableItem.setToolTipText(__("Displays in the settings area, the list of nodes schedulable for the selected icon"));
        this.jCheckBoxMenuSchedulableItem.setEnabled(false);
        this.jCheckBoxMenuSchedulableItem.addActionListener(this::jCheckBoxMenuItemSchedulableActionPerformed);
        this.jMenuShow.add(this.jCheckBoxMenuSchedulableItem);
        this.jCheckBoxMenuGridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        this.jCheckBoxMenuGridItem.setSelected(true);
        this.jCheckBoxMenuGridItem.setText(__("Drawing grid"));
        this.jCheckBoxMenuGridItem.setToolTipText(__("Displays grid in the drawing area"));
        this.jCheckBoxMenuGridItem.addActionListener(this::jCheckBoxMenuItemGradeActionPerformed);
        this.jMenuShow.add(this.jCheckBoxMenuGridItem);
        this.jCheckBoxRulerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        this.jCheckBoxRulerMenuItem.setSelected(true);
        this.jCheckBoxRulerMenuItem.setText(__("Drawing rule"));
        this.jCheckBoxRulerMenuItem.setToolTipText(__("Displays rule in the drawing area"));
        this.jCheckBoxRulerMenuItem.addActionListener(this::jCheckBoxMenuItemRulerActionPerformed);
        this.jMenuShow.add(this.jCheckBoxRulerMenuItem);
    }

    private void initMenuEdit ()
    {
        this.jMenuEdit.setText(__("Edit"));
        this.jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemCopy.setIcon(JPrincipal.getImage("/ispd/gui/imagens/edit-copy.png"));
        this.jMenuItemCopy.setText(__("Copy"));
        this.jMenuItemCopy.setEnabled(false);
        this.jMenuItemCopy.addActionListener(this::jMenuItemCopyActionPerformed);
        this.jMenuEdit.add(this.jMenuItemCopy);
        this.jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemPaste.setIcon(JPrincipal.getImage("/ispd/gui/imagens/edit-paste.png"));
        this.jMenuItemPaste.setText(__("Paste"));
        this.jMenuItemPaste.setEnabled(false);
        this.jMenuItemPaste.addActionListener(this::jMenuItemPasteActionPerformed);
        this.jMenuEdit.add(this.jMenuItemPaste);
        this.jMenuItemDelete.setIcon(JPrincipal.getImage("/ispd/gui/imagens/edit-delete.png"));
        this.jMenuItemDelete.setText(__("Delete"));
        this.jMenuItemDelete.setEnabled(false);
        this.jMenuItemDelete.addActionListener(this::jMenuItemDeleteActionPerformed);
        this.jMenuEdit.add(this.jMenuItemDelete);
        this.jMenuItemCompare.setText(__("Match network settings"));
        this.jMenuItemCompare.setToolTipText(__("Matches the settings of icons of networks according to a selected icon"));
        this.jMenuItemCompare.setActionCommand(__("Match network settings"));
        this.jMenuItemCompare.setEnabled(false);
        this.jMenuItemCompare.addActionListener(this::jMenuItemCompareActionPerformed);
        this.jMenuEdit.add(this.jMenuItemCompare);
        this.jMenuItemSort.setText("Arrange icons");
        this.jMenuItemSort.setEnabled(false);
        this.jMenuItemSort.addActionListener(this::jMenuItemSortActionPerformed);
        this.jMenuEdit.add(this.jMenuItemSort);
        this.jMenuEdit.add(new Separator());
        this.jMenuItemPreferences.setText("Preferences");
        this.jMenuItemPreferences.addActionListener(this::jMenuItem1ActionPerformed);
        this.jMenuEdit.add(this.jMenuItemPreferences);
    }

    private void initMenuFile ()
    {
        this.jMenuFile.setText(__("File"));
        this.jMenuFile.addActionListener(this::jMenuFileActionPerformed);
        this.jMenuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemNew.setIcon(JPrincipal.getImage("/ispd/gui/imagens/insert-object_1.png"));
        this.jMenuItemNew.setText(__("New"));
        this.jMenuItemNew.setToolTipText(__("Starts a new model"));
        this.jMenuItemNew.addActionListener(this::jMenuItemNovoActionPerformed);
        this.jMenuFile.add(this.jMenuItemNew);
        this.jMenuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemOpen.setIcon(JPrincipal.getImage("/ispd/gui/imagens/document-open.png"));
        this.jMenuItemOpen.setText(__("Open"));
        this.jMenuItemOpen.setToolTipText(__("Opens an existing model"));
        this.jMenuItemOpen.addActionListener(this::jMenuItemOpenActionPerformed);
        this.jMenuFile.add(this.jMenuItemOpen);
        this.jMenuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemSave.setIcon(JPrincipal.getImage("/ispd/gui/imagens/document-save_1.png"));
        this.jMenuItemSave.setText(__("Save"));
        this.jMenuItemSave.setToolTipText(__("Save the open model"));
        this.jMenuItemSave.setEnabled(false);
        this.jMenuItemSave.addActionListener(this::jMenuItemSaveActionPerformed);
        this.jMenuFile.add(this.jMenuItemSave);
        this.jMenuItemSaveAs.setText(__("Save as..."));
        this.jMenuItemSaveAs.setEnabled(false);
        this.jMenuItemSaveAs.addActionListener(this::jMenuItemSaveAsActionPerformed);
        this.jMenuFile.add(this.jMenuItemSaveAs);
        this.jMenuItemOpenResult.setIcon(JPrincipal.getImage("/ispd/gui/imagens/document-open.png"));
        this.jMenuItemOpenResult.setText("Open Results");
        this.jMenuItemOpenResult.addActionListener(this::jMenuItemOpenResultActionPerformed);
        this.jMenuFile.add(this.jMenuItemOpenResult);
        this.jMenuImport.setIcon(JPrincipal.getImage("/ispd/gui/imagens/document-import.png"));
        this.jMenuImport.setText(__("Import"));
        this.jMenuItemSimGrid.setText(__("SimGrid model"));
        this.jMenuItemSimGrid.setToolTipText(__("Open model from the specification files of Simgrid"));
        this.jMenuItemSimGrid.addActionListener(this::jMenuItemSimGridActionPerformed);
        this.jMenuImport.add(this.jMenuItemSimGrid);
        this.jMenuItemGridSim.setText(__("GridSim model"));
        this.jMenuItemGridSim.setToolTipText(__("Open model from the specification files of GridSim"));
        this.jMenuItemGridSim.addActionListener(this::jMenuItemGridSimActionPerformed);
        this.jMenuImport.add(this.jMenuItemGridSim);
        this.jMenuFile.add(this.jMenuImport);
        this.jMenuExport.setIcon(JPrincipal.getImage("/ispd/gui/imagens/document-export.png"));
        this.jMenuExport.setText(__("Export"));
        this.jMenuItemToJPG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemToJPG.setText(__("to JPG"));
        this.jMenuItemToJPG.setToolTipText(__("Creates a jpg file with the model image"));
        this.jMenuItemToJPG.setEnabled(false);
        this.jMenuItemToJPG.addActionListener(this::jMenuItemToJPGActionPerformed);
        this.jMenuExport.add(this.jMenuItemToJPG);
        this.jMenuItemToTxt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemToTxt.setText(__("to TXT"));
        this.jMenuItemToTxt.setToolTipText(__("Creates a file in plain text with the model data according to the grammar of the iconic model"));
        this.jMenuItemToTxt.setEnabled(false);
        this.jMenuItemToTxt.addActionListener(this::jMenuItemToTxtActionPerformed);
        this.jMenuExport.add(this.jMenuItemToTxt);
        this.jMenuItemToSimGrid.setText("to SimGrid");
        this.jMenuItemToSimGrid.setEnabled(false);
        this.jMenuItemToSimGrid.addActionListener(this::jMenuItemToSimGridActionPerformed);
        this.jMenuExport.add(this.jMenuItemToSimGrid);
        this.jMenuItemToGridSim.setText("to GridSim");
        this.jMenuItemToGridSim.setEnabled(false);
        this.jMenuItemToGridSim.addActionListener(this::jMenuItemToGridSimActionPerformed);
        this.jMenuExport.add(this.jMenuItemToGridSim);
        this.jMenuFile.add(this.jMenuExport);
        this.jMenuFile.add(new Separator());
        this.jMenuLanguage.setText(__("Language"));
        this.jMenuItemEnglish.setText(__("English"));
        this.jMenuItemEnglish.addActionListener(this::jMenuItemEnglishActionPerformed);
        this.jMenuLanguage.add(this.jMenuItemEnglish);
        this.jMenuItemPortuguese.setText(__("Portuguese"));
        this.jMenuItemPortuguese.addActionListener(this::jMenuItemPortugueseActionPerformed);
        this.jMenuLanguage.add(this.jMenuItemPortuguese);
        this.jMenuFile.add(this.jMenuLanguage);
        this.jMenuFile.add(new Separator());
        this.jMenuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK));
        this.jMenuItemClose.setIcon(JPrincipal.getImage("/ispd/gui/imagens/document-close.png"));
        this.jMenuItemClose.setText(__("Close"));
        this.jMenuItemClose.setToolTipText(__("Closes the currently open model"));
        this.jMenuItemClose.setEnabled(false);
        this.jMenuItemClose.addActionListener(this::jMenuItemCloseActionPerformed);
        this.jMenuFile.add(this.jMenuItemClose);
        this.jMenuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        this.jMenuItemExit.setIcon(JPrincipal.getImage("/ispd/gui/imagens/window-close.png"));
        this.jMenuItemExit.setText(__("Exit"));
        this.jMenuItemExit.setToolTipText(__("Closes the program"));
        this.jMenuItemExit.addActionListener(this::jMenuItemExitActionPerformed);
        this.jMenuFile.add(this.jMenuItemExit);
    }

    private void initWindowProperties ()
    {
        this.setTitle(__("nomePrograma"));
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getResourceOrThrow(ISPD_LOGO_FILE_PATH)));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new IspdWindowAdapter(this));
    }

    private void initToolBarAndButtons ()
    {
        this.jToolBar.setFloatable(false);
        this.initButton(
                this.jToggleButtonMachine, "/ispd/gui/imagens/botao_no.gif",
                "Selects machine icon for add to the model", this::jToggleButtonMachineActionPerformed
        );
        this.initButton(
                this.jToggleButtonNetwork, "/ispd/gui/imagens/botao_rede.gif",
                "Selects network icon for add to the model", this::jToggleButtonNetworkActionPerformed
        );
        this.initButton(
                this.jToggleButtonCluster, "/ispd/gui/imagens/botao_cluster.gif",
                "Selects cluster icon for add to the model", this::jToggleButtonClusterActionPerformed
        );
        this.initButton(
                this.jToggleButtonInternet, "/ispd/gui/imagens/botao_internet.gif",
                "Selects internet icon for add to the model", this::jToggleButtonInternetActionPerformed
        );
        this.jToolBar.add(new JToolBar.Separator());
        this.initButton(
                this.jButtonTasks, "/ispd/gui/imagens/botao_tarefas.gif",
                "Selects insertion model of tasks", this::jButtonTaskActionPerformed
        );

        this.jButtonConfigVM.setIcon(JPrincipal.getImage("/ispd/gui/imagens/vm_icon.png"));
        this.jButtonConfigVM.setToolTipText("Add and remove the virtual machines"); // TODO: Add to resource bundle to make method consistent
        this.jButtonConfigVM.setEnabled(false);
        this.jButtonConfigVM.setFocusable(false);
        this.jButtonConfigVM.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonConfigVM.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonConfigVM.addActionListener(this::jButtonConfigVMActionPerformed);
        this.jToolBar.add(this.jButtonConfigVM);

        this.initButton(
                this.jButtonUsers, "/ispd/gui/imagens/system-users.png",
                "Add and remove users to the model", this::jButtonUsersActionPerformed
        );
        this.initButton(
                this.jButtonSimulate, "/ispd/gui/imagens/system-run.png",
                "Starts the simulation", this::jButtonSimulateActionPerformed
        );
        this.jButtonSimulate.setText(__("Simulate"));

        this.jButtonInjectFaults.setIcon(JPrincipal.getImage("/ispd/gui/imagens/vermelho.png"));
        this.jButtonInjectFaults.setToolTipText("Select the faults");
        this.jButtonInjectFaults.setEnabled(false);
        this.jButtonInjectFaults.setFocusable(false);
        this.jButtonInjectFaults.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonInjectFaults.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonInjectFaults.addActionListener(this::jButtonInjectFaultsActionPerformed);
        this.jToolBar.add(this.jButtonInjectFaults);

        this.jButtonInjectFaults.setText("Faults Injection");
    }

    private void initButton (
            final AbstractButton button, final String iconPath, final String toolTip, final ActionListener onClick)
    {
        button.setIcon(JPrincipal.getImage(iconPath));
        button.setToolTipText(__(toolTip));
        button.setEnabled(false);
        button.setFocusable(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.addActionListener(onClick);
        this.jToolBar.add(button);
    }

    private void initNotificationArea ()
    {
        this.jTextAreaNotification.setEditable(false);
        this.jTextAreaNotification.setColumns(20);
        this.jTextAreaNotification.setRows(5);
        this.jTextAreaNotification.setBorder(null);
    }

    private void buildLayoutAndPack ()
    {
        final var contentPane = this.getContentPane();
        final var layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(this.jScrollPaneProperties)
                                        .addComponent(this.jScrollPaneSideBar, GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.jScrollPaneNotificationBar)
                                        .addComponent(this.jToolBar, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                                        .addComponent(this.jScrollPaneDrawingArea))
                                .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(this.jScrollPaneSideBar, GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(this.jScrollPaneProperties, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(this.jToolBar, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(this.jScrollPaneDrawingArea)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(this.jScrollPaneNotificationBar, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        this.pack();
    }

    private void initMenuBar ()
    {
        final var menus = new JMenu[] {
                this.jMenuFile,
                this.jMenuEdit,
                this.jMenuShow,
                this.jMenuTools,
                this.jMenuHelp,
        };

        final var menuBar = new JMenuBar();

        for (var menu : menus)
            menuBar.add(menu);

        this.setJMenuBar(menuBar);
    }

    private void addKeyListeners ()
    {
        final var components = new Component[] {
                this,
                this.jScrollPaneDrawingArea,
                this.jScrollPaneSideBar,
                this.jScrollPaneNotificationBar,
                this.jScrollPaneProperties,
                this.jTextAreaNotification,
                this.jToolBar,
                this.jToggleButtonCluster,
                this.jToggleButtonInternet,
                this.jToggleButtonMachine,
                this.jToggleButtonNetwork,
                this.jButtonTasks,
                this.jButtonUsers,
                this.jButtonSimulate,
                this.jPanelSimple,
                this.jPanelSettings,
                this.jPanelProperties,
        };

        for (var c : components)
            c.addKeyListener(this);
    }

    private void initFileChooser ()
    {
        this.jFileChooser.setAcceptAllFileFilterUsed(false);
        this.jFileChooser.setFileFilter(this.fileFilter);
        this.jFileChooser.setFileView(new IspdFileView());
        this.jFileChooser.setSelectedFile(this.configure.getLastFile());
    }

    private void jMenuItemManageActionPerformed (final ActionEvent evt)
    {
        this.showSubWindow(this.jFrameManager);
    }

    private void jMenuItemAboutActionPerformed (final ActionEvent evt)
    {
        this.showSubWindow(this.jAbout);
    }

    private void jMenuItemEnglishActionPerformed (final ActionEvent evt)
    {
        this.setLanguage(LOCALE_EN_US);
    }

    private void jMenuItemPortugueseActionPerformed (final ActionEvent evt)
    {
        this.setLanguage(LOCALE_PT_BR);
    }

    private void setLanguage (final Locale locale)
    {
        this.words = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);
        this.initTexts();
        if (this.drawingArea != null)
        {
            this.drawingArea.setIdioma(this.words);
        }
    }

    private void iconButtonOnClick (
            final JToggleButton clickedButton, final int drawingIndex, final String notificationText)
    {
        this.deselectOtherButtons(clickedButton);
        this.updateDrawingAreaButton(clickedButton, drawingIndex, notificationText);
    }

    private void updateDrawingAreaButton (
            final JToggleButton clickedButton, final int drawingIndex, final String notificationText)
    {
        if (!clickedButton.isSelected())
        {
            this.drawingArea.setIconeSelecionado(null);
            return;
        }

        this.drawingArea.setIconeSelecionado(drawingIndex);
        this.appendNotificacao(__(notificationText));
    }

    private void deselectOtherButtons (final JToggleButton button)
    {
        final boolean originalStatus = button.isSelected();
        this.deselectAllIconButtons();
        button.setSelected(originalStatus);
    }

    private void deselectAllIconButtons ()
    {
        for (var b : this.iconButtons)
            b.setSelected(false);
    }

    private void jToggleButtonMachineActionPerformed (final ActionEvent evt)
    {
        this.iconButtonOnClick(this.jToggleButtonMachine, DesenhoGrade.MACHINE, "Machine button selected.");
    }

    private void jToggleButtonNetworkActionPerformed (final ActionEvent evt)
    {
        this.iconButtonOnClick(this.jToggleButtonNetwork, DesenhoGrade.NETWORK, "Network button selected.");
    }

    private void jToggleButtonClusterActionPerformed (final ActionEvent evt)
    {
        this.iconButtonOnClick(this.jToggleButtonCluster, DesenhoGrade.CLUSTER, "Cluster button selected.");
    }

    private void jToggleButtonInternetActionPerformed (final ActionEvent evt)
    {
        this.iconButtonOnClick(this.jToggleButtonInternet, DesenhoGrade.INTERNET, "Internet button selected.");
    }

    private void jButtonTaskActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        final var loadConfigWindow = new SelecionaCargas(
                this,
                true,
                this.drawingArea.getUsuarios().toArray(),
                this.drawingArea.getNosEscalonadores().toArray(),
                this.drawingArea.getCargasConfiguracao(),
                this.words
        );

        this.showSubWindow(loadConfigWindow);
        this.updateDrawingLoad(loadConfigWindow);
        this.modificar();
    }

    private void updateDrawingLoad (final SelecionaCargas loadConfigWindow)
    {
        this.drawingArea.setCargasConfiguracao(loadConfigWindow.getCargasConfiguracao());
        this.drawingArea.setUsuarios(loadConfigWindow.getUsuarios());
    }

    private void jMenuItem1ActionPerformed (final ActionEvent evt)
    {
        this.showSubWindow(
                new JPreferences(
                        this,
                        true,
                        this.configure
                )
        );
    }

    private void jButtonSimulateActionPerformed (final ActionEvent evt)
    {
        final var simulationWindow = new JSimulacao(
                this,
                true,
                this.drawingArea.getGrade(),
                this.drawingArea.toString(),
                this.words,
                this.modelType
        );

        simulationWindow.iniciarSimulacao();
        this.showSubWindow(simulationWindow);
        this.appendNotificacao(__("Simulate button added."));
    }

    private void jMenuItemNovoActionPerformed (final ActionEvent evt)
    {
        if (this.currentFileHasUnsavedChanges)
            this.saveChanges();

        //janela de escolha de qual tipo de serviço irá ser modelado
        final var classPickWindow = new EscolherClasse(this, true);
        this.showSubWindow(classPickWindow);

        this.drawingArea = new DesenhoGrade(1500, 1500);
        this.updateGuiWithOpenFile("New model opened", null);
        this.modificar();
        this.onModelTypeChange(classPickWindow);
    }

    private void onModelTypeChange (final EscolherClasse classPickWindow)
    {
        this.modelType = classPickWindow.getEscolha();
        this.drawingArea.setTipoModelo(this.modelType);
        this.updateVmConfigButtonVisibility();
    }

    private void updateVmConfigButtonVisibility ()
    {
        this.jButtonConfigVM.setVisible(this.modelType == EscolherClasse.IAAS);
    }

    private void jMenuItemOpenActionPerformed (final ActionEvent evt)
    {
        if (this.shouldContinueEditingCurrentlyOpenedFile())
            return;

        this.configureFileFilterAndChooser("Iconic Model of Simulation", ISPD_FILE_EXTENSIONS, false);

        if (this.jFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        var file = this.jFileChooser.getSelectedFile();

        if (!this.hasValidIspdFileExtension(file))
        {
            this.invalidFileSelected(file);
            return;
        }

        try
        {
            file = readFileContents(file);
            this.updateGuiWithOpenFile("model opened", file);
        } catch (ClassNotFoundException | ParserConfigurationException | IOException | SAXException ex)
        {
            this.processFileOpeningException(ex);
        }
    }

    private void invalidFileSelected (File file)
    {
        if (file.getName().equals("Torre"))
            this.jScrollPaneDrawingArea.setViewportView(new Stalemate());
        else
            JOptionPane.showMessageDialog(null, __("Invalid file"), __("WARNING"), JOptionPane.PLAIN_MESSAGE);
    }

    private boolean hasValidIspdFileExtension (final File file)
    {
        return file.getName().endsWith(".ims") || file.getName().endsWith(".imsx");
    }

    private void processFileOpeningException (Exception ex)
    {
        Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        final var message = String.format("%s\n%s", __("Error opening file."), ex.getMessage());
        JOptionPane.showMessageDialog(null, message, __("WARNING"), JOptionPane.PLAIN_MESSAGE);
    }

    private File readFileContents (final File file) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException
    {
        if (file.getName().endsWith(".imsx"))
        {
            this.readFileNewExtension(file);
            return file;
        }

        return this.readFileOldExtension(file);
    }

    private void updateGuiWithOpenFile (final String message, final File file)
    {
        this.drawingArea.addKeyListener(this);
        this.drawingArea.setPaineis(this);
        this.jScrollPaneSideBar.setViewportView(null);
        this.jPanelProperties.setjLabelTexto("");
        this.jScrollPaneDrawingArea.setViewportView(this.drawingArea);
        this.appendNotificacao(__(message));
        this.openEditing(file);
    }

    private void readFileNewExtension (final File file) throws ParserConfigurationException, IOException, SAXException
    {
        final var doc = IconicoXML.ler(file);
        this.startNewDrawing(doc);
        this.modelType = this.drawingArea.getTipoModelo();
        this.virtualMachines = this.drawingArea.getMaquinasVirtuais();
        this.updateVmConfigButtonVisibility();
    }

    private File readFileOldExtension (final File file) throws IOException, ClassNotFoundException
    {
        final var description = this.getSystemDescription(file);

        this.drawingArea = new DesenhoGrade(1500, 1500);
        this.drawingArea.setGrade(description);
        return null;
    }

    private DescreveSistema getSystemDescription (final File file) throws IOException, ClassNotFoundException
    {
        try (final var input = new ObjectInputStream(new FileInputStream(file)))
        {
            return (DescreveSistema) input.readObject();
        }
    }

    private void startNewDrawing (final Document doc)
    {
        this.drawingArea = new DesenhoGrade(1500, 1500);
        this.drawingArea.setGrade(doc);
    }

    private boolean shouldContinueEditingCurrentlyOpenedFile ()
    {
        if (!this.currentFileHasUnsavedChanges)
            return false;

        int userChoice = this.saveChanges();
        return userChoice == JOptionPane.CANCEL_OPTION || userChoice == JOptionPane.CLOSED_OPTION;
    }

    private void jMenuItemSaveActionPerformed (final ActionEvent evt)
    {
        if (this.openFile == null)
        {
            this.jMenuItemSaveAsActionPerformed(null);
            return;
        }

        if (this.drawingArea == null)
            return;

        this.saveDrawingAreaToFile(this.openFile);
        this.refreshEdits();
    }

    private void jMenuItemSimGridActionPerformed (final ActionEvent evt)
    {
        this.configureFileFilterAndChooser("XML File", new String[] { ".xml" }, true);

        JOptionPane.showMessageDialog(null, __("Select the application file."), __("WARNING"), JOptionPane.PLAIN_MESSAGE);

        if (this.jFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        final var appFile = this.jFileChooser.getSelectedFile();

        JOptionPane.showMessageDialog(null, __("Select the platform file."), __("WARNING"), JOptionPane.PLAIN_MESSAGE);

        if (this.jFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        final var platformFile = this.jFileChooser.getSelectedFile();

        this.interpretAndOpenModel(appFile, platformFile);
    }

    private void interpretAndOpenModel (final File appFile, final File platFile)
    {
        final var interpreter = new InterpretadorSimGrid();
        interpreter.interpreta(appFile, platFile);

        try
        {
            final var model = interpreter.getModelo();

            if (model == null)
            {
                JOptionPane.showMessageDialog(
                        null,
                        String.format("%s\n", __("File not found.")),
                        __("WARNING"),
                        JOptionPane.PLAIN_MESSAGE
                );
                return;
            }

            this.openModel(model);

        } catch (Exception e)
        {
            final var message = String.format("%s\n%s", __("Error opening file."), e.getMessage());
            JOptionPane.showMessageDialog(null, message, __("WARNING"), JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void openModel (final Document model)
    {
        this.startNewDrawing(model);
        this.drawingArea.iconArrange();
        this.updateGuiWithOpenFile("model opened", null);
        this.modificar();
    }

    private void configureFileFilterAndChooser (
            final String description, final String[] extensions, final boolean shouldAcceptAllFiles)
    {
        this.fileFilter.setDescricao(__(description));
        this.fileFilter.setExtensao(extensions);
        this.jFileChooser.setAcceptAllFileFilterUsed(shouldAcceptAllFiles);
    }

    private void jMenuItemToJPGActionPerformed (final ActionEvent evt)
    {
        this.configureFileFilterAndChooser("JPG Image (.jpg)", new String[] { ".jpg" }, false);

        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        final var file = this.getFileWithExtension(".jpg");
        final var img = this.drawingArea.createImage();

        try
        {
            ImageIO.write(img, "jpg", file);
        } catch (IOException ex)
        {
            Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void jMenuItemCloseActionPerformed (final ActionEvent evt)
    {
        int choice = this.currentFileHasUnsavedChanges ? saveChanges() : JOptionPane.YES_OPTION;

        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION)
            return;

        this.closeModel();
    }

    private void closeModel ()
    {
        this.jScrollPaneDrawingArea.setViewportView(null);
        this.jScrollPaneSideBar.setViewportView(null);
        this.jPanelProperties.setjLabelTexto("");
        this.appendNotificacao(__("model closed"));
        this.closeEditing();
    }

    private void jMenuItemExitActionPerformed (final ActionEvent evt)
    {
        this.formWindowClosing();
    }

    private void jMenuItemPasteActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        this.drawingArea.botaoPainelActionPerformed(evt);
    }

    private void jMenuItemDeleteActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        this.drawingArea.botaoIconeActionPerformed(evt);
    }

    private void jMenuItemCopyActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        this.drawingArea.botaoVerticeActionPerformed(evt);
    }

    private void jMenuItemCompareActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        this.drawingArea.matchNetwork();
    }

    private void showOrHideElements (
            final JCheckBoxMenuItem box,
            final String textIfSelected,
            final String textIfUnselected,
            final Consumer<Boolean> drawingAreaSetter,
            final ActionEvent event
    )
    {
        final boolean isSelected = box.isSelected();
        final String text = isSelected ? textIfSelected : textIfUnselected;
        box.setSelected(isSelected);
        if (this.drawingArea != null)
            drawingAreaSetter.accept(isSelected);
        if (event != null)
            this.appendNotificacao(__(text));
    }

    private void jCheckBoxMenuItemConnectedActionPerformed (final ActionEvent evt)
    {
        this.showOrHideElements(
                this.jCheckBoxMenuConnectedItem,
                "Connected Nodes unhidden.",
                "Connected Nodes hidden.",
                this.drawingArea::setConectados,
                evt
        );
    }

    private void jCheckBoxMenuItemIndirectActionPerformed (final ActionEvent evt)
    {
        this.showOrHideElements(
                this.jCheckBoxIndirectMenuItem,
                "Indirectly Connected Nodes are now being shown",
                "Indirectly Connected Nodes are now not being shown",
                this.drawingArea::setIndiretos,
                evt
        );
    }

    private void jCheckBoxMenuItemSchedulableActionPerformed (final ActionEvent evt)
    {
        this.showOrHideElements(
                this.jCheckBoxMenuSchedulableItem,
                "Schedulable Nodes unhidden.",
                "Schedulable Nodes hidden.",
                this.drawingArea::setEscalonaveis,
                evt
        );
    }

    private void jCheckBoxMenuItemGradeActionPerformed (final ActionEvent evt)
    {
        this.showOrHideElements(
                this.jCheckBoxMenuGridItem,
                "Drawing grid enabled.",
                "Drawing grid disabled.",
                b -> this.drawingArea.setGridOn(b), // Do this to avoid NPE when no file is open // TODO: Disable when no file
                evt
        );
    }

    private void jCheckBoxMenuItemRulerActionPerformed (final ActionEvent evt)
    {
        this.showOrHideElements(
                this.jCheckBoxRulerMenuItem,
                "Drawing rule enabled.",
                "Drawing rule disabled.",
                b -> {
                    if (b) showRuler();
                    else closeDrawingArea();
                },
                evt
        );
    }

    private void showRuler ()
    {
        this.jScrollPaneDrawingArea.setColumnHeaderView(this.drawingArea.getColumnView());
        this.jScrollPaneDrawingArea.setRowHeaderView(this.drawingArea.getRowView());

        this.jScrollPaneDrawingArea.setCorner(JScrollPane.UPPER_LEFT_CORNER, this.drawingArea.getCorner());
        this.jScrollPaneDrawingArea.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
        this.jScrollPaneDrawingArea.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new Corner());
    }

    private void jMenuItemGenerateActionPerformed (final ActionEvent evt)
    {
        if (this.modelType == EscolherClasse.GRID)
        {
            this.generateSchedulerGrid();
            return;
        }

        if (this.modelType == EscolherClasse.IAAS)
        {
            this.generateSchedulerCloud();
            this.generateSchedulerAlloc();
        }
    }

    private void generateScheduler (
            final String path,
            final Consumer<GerarEscalonador> transferSchedulers,
            final Runnable updateSchedulers)
    {
        final var ge = new GerarEscalonador(this, true, path, this.words);
        transferSchedulers.accept(ge);
        this.showSubWindow(ge);
        if (ge.getParse() != null)
            updateSchedulers.run();
    }

    private void generateSchedulerGrid ()
    {
        this.generateScheduler(
                this.jFrameManager.getEscalonadores().getDiretorio().getAbsolutePath(),
                (ge) -> ge.setEscalonadores(this.jFrameManager.getEscalonadores()),
                this.jFrameManager::atualizarEscalonadores
        );
    }

    private void generateSchedulerCloud ()
    {
        this.generateScheduler(
                this.jFrameCloudManager.getEscalonadores().getDiretorio().getAbsolutePath(),
                (ge) -> ge.setEscalonadoresCloud(this.jFrameCloudManager.getEscalonadores()),
                this.jFrameCloudManager::atualizarEscalonadores
        );
    }

    private void generateSchedulerAlloc ()
    {
        this.generateScheduler(
                this.jFrameAllocManager.getAlocadores().getDiretorio().getAbsolutePath(),
                (ge) -> ge.setAlocadores(this.jFrameAllocManager.getAlocadores()),
                this.jFrameAllocManager::atualizarAlocadores
        );
    }

    private void jMenuItemHelpActionPerformed (final ActionEvent evt)
    {
        this.showSubWindow(new TreeHelp());
    }

    private void jMenuItemToTxtActionPerformed (final ActionEvent evt)
    {
        this.configureFileFilterAndChooser("Plane Text", new String[] { ".txt" }, false);

        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        try
        {
            try (final var fw = new FileWriter(this.getFileWithExtension(".txt"));
                 final var pw = new PrintWriter(fw, true))
            {
                pw.print(this.drawingArea.toString());
            }
        } catch (IOException ex)
        {
            Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void jMenuItemSaveAsActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        this.configureFileFilterAndChooser("Iconic Model of Simulation", new String[] { ".imsx" }, false);
        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        final var file = this.getFileWithExtension(".imsx");
        this.saveDrawingAreaToFile(file);
        this.openEditing(file);
    }

    private void saveDrawingAreaToFile (File file)
    {
        final var doc = this.drawingArea.getGrade();
        IconicoXML.escrever(doc, file);
        this.appendNotificacao(__("model saved"));
    }

    private void jButtonUsersActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        final var users = new JUsuarios(
                this,
                true,
                this.drawingArea.getUsuarios(),
                this.words,
                this.drawingArea.getPerfil()
        );

        this.showSubWindow(users);
        this.updateDrawingAreaUsers(users);
        this.modificar();
    }

    private void updateDrawingAreaUsers (final JUsuarios users)
    {
        this.drawingArea.setUsuarios(users.getUsuarios());
        this.drawingArea.setPerfil(users.getLimite());
    }

    private void formWindowClosing ()
    {
        this.configure.setLastFile(this.openFile);
        this.configure.save();

        if (!this.currentFileHasUnsavedChanges)
            System.exit(0);

        int choice = this.saveChanges();
        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION)
            return;

        System.exit(0);
    }

    private void jMenuFileActionPerformed (final ActionEvent evt)
    {
    }

    private void jMenuItemGridSimActionPerformed (final ActionEvent evt)
    {
        this.configureFileFilterAndChooser("Java Source Files (. java)", new String[] { ".java" }, true);

        int returnVal = this.jFileChooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;

        final var window = new JDialog(this, "Carregando");
        final var thread = JPrincipal.threadForLoadingScreen(window);
        window.setLocationRelativeTo(this);
        thread.start();

        try
        {
            this.interpretFileAndUpdateDrawing();
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, __("Error opening file.") + "\n" + e.getMessage(), __("WARNING"), JOptionPane.PLAIN_MESSAGE);
        }

        window.dispose();
    }

    private void interpretFileAndUpdateDrawing ()
    {
        final var file = this.jFileChooser.getSelectedFile();
        final var interpreter = new InterpretadorGridSim();

        if (!file.exists())
        {
            final var message = String.format("%s\n", __("File not found."));
            JOptionPane.showMessageDialog(null, message, __("WARNING"), JOptionPane.PLAIN_MESSAGE);
            return;
        }

        interpreter.interpreta(file);

        final int gridSize = Math.max(interpreter.getW(), 1500);
        this.drawingArea = new DesenhoGrade(gridSize, gridSize);
        this.drawingArea.setGrade(interpreter.getDescricao());
        this.updateGuiWithOpenFile("model opened", null);
        this.modificar();
    }

    private void jMenuItemSortActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea == null)
            return;

        if (this.jMenuItemSort.getDisplayedMnemonicIndex() == 2)
        {
            this.jMenuItemSort.setDisplayedMnemonicIndex(1);
            this.drawingArea.iconArrangeType();
        } else
        {
            this.jMenuItemSort.setDisplayedMnemonicIndex(2);
            this.drawingArea.iconArrange();
        }

        this.drawingArea.repaint();
    }

    private void jMenuItemToSimGridActionPerformed (final ActionEvent evt)
    {
        this.exportToFileType("XML File", ".xml");
    }

    private void exportToFileType (final String description, final String extension)
    {
        this.configureFileFilterAndChooser(description, new String[] { extension }, false);

        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        final var file = this.getFileWithExtension(extension);

        try
        {
            new Exportador(this.drawingArea.getGrade()).toGridSim(file);
            JOptionPane.showMessageDialog(this, __("model saved"), "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage(), __("WARNING"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jMenuItemOpenResultActionPerformed (final ActionEvent evt)
    {
        this.jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.openResultInternal();
        this.jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private void openResultInternal ()
    {
        if (this.jFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        final var dir = this.jFileChooser.getSelectedFile();

        if (!JPrincipal.isValidDirectory(dir))
            return;

        try
        {
            final var path = String.format("file://%s/result.html", dir.getAbsolutePath());
            HtmlPane.openDefaultBrowser(new URL(path));
        } catch (IOException e)
        {
            final var message = String.format("%s\n%s", __("Error opening file."), e.getMessage());
            JOptionPane.showMessageDialog(null, message, __("WARNING"), JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void jMenuItemToGridSimActionPerformed (final ActionEvent evt)
    {
        this.exportToFileType("Java Source Files (. java)", ".java");
    }

    private File getFileWithExtension (final String ext)
    {
        final var file = this.jFileChooser.getSelectedFile();

        if (file.getName().endsWith(ext))
            return file;

        return new File(file + ext);
    }

    private void jButtonConfigVMActionPerformed (final ActionEvent evt)
    {
        if (this.drawingArea.getNosEscalonadores().isEmpty())
        {
            JOptionPane.showMessageDialog(
                    null,
                    "One or more VMMs need to be configurated",
                    "WARNING!",
                    JOptionPane.PLAIN_MESSAGE
            );
            return;
        }

        final var vmConfigWindow = new ConfigurarVMs(
                this,
                true,
                this.drawingArea.getUsuarios().toArray(),
                this.drawingArea.getNosEscalonadores().toArray(),
                this.virtualMachines
        );

        this.showSubWindow(vmConfigWindow);
        this.updateVirtualMachines(vmConfigWindow);
        this.modificar();
    }

    private void updateVirtualMachines (final ConfigurarVMs vmConfigWindow)
    {
        this.virtualMachines = vmConfigWindow.getMaqVirtuais();
        this.updateDrawingVms(vmConfigWindow);
    }

    private void updateDrawingVms (final ConfigurarVMs vmConfigWindow)
    {
        // TODO: Feature envy
        this.drawingArea.setUsuarios(vmConfigWindow.atualizaUsuarios());
        this.drawingArea.setMaquinasVirtuais(vmConfigWindow.getMaqVirtuais());
    }

    private void jMenuItemManageCloudActionPerformed (final ActionEvent evt)
    {
        this.showSubWindow(this.jFrameCloudManager);
    }

    private void jMenuItemManageAllocationActionPerformed (final ActionEvent evt)
    {
        this.showSubWindow(this.jFrameAllocManager);
    }

    private void showSubWindow (final Window w)
    {
        w.setLocationRelativeTo(this);
        w.setVisible(true);
    }

    private void jMenuToolsActionPerformed (final ActionEvent evt)
    {
    }

    private void jButtonInjectFaultsActionPerformed (final ActionEvent evt)
    {
        new JSelecionarFalhas().setVisible(true);
    }

    public JPanelConfigIcon getjPanelConfiguracao ()
    {
        return this.jPanelSettings;
    }

    private void initTexts ()
    {
        this.jScrollPaneSideBar.setBorder(BorderFactory.createTitledBorder(__("Settings")));
        this.jScrollPaneProperties.setBorder(BorderFactory.createTitledBorder(__("Properties")));
        this.jScrollPaneNotificationBar.setBorder(BorderFactory.createTitledBorder(__("Notifications")));

        this.jToggleButtonMachine.setToolTipText(__("Selects machine icon for add to the model"));
        this.jToggleButtonNetwork.setToolTipText(__("Selects network icon for add to the model"));
        this.jToggleButtonCluster.setToolTipText(__("Selects cluster icon for add to the model"));
        this.jToggleButtonInternet.setToolTipText(__("Selects internet icon for add to the model"));
        this.jButtonTasks.setToolTipText(__("Selects insertion model of tasks"));
        this.jButtonUsers.setToolTipText(__("Add and remove users to the model"));
        this.jButtonSimulate.setText(__("Simulate"));
        this.jButtonSimulate.setToolTipText(__("Starts the simulation"));

        this.jButtonInjectFaults.setText(__("Simulate"));
        this.jButtonInjectFaults.setToolTipText("Select the faults");

        this.jMenuFile.setText(__("File"));
        this.jMenuItemNew.setText(__("New"));
        this.jMenuItemNew.setToolTipText(__("Starts a new model"));
        this.jMenuItemOpen.setText(__("Open"));
        this.jMenuItemOpen.setToolTipText(__("Opens an existing model"));
        this.jMenuItemSave.setText(__("Save"));
        this.jMenuItemSave.setToolTipText(__("Save the open model"));
        this.jMenuImport.setText(__("Import"));
        this.jMenuItemSimGrid.setText(__("SimGrid model"));
        this.jMenuItemSimGrid.setToolTipText(__("Open model from the specification files of Simgrid"));
        this.jMenuExport.setText(__("Export"));
        this.jMenuItemToJPG.setText(__("to JPG"));
        this.jMenuItemToJPG.setToolTipText(__("Creates a jpg file with the model image"));
        this.jMenuItemToTxt.setText(__("to TXT"));
        this.jMenuItemToTxt.setToolTipText(__("Creates a file in plain text with the model data according to the grammar of the iconic model"));
        this.jMenuLanguage.setText(__("Language"));
        this.jMenuItemEnglish.setText(__("English"));
        this.jMenuItemPortuguese.setText(__("Portuguese"));
        this.jMenuItemClose.setText(__("Close"));
        this.jMenuItemClose.setToolTipText(__("Closes the currently open model"));
        this.jMenuItemExit.setText(__("Exit"));
        this.jMenuItemExit.setToolTipText(__("Closes the program"));

        this.jMenuEdit.setText(__("Edit"));
        this.jMenuItemCopy.setText(__("Copy"));
        this.jMenuItemPaste.setText(__("Paste"));
        this.jMenuItemDelete.setText(__("Delete"));
        this.jMenuItemCompare.setText(__("Match network settings"));
        this.jMenuItemCompare.setToolTipText(__("Matches the settings of icons of networks according to a selected icon"));

        this.jMenuShow.setText(__("View"));
        this.jCheckBoxMenuConnectedItem.setText(__("Show Connected Nodes"));
        this.jCheckBoxMenuConnectedItem.setToolTipText(__("Displays in the settings area, the list of nodes connected for the selected icon"));
        this.jCheckBoxIndirectMenuItem.setText(__("Show Indirectly Connected Nodes"));
        this.jCheckBoxIndirectMenuItem.setToolTipText(__("Displays in the settings area, the list of nodes connected through the internet icon, to the icon selected"));
        this.jCheckBoxMenuSchedulableItem.setText(__("Show Schedulable Nodes"));
        this.jCheckBoxMenuSchedulableItem.setToolTipText(__("Displays in the settings area, the list of nodes schedulable for the selected icon"));
        this.jCheckBoxMenuGridItem.setText(__("Drawing grid"));
        this.jCheckBoxMenuGridItem.setToolTipText(__("Displays grid in the drawing area"));
        this.jCheckBoxRulerMenuItem.setText(__("Drawing rule"));
        this.jCheckBoxRulerMenuItem.setToolTipText(__("Displays rule in the drawing area"));

        this.jMenuTools.setText(__("Tools"));
        this.jMenuItemManage.setText(__("Manage Schedulers"));
        this.jMenuItemGenerate.setText(__("Generate Scheduler"));

        this.jMenuHelp.setText(__("Help"));
        this.jMenuItemHelp.setText(__("Help"));
        this.jMenuItemHelp.setToolTipText(__("Help"));
        this.jMenuItemAbout.setText(__("About") + " " + __("nomePrograma"));
        this.jMenuItemAbout.setToolTipText(__("About") + " " + __("nomePrograma"));

        this.jPanelSimple.setjLabelTexto(__("No icon selected."));
        this.jPanelSettings.setPalavras(this.words);
    }

    public void appendNotificacao (final String notificationText)
    {
        this.jTextAreaNotification.append(notificationText + "\n");
    }

    public void modificar ()
    {
        final var newTitle = String.format("%s [%s] - %s",
                this.getOpenFileNameOrDefault(), __("modified"), __("nomePrograma"));

        this.setTitle(newTitle);
        this.currentFileHasUnsavedChanges = true;
    }

    public void refreshEdits ()
    {
        final var newTitle = String.format("%s - %s",
                this.getOpenFileNameOrDefault(), __("nomePrograma"));

        this.setTitle(newTitle);
        this.currentFileHasUnsavedChanges = false;
    }

    private int saveChanges ()
    {
        final int choice = this.getChoiceForSavingChanges();

        if (choice == JOptionPane.YES_OPTION)
        {
            this.jMenuItemSaveActionPerformed(null);
            this.refreshEdits();
        }

        return choice;
    }

    private int getChoiceForSavingChanges ()
    {
        final var message = String.format("%s %s",
                __("Do you want to save changes to"), this.getOpenFileNameOrDefault());

        return JOptionPane.showConfirmDialog(this, message);
    }

    private String getOpenFileNameOrDefault ()
    {
        return (this.openFile == null)
                ? "New_Model.ims"
                : this.openFile.getName();
    }

    private void openEditing (final File file)
    {
        this.openFile = file;
        this.updateDrawingWithViewMenuOptions();
        this.jCheckBoxMenuItemRulerActionPerformed(null);
        this.deselectAllIconButtons();
        this.enableInteractables();
        this.refreshEdits();
    }

    private void enableInteractables ()
    {
        this.setInteractablesEnabled(true);
    }

    private void updateDrawingWithViewMenuOptions ()
    {
        this.drawingArea.setConectados(this.jCheckBoxMenuConnectedItem.isSelected());
        this.drawingArea.setIndiretos(this.jCheckBoxIndirectMenuItem.isSelected());
        this.drawingArea.setEscalonaveis(this.jCheckBoxMenuSchedulableItem.isSelected());
        this.drawingArea.setGridOn(this.jCheckBoxMenuGridItem.isSelected());
    }

    private void closeEditing ()
    {
        this.setTitle(__("nomePrograma"));
        this.openFile = null;
        this.disableInteractables();
        this.currentFileHasUnsavedChanges = false;
        this.closeDrawingArea();
    }

    private void closeDrawingArea ()
    {
        this.jScrollPaneDrawingArea.setColumnHeaderView(null);
        this.jScrollPaneDrawingArea.setRowHeaderView(null);
        this.jScrollPaneDrawingArea.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
        this.jScrollPaneDrawingArea.setCorner(JScrollPane.LOWER_LEFT_CORNER, null);
        this.jScrollPaneDrawingArea.setCorner(JScrollPane.UPPER_RIGHT_CORNER, null);
    }

    private void disableInteractables ()
    {
        this.setInteractablesEnabled(false);
    }

    private void setInteractablesEnabled (final boolean enabled)
    {
        for (var i : this.interactables)
            i.setEnabled(enabled);
    }

    @Override
    public void keyTyped (final KeyEvent e)
    {
    }

    @Override
    public void keyPressed (final KeyEvent evt)
    {
        if (this.drawingArea == null)
            return;

        if (evt.getKeyCode() == KeyEvent.VK_DELETE)
            this.drawingArea.botaoIconeActionPerformed(null);

        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_C)
            this.drawingArea.botaoVerticeActionPerformed(null);

        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_V)
            this.drawingArea.botaoPainelActionPerformed(null);
    }

    @Override
    public void keyReleased (final KeyEvent e)
    {
    }

    public void setSelectedIcon (final ItemGrade icon, final String text)
    {
        if (icon == null)
        {
            this.jScrollPaneSideBar.setViewportView(this.jPanelSimple);
            this.jPanelProperties.setjLabelTexto("");
            return;
        }

        if (icon instanceof Machine || icon instanceof Cluster)
        {
            this.jPanelSettings.setIcone(icon, this.drawingArea.getUsuarios(), this.modelType);
        } else
        {
            this.jPanelSettings.setIcone(icon);
        }

        this.jScrollPaneSideBar.setViewportView(this.jPanelSettings);
        this.jPanelProperties.setjLabelTexto(text);
    }

    private static class IspdFileView extends FileView
    {
        @Override
        public Icon getIcon (final File file)
        {
            return JPrincipal.getIconForFileExtension(file);
        }
    }

    private static class IspdWindowAdapter extends WindowAdapter
    {
        private final JPrincipal mainWindow;

        private IspdWindowAdapter (final JPrincipal mainWindow)
        {
            this.mainWindow = mainWindow;
        }

        @Override
        public void windowClosing (final WindowEvent evt)
        {
            mainWindow.formWindowClosing();
        }
    }
}