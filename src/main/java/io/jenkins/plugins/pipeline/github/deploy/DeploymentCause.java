package io.jenkins.plugins.pipeline.github.deploy;

import static java.lang.String.format;

import org.kohsuke.github.GHEventPayload;

import hudson.model.Cause;

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
}
