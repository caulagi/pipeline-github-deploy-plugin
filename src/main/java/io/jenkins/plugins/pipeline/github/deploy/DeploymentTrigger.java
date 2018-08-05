package io.jenkins.plugins.pipeline.github.deploy;

import static org.jenkinsci.plugins.github.util.JobInfoHelpers.asParameterizedJobMixIn;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.github.GHDeployment;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.NamingThreadFactory;
import hudson.util.SequentialExecutionQueue;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem.SCMTriggerItems;


public class DeploymentTrigger extends Trigger<WorkflowJob> {

    private GHDeployment deployment;

    @DataBoundConstructor
    public DeploymentTrigger(GHDeployment dep) {
        deployment = dep;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public void onPost(final DeploymentTriggerEvent event) {
        DescriptorImpl d = getDescriptor();
        d.queue.execute(new Runnable() {
            @Override
            public void run() {
                DeploymentCause cause = new DeploymentCause(deployment);
                asParameterizedJobMixIn(job).scheduleBuild(cause);

            }
        });
    }

    public GHDeployment getDeployment() {
        return deployment;
    }

    @Extension
    @Symbol("githubDeployment")
    public static class DescriptorImpl extends TriggerDescriptor {
        private final transient SequentialExecutionQueue queue = new SequentialExecutionQueue(
                Executors.newSingleThreadExecutor(threadFactory()));

        private static ThreadFactory threadFactory() {
            return new NamingThreadFactory(Executors.defaultThreadFactory(), "DeploymentTrigger");
        }

        @Override
        public boolean isApplicable(Item item) {
            // TODO
            return item instanceof Job && SCMTriggerItems.asSCMTriggerItem(item) != null
                    && item instanceof ParameterizedJobMixIn.ParameterizedJob;
        }

        @Override
        public String getDisplayName() {
            return "Deployment trigger for github";
        }

    }

}
