package org.jenkinsci.plugins.pipeline.github.deploy;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import hudson.model.Cause;

public class DeploymentCause extends Cause {

    private String deploymentBy;

    public DeploymentCause(String deploymentBy) {
        this.deploymentBy = deploymentBy;
    }

    @Override
    public String getShortDescription() {
        return format("Started by GitHub deployment from %s", trimToEmpty(deploymentBy));
    }
}
