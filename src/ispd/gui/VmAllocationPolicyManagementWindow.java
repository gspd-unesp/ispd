package ispd.gui;

import ispd.policy.managers.VmAllocationPolicyManager;

class VmAllocationPolicyManagementWindow extends GenericPolicyManagementWindow {
    public VmAllocationPolicyManagementWindow() {
        super(new VmAllocationPolicyManager());
    }

    @Override
    protected String getWindowTitle() {
        return this.translate("Manage Allocation Policies");
    }
}
