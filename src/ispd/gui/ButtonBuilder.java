package ispd.gui;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Optional;

/**
 * Utility class for constructing buttons in a builder-like approach.
 * <p>
 * TODO: Abstract away construction, may want to return AbstractButton instead.
 * TODO: Optimize 'fixed' build patterns (e.g., basicButton)
 * TODO: Optionals may be unnecessary. Keep button instead and update it.
 */
public class ButtonBuilder {
    private final String text;
    private final ActionListener onClick;
    private Optional<String> toolTip = Optional.empty();
    private Optional<ImageIcon> icon = Optional.empty();
    private Optional<Boolean> focusability = Optional.empty();
    private Optional<Dimension> size = Optional.empty();
    private Optional<Dimension> preferredSize = Optional.empty();
    private Optional<Position> textPosition = Optional.empty();

    private ButtonBuilder(final String text, final ActionListener onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    static JButton basicButton(
            final String text, final ActionListener onClick) {
        return ButtonBuilder.aButton(text, onClick).build();
    }

    public JButton build() {
        final var button = new JButton(this.text);
        button.addActionListener(this.onClick);
        this.toolTip.ifPresent(button::setToolTipText);
        this.icon.ifPresent(button::setIcon);
        this.focusability.ifPresent(button::setFocusable);
        this.size.ifPresent(button::setSize);
        this.preferredSize.ifPresent(button::setPreferredSize);
        this.setButtonTextPosition(button);
        return button;
    }

    static ButtonBuilder aButton(
            final String text, final ActionListener onClick) {
        return new ButtonBuilder(text, onClick);
    }

    private void setButtonTextPosition(final AbstractButton button) {
        if (this.textPosition.isEmpty())
            return;
        button.setHorizontalTextPosition(this.textPosition.get().h());
        button.setVerticalTextPosition(this.textPosition.get().v());
    }

    ButtonBuilder withToolTip(final String theText) {
        this.toolTip = Optional.of(theText);
        return this;
    }

    ButtonBuilder withIcon(final ImageIcon theIcon) {
        this.icon = Optional.of(theIcon);
        return this;
    }

    ButtonBuilder nonFocusable() {
        this.focusability = Optional.of(false);
        return this;
    }

    ButtonBuilder withSize(final Dimension theSize) {
        this.size = Optional.of(theSize);
        return this;
    }

    ButtonBuilder withPreferredSize(final Dimension theSize) {
        this.preferredSize = Optional.of(theSize);
        return this;
    }

    ButtonBuilder withTextPosition(final int horizontal, final int vertical) {
        this.textPosition = Optional.of(new Position(horizontal, vertical));
        return this;
    }

    private record Position(int h, int v) {
    }
}