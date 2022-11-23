package ispd.gui.policy;

import ispd.policy.managers.VmAllocationPolicyManager;

public class VmAllocationPolicyManagementWindow
        extends GenericPolicyManagementWindow {
    public VmAllocationPolicyManagementWindow() {
        super(new VmAllocationPolicyManager());
    }

    @Override
    protected String getButtonOpenTooltip() {
        return "Opens an existing policy";
    }

    @Override
    protected String getButtonNewTooltip() {
        return "Creates a new policy";
    }

    @Override
    protected String getPolicyListTitle() {
        return "Policies";
    }

    @Override
    protected String getWindowTitle() {
        return "Manage Allocation Policies";
    }
}
