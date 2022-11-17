package ispd.gui;

import org.junit.jupiter.api.Test;

import javax.swing.WindowConstants;

class ManageAllocationPoliciesTest {
    @Test
    public void testWindow_compareWithNonRefactored() {
        final var window = new ManageAllocationPolicies();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final var compar = new ManageSchedulers();
        compar.setLocationRelativeTo(null);
        compar.setVisible(true);
        compar.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        while (true) {
            continue;
        }
    }
}