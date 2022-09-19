package ispd.gui.utils;

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

    public static JButton basicButton(
            final String text, final ActionListener onClick) {
        return ButtonBuilder.aButton(text, onClick).build();
    }

    public JButton build() {
        return this.button;
    }

    public static ButtonBuilder aButton(
            final String text, final ActionListener onClick) {
        return new ButtonBuilder(text, onClick);
    }

    public static ButtonBuilder aButton(
            final Icon icon, final ActionListener onClick) {
        return new ButtonBuilder(icon, onClick);
    }

    public ButtonBuilder withToolTip(final String theText) {
        this.button.setToolTipText(theText);
        return this;
    }

    public ButtonBuilder withIcon(final Icon theIcon) {
        this.button.setIcon(theIcon);
        return this;
    }

    public ButtonBuilder nonFocusable() {
        this.button.setFocusable(false);
        return this;
    }

    public ButtonBuilder withSize(final Dimension theSize) {
        // TODO: Find out if these three together are equivalent to setSize()
        this.button.setMaximumSize(theSize);
        this.button.setMinimumSize(theSize);
        this.button.setPreferredSize(theSize);
        return this;
    }

    public ButtonBuilder withPreferredSize(final Dimension theSize) {
        this.button.setPreferredSize(theSize);
        return this;
    }

    public ButtonBuilder withCenterBottomTextPosition() {
        this.button.setHorizontalTextPosition(SwingConstants.CENTER);
        this.button.setVerticalTextPosition(SwingConstants.BOTTOM);
        return this;
    }

    public ButtonBuilder disabled() {
        this.button.setEnabled(false);
        return this;
    }

    public ButtonBuilder withActionCommand(final String theText) {
        this.button.setActionCommand(theText);
        return this;
    }
}