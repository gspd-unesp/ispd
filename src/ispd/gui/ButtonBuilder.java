package ispd.gui;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.event.ActionListener;

/**
 * Utility class for constructing buttons in a builder-like approach.
 * <p>
 * TODO: Abstract away construction, may want to return AbstractButton instead.
 * TODO: Optimize 'fixed' build patterns (e.g., basicButton)
 */
public class ButtonBuilder {
    private final JButton button;

    private ButtonBuilder(final String text, final ActionListener onClick) {
        this.button = new JButton(text);
        this.button.addActionListener(onClick);
    }
    private ButtonBuilder(final Icon icon, final ActionListener onClick){
        this.button = new JButton(icon);
        this.button.addActionListener(onClick);
    }

    static JButton basicButton(
            final String text, final ActionListener onClick) {
        return ButtonBuilder.aButton(text, onClick).build();
    }

    public JButton build() {
        return this.button;
    }

    static ButtonBuilder aButton(
            final String text, final ActionListener onClick) {
        return new ButtonBuilder(text, onClick);
    }

    static ButtonBuilder aButton(
            final Icon icon, final ActionListener onClick) {
        return new ButtonBuilder(icon, onClick);
    }

    ButtonBuilder withToolTip(final String theText) {
        this.button.setToolTipText(theText);
        return this;
    }

    ButtonBuilder withIcon(final Icon theIcon) {
        this.button.setIcon(theIcon);
        return this;
    }

    ButtonBuilder nonFocusable() {
        this.button.setFocusable(false);
        return this;
    }

    ButtonBuilder withSize(final Dimension theSize) {
        // TODO: Find out if these three together are equivalent to setSize()
        this.button.setMaximumSize(theSize);
        this.button.setMinimumSize(theSize);
        this.button.setPreferredSize(theSize);
        return this;
    }

    ButtonBuilder withPreferredSize(final Dimension theSize) {
        this.button.setPreferredSize(theSize);
        return this;
    }

    ButtonBuilder withCenterBottomTextPosition() {
        this.button.setHorizontalTextPosition(SwingConstants.CENTER);
        this.button.setVerticalTextPosition(SwingConstants.BOTTOM);
        return this;
    }

    ButtonBuilder disabled() {
        this.button.setEnabled(false);
        return this;
    }

    ButtonBuilder withActionCommand(final String theText) {
        this.button.setActionCommand(theText);
        return this;
    }
}