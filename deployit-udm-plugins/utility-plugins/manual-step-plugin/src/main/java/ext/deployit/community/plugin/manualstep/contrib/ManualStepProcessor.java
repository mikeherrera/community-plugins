package ext.deployit.community.plugin.manualstep.contrib;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.xebialabs.deployit.plugin.api.deployment.planning.PostPlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.Environment;

import ext.deployit.community.plugin.manualstep.ci.ContributorType;
import ext.deployit.community.plugin.manualstep.ci.ManualStep;
import ext.deployit.community.plugin.manualstep.ci.ManualSteps;
import ext.deployit.community.plugin.manualstep.step.InstructionStep;
import ext.deployit.community.plugin.manualstep.util.Util;

public class ManualStepProcessor {

    @PrePlanProcessor
    public List<Step> triggerManualStepsForPrePlanProcessor(DeltaSpecification deltaSpec) {
        return triggerManualSteps(deltaSpec, ContributorType.ONCE_AT_THE_START);
    }

    @PostPlanProcessor
    public List<Step> triggerManualStepsForPostPlanProcessor(DeltaSpecification deltaSpec) {
        return triggerManualSteps(deltaSpec, ContributorType.ONCE_AT_THE_END);
    }

    @SuppressWarnings("rawtypes")
	private List<Step> triggerManualSteps(DeltaSpecification deltaSpec, ContributorType contributorType) {
        Map<String, Object> commonVars = newHashMap();
        commonVars.put("deltas", deltaSpec.getDeltas());
        commonVars.put("deployedApplication", deltaSpec.getDeployedApplication());
        commonVars.put("previousDeployedApplication", deltaSpec.getPreviousDeployedApplication());
        commonVars.put("operation", deltaSpec.getOperation());

        Environment environment = deltaSpec.getDeployedApplication().getEnvironment();
        Iterable<ManualStep> manualSteps = ManualSteps.getSteps(environment, contributorType, deltaSpec.getOperation());
        List<Step> steps = newArrayList();
        for (ManualStep manualStep : manualSteps) {
            HashMap<String,Object> vars = newHashMap(commonVars);
            vars.put("step",manualStep);
            InstructionStep step = new InstructionStep(manualStep, vars);
            steps.add(step);
        }
        
        Map<String, Container> deployedContainers = newHashMap();
        Set<Deployed> deployeds = deltaSpec.getDeployedApplication().getDeployeds();
        for (Deployed deployed : deployeds)	{
        	Container container = deployed.getContainer();
        	String containerIdentifier = Util.getContainerHostName(deployed.getContainer()) + "-" + container.getName();
        	
        	if (deployedContainers.get(containerIdentifier) == null)
        		deployedContainers.put(containerIdentifier, container);
        }
        
        Iterator<Entry<String, Container>> it = deployedContainers.entrySet().iterator();
        while (it.hasNext())	{
        	Map.Entry<String, Container> uniqueDeployedContainerPair = it.next();
        	Container uniqueDeployedContainer = uniqueDeployedContainerPair.getValue();
            Iterable<ManualStep> containerManualSteps = ManualSteps.getSteps(uniqueDeployedContainer, contributorType, deltaSpec.getOperation());
            for (ManualStep manualStep : containerManualSteps) {
                HashMap<String,Object> vars = newHashMap(commonVars);
                vars.put("step", manualStep);
                InstructionStep step = new InstructionStep(manualStep, vars);
                steps.add(step);
            }
        }

        return steps;
    }
}
