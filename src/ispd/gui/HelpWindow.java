/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ispd.gui;

import ispd.gui.auxiliar.HtmlPane;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

public class HelpWindow extends JFrame implements TreeSelectionListener {
    private static final boolean DEBUG = false;
    private static final Dimension CONVENIENT_SIZE =
            new Dimension(700, 400);
    private static final int DIVIDER_LOCATION = 200;
    private final JEditorPane htmlPane;
    private final JTree tree;
    private URL helpURL;

    HelpWindow() {
        this.setTitle("Help");
        this.setMinimumSize(HelpWindow.CONVENIENT_SIZE);
        final var image =
                Toolkit.getDefaultToolkit().getImage(
                        this.getClass().getResource(
                                "imagens/Logo_iSPD_25.png"));
        this.setIconImage(image);

        //Create the nodes.
        final var top = new DefaultMutableTreeNode(
                "This Project");
        HelpWindow.createNodes(top);

        //Create a tree that allows one selection at a time.
        this.tree = new JTree(top);
        this.tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        this.tree.addTreeSelectionListener(this);

        //Create the scroll pane and add the tree to it.
        final var treeView = new JScrollPane(this.tree);

        //Create the HTML viewing pane.
        this.htmlPane = new HtmlPane();
        this.initHelp();
        final var htmlView = new JScrollPane(this.htmlPane);

        //Add the scroll panes to a split pane.
        final var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(treeView);
        splitPane.setRightComponent(htmlView);

        final var minimumSize = new Dimension(50, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(HelpWindow.DIVIDER_LOCATION);
        splitPane.setPreferredSize(HelpWindow.CONVENIENT_SIZE);

        //Add the split pane to this panel.
        this.add(splitPane);
    }

    private static void createNodes(final DefaultMutableTreeNode top) {
        final var category = new DefaultMutableTreeNode("Interface");
        top.add(category);

        category.add(new DefaultMutableTreeNode(new BookInfo(
                "Icons", "html/icones.html")));
    }

    private void initHelp() {
        final var s = "html/HelpStart.html";
        this.helpURL = this.getClass().getResource(s);
        if (this.helpURL == null) {
            System.err.println("Couldn't open help file: " + s);
        } else if (HelpWindow.DEBUG) {
            System.out.println("Help URL is " + this.helpURL);
        }
        this.displayURL(this.helpURL);
    }

    private void displayURL(final URL url) {
        try {
            if (url != null) {
                this.htmlPane.setPage(url);
                return;
            }

            this.htmlPane.setText("File Not Found");
            if (HelpWindow.DEBUG) {
                System.out.println("Attempted to display a null URL.");
            }
        } catch (final IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }

    /**
     * Required by TreeSelectionListener interface.
     */
    public void valueChanged(final TreeSelectionEvent e) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                this.tree.getLastSelectedPathComponent();

        if (node == null) return;

        final Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            final var book = (BookInfo) nodeInfo;
            this.displayURL(book.getBookURL());
            if (HelpWindow.DEBUG) {
                System.out.print(book.getBookURL() + ":  \n    ");
            }
        } else {
            this.displayURL(this.helpURL);
        }
        if (HelpWindow.DEBUG) {
            System.out.println(nodeInfo.toString());
        }
    }

    private static class BookInfo {
        private final String bookName;
        private final URL bookURL;

        private BookInfo(final String book, final String filename) {
            this.bookName = book;
            this.bookURL = this.getClass().getResource(filename);
            if (this.bookURL == null) {
                System.err.println("Couldn't find the file: " + filename);
            }
        }

        @Override
        public String toString() {
            return this.bookName;
        }

        URL getBookURL() {
            return this.bookURL;
        }
    }
}