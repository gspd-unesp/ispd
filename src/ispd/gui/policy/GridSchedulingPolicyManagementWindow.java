package ispd.gui.policy;

import ispd.policy.managers.GridSchedulingPolicyManager;

public class GridSchedulingPolicyManagementWindow
        extends GenericPolicyManagementWindow {
    public GridSchedulingPolicyManagementWindow() {
        super(new GridSchedulingPolicyManager());
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
        return "Manage Grid Scheduling Policies";
    }
}