package ispd.gui;

import ispd.policy.managers.CloudSchedulingPolicyManager;

public class CloudSchedulingPolicyManagementWindow
        extends GenericPolicyManagementWindow {
    protected CloudSchedulingPolicyManagementWindow() {
        super(new CloudSchedulingPolicyManager());
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
        return "Manage Cloud Scheduling Policies";
    }
}