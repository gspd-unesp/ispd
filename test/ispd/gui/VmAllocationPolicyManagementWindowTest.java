package ispd.gui;

import ispd.gui.policy.GridSchedulingPolicyManagementWindow;
import ispd.gui.policy.VmAllocationPolicyManagementWindow;
import org.junit.jupiter.api.Test;

import javax.swing.WindowConstants;

class VmAllocationPolicyManagementWindowTest {
    @Test
    public void testWindow_compareWithNonRefactored() {
        final var window = new VmAllocationPolicyManagementWindow();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final var compar = new GridSchedulingPolicyManagementWindow();
        compar.setLocationRelativeTo(null);
        compar.setVisible(true);
        compar.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        while (true) {
            continue;
        }
    }
}