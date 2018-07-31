package org.jenkinsci.plugins.pipeline.github.deploy;

import static java.lang.String.format;

import org.jenkinsci.Symbol;
import org.kohsuke.github.GHEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.triggers.TriggerDescriptor;

public class DeploymentCause extends Cause {

    private GHEventPayload.Deployment deploymentEvent;

    public DeploymentCause(GHEventPayload.Deployment deploymentEvent) {
        this.deploymentEvent = deploymentEvent;
    }

    @Override
    public String getShortDescription() {
        return format("Started by GitHub deployment for repo:%s, branch:%s", deploymentEvent.getRepository().getName(),
                deploymentEvent.getDeployment().getRef());
    }

    @Symbol("githubDeployment")
    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            LOGGER.info("TRIGGER: {}", item);
            return false;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentEventSubscriber.class);
}
