package ispd.gui.utils;

import javax.swing.JButton;
import java.awt.Component;

public class MultipaneButton extends JButton {

    /**
     * It represents the component that is going to be switched in the multipane.
     */
    private final Component component;

    /**
     * Constructor which specifies the button name and the component.
     *
     * @param name the button name
     * @param component the component to be switched when clicked on the
     *                  button.
     */
    public MultipaneButton(final String name,
                                  final Component component) {
        super(name);
        this.component = component;
    }

    /**
     * It returns the multipane button's component.
     *
     * @return the multipane button's component.
     */
    public Component getComponent() {
        return this.component;
    }
}
