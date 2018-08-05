package io.jenkins.plugins.pipeline.github.deploy;

import org.kohsuke.github.GHDeployment;

import hudson.model.Cause;

public class DeploymentCause extends Cause {

    private GHDeployment deployment;

    public DeploymentCause(GHDeployment deployment) {
        this.deployment = deployment;
    }

    @Override
    public String getShortDescription() {
        return deployment.toString();
    }
}
