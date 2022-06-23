package ispd.gui;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.Optional;

/**
 * Utility class for constructing buttons in a builder-like approach.
 * <p>
 * TODO: Abstract away construction, may want to return AbstractButton instead.
 */
public class ButtonBuilder {
    private final String text;
    private final ActionListener onClick;
    private Optional<String> toolTip = Optional.empty();

    private ButtonBuilder(final String text, final ActionListener onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    static ButtonBuilder aButton(
            final String text, final ActionListener onClick) {
        return new ButtonBuilder(text, onClick);
    }

    ButtonBuilder withToolTip(final String toolTipText) {
        this.toolTip = Optional.of(toolTipText);
        return this;
    }

    public JButton build() {
        final var button = new JButton(this.text);
        button.addActionListener(this.onClick);
        this.toolTip.ifPresent(button::setToolTipText);
        return button;
    }
}