package ispd.gui.utils;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Multipane extends JScrollPane {

    /**
     * It represents the list used to store the buttons that are going to
     * be displayed at the top of the multipane and are used to switch among
     * the panes.
     * <p>
     * Each button contains the pane it represents, such that when a user
     * clicks on the button, then the pane represented by the button is
     * switched on.
     */
    private final List<MultipaneButton> buttons;

    /**
     * Constructor which specifies the buttons in this multipane.
     *
     * @param buttons the multipane buttons
     *
     * @throws NullPointerException if buttons is {@code null}
     */
    public Multipane(final List<MultipaneButton> buttons) {
        if (buttons == null)
            throw new NullPointerException("buttons is null. It was not possible created the multipane.");

        /* Filter those buttons which has a component */
        this.buttons = buttons
                .stream()
                .filter((button) -> button.getComponent() != null)
                .collect(Collectors.toList());

        /* Multipane initialization */
        this.initializeMultipane();
    }

    /**
     * It initializes the multipane.
     */
    public void initializeMultipane() {
        final var prePane = new JPanel();
        final var toolbar = new JToolBar();

        this.setViewportView(prePane);

        prePane.setLayout(new BoxLayout(prePane, BoxLayout.Y_AXIS));
        prePane.add(toolbar);

        /* Add the first button's component as the default one */
        if (!this.buttons.isEmpty())
            prePane.add(this.buttons.get(0).getComponent());

        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));

        final Consumer<Component> switchComponent = (component) -> {
            /* Remove all others components from the pre pane. */
            this.buttons
                    .stream()
                    .map(MultipaneButton::getComponent)
                    .filter((c) -> c != component).forEach(prePane::remove);

            /* Add the switched in component */
            prePane.add(component);

            this.repaint();
        };

        for (final var multipaneButton : this.buttons) {
            multipaneButton.setFocusable(false);
            multipaneButton.setHorizontalTextPosition(SwingConstants.LEFT);
            multipaneButton.setVerticalTextPosition(SwingConstants.CENTER);
            multipaneButton.addActionListener((event) ->
                    switchComponent.accept(multipaneButton.getComponent()));

            toolbar.add(multipaneButton);
        }
    }
}
