package ispd.policy;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class PolicyConditions {
    // TODO: Document what "ALL" entails
    public static final Set<PolicyCondition> ALL =
            Collections.unmodifiableSet(EnumSet.allOf(PolicyCondition.class));
    public static final Set<PolicyCondition> WHILE_MUST_DISTRIBUTE =
            Collections.unmodifiableSet(EnumSet.of(PolicyCondition.WHILE_MUST_DISTRIBUTE));
    @SuppressWarnings("unused")
    public static final Set<PolicyCondition> WHEN_RECEIVES_RESULT =
            Collections.unmodifiableSet(EnumSet.of(PolicyCondition.WHEN_RECEIVES_RESULT));
}
