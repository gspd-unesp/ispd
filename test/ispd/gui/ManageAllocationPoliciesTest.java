package ispd.gui;

import org.junit.jupiter.api.Test;

import javax.swing.WindowConstants;

class ManageAllocationPoliciesTest {
    @Test
    public void allocatorManagementWindow_startAndLoop() {
        final var window = new ManageAllocationPolicies();
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        while (true) {
            continue;
        }
    }
}