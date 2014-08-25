package ext.deployit.community.plugin.manualstep.contrib;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.Environment;

import ext.deployit.community.plugin.manualstep.ci.ContributorType;
import ext.deployit.community.plugin.manualstep.ci.ManualStep;
import ext.deployit.community.plugin.manualstep.ci.ManualSteps;
import ext.deployit.community.plugin.manualstep.step.InstructionStep;
import ext.deployit.community.plugin.manualstep.util.Util;

public class ManualStepContributor {

    @SuppressWarnings("rawtypes")
	@Contributor
    public void triggerManualSteps(Deltas deltas, DeploymentPlanningContext ctx) {
        Operation operation = determineDeploymentOperation(deltas);
        Map<String, Object> commonVars = newHashMap();
        commonVars.put("deltas", deltas);
        commonVars.put("deployedApplication", ctx.getDeployedApplication());
        commonVars.put("operation", operation);

        Environment environment = ctx.getDeployedApplication().getEnvironment();
        Iterable<ManualStep> manualSteps = ManualSteps.getSteps(environment, ContributorType.EVERY_SUBPLAN, operation);
        for (ManualStep manualStep : manualSteps) {
            Map<String,Object> vars = newHashMap(commonVars);
            vars.put("step",manualStep);
            InstructionStep step = new InstructionStep(manualStep, vars);
            ctx.addStep(step);
        }
        
        Map<String, Container> deployedContainers = newHashMap();
        Set<Deployed> deployeds = ctx.getDeployedApplication().getDeployeds();
        for (Deployed deployed : deployeds)	{
        	Container container = deployed.getContainer();
        	String containerIdentifier = Util.getContainerHostName(deployed.getContainer()) + "-" + container.getName();
        	
        	if (deployedContainers.get(containerIdentifier) == null)
        		deployedContainers.put(containerIdentifier, container);
        }
        
        Iterator<Entry<String, Container>> it = deployedContainers.entrySet().iterator();
        while (it.hasNext())	{
        	Map.Entry<String, Container> uniqueDeployedContainerPair = (Map.Entry<String, Container>) it.next();
        	Container uniqueDeployedContainer = uniqueDeployedContainerPair.getValue();
            Iterable<ManualStep> containerManualSteps = ManualSteps.getSteps(uniqueDeployedContainer, ContributorType.EVERY_SUBPLAN, operation);
            for (ManualStep manualStep : containerManualSteps) {
                HashMap<String,Object> vars = newHashMap(commonVars);
                vars.put("step", manualStep);
                InstructionStep step = new InstructionStep(manualStep, vars);
                ctx.addStep(step);
            }
        }
    }

    private Operation determineDeploymentOperation(Deltas deltas) {
        Operation operation = Operation.MODIFY;
        int size = deltas.getDeltas().size();
        if(numberOfDeltasForOperation(deltas, Operation.CREATE) == size) {
            operation = Operation.CREATE;
        } else if(numberOfDeltasForOperation(deltas, Operation.DESTROY) == size) {
            operation = Operation.DESTROY;
        }  else if(numberOfDeltasForOperation(deltas, Operation.NOOP) == size) {
            operation = Operation.NOOP;
        }
        return operation;
    }

    private int numberOfDeltasForOperation(Deltas deltas, final Operation operation) {
        return Iterables.size(filter(deltas.getDeltas(), new Predicate<Delta>() {
            @Override
            public boolean apply(Delta input) {
                return input.getOperation() == operation;
            }
        }));

    }
}
