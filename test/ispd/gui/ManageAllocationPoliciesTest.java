package ispd.gui;

import org.junit.jupiter.api.Test;

class ManageAllocationPoliciesTest {
    @Test
    public void allocatorManagementWindow_onStart_doesNotThrow ()
    {
        final var window = new ManageAllocationPolicies();
        window.setVisible(true);
    }
}