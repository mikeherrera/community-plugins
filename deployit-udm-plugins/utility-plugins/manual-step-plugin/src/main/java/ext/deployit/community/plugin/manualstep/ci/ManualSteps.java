package ext.deployit.community.plugin.manualstep.ci;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.base.BaseConfigurationItem;

@SuppressWarnings("serial")
@Metadata(root = Metadata.ConfigurationItemRoot.CONFIGURATION, description = "Contains a list of ManualStep configurations.")
public class ManualSteps extends BaseConfigurationItem {

    public static final String MANUAL_STEPS = "manualSteps";
    @Property(description = "List of ManualStep configurations")
    private List<ManualStep> steps = newArrayList();

    public List<ManualStep> getSteps() {
        return steps;
    }

    public void setSteps(List<ManualStep> steps) {
        this.steps = steps;
    }

    public static Iterable<ManualStep> getSteps(Environment environment, final ContributorType contributorType, final Operation operation) {
        if (!environment.hasProperty(MANUAL_STEPS)) {
            return Collections.emptyList();
        }
        List<ManualSteps> manualStepSets = environment.getProperty(MANUAL_STEPS);
        Iterable<ManualStep> manualSteps = concat(transform(manualStepSets, new Function<ManualSteps, Iterable<ManualStep>>() {
            @Override
            public Iterable<ManualStep> apply(ManualSteps input) {
                return filter(input.getSteps(), new Predicate<ManualStep>() {
                    @Override
                    public boolean apply(ManualStep input) {
                        return (input.getContributorType() == contributorType) &&
                                ((input.getOperation() == DeploymentOperation.ANY) || (input.getOperation().getOperation() == operation));
                    }
                });
            }
        }));

        return manualSteps;
    }
    
    public static Iterable<ManualStep> getSteps(Container container, final ContributorType contributorType, final Operation operation) {
        if (! container.hasProperty(MANUAL_STEPS)) {
            return Collections.emptyList();
        }
        List<ManualSteps> manualStepSets = container.getProperty(MANUAL_STEPS);
        Iterable<ManualStep> manualSteps = concat(transform(manualStepSets, new Function<ManualSteps, Iterable<ManualStep>>() {
            @Override
            public Iterable<ManualStep> apply(ManualSteps input) {
                return filter(input.getSteps(), new Predicate<ManualStep>() {
                    @Override
                    public boolean apply(ManualStep input) {
                        return (input.getContributorType() == contributorType) &&
                                ((input.getOperation() == DeploymentOperation.ANY) || (input.getOperation().getOperation() == operation));
                    }
                });
            }
        }));

        return manualSteps;
    }
}

