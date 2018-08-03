package io.jenkins.plugins.pipeline.github.deploy;

import static com.google.common.collect.Sets.immutableEnumSet;
import static org.jenkinsci.plugins.github.util.JobInfoHelpers.withTrigger;
import static org.kohsuke.github.GHEvent.DEPLOYMENT;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import com.cloudbees.jenkins.GitHubPushTrigger;
import com.cloudbees.jenkins.GitHubRepositoryName;

import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHEventPayload.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;

/**
 * 
 */
@Extension
public class DeploymentEventSubscriber extends GHEventsSubscriber {

    @Override
    protected boolean isApplicable(Item project) {
        LOGGER.error("5555555 566sdf7asdjföiasjdflkas fdas fasdkfjlaskdfj sdf");
        return withTrigger(GitHubPushTrigger.class).apply(project);
    }

    /**
     * @return set with only push event
     */
    @Override
    protected Set<GHEvent> events() {
        return immutableEnumSet(DEPLOYMENT);
    }

    public static <T extends Trigger> T triggerFrom(Item item, Class<T> tClass) {
        if (item instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) item;
            LOGGER.info("pjob:", pJob);

            for (Object candidate : pJob.getTriggers().values()) {
                LOGGER.info("candidate: {}", candidate);
                if (tClass.isInstance(candidate)) {
                    return tClass.cast(candidate);
                }
            }
        }
        return null;
    }

    @Override
    protected void onEvent(final GHSubscriberEvent event) {
        final GHEventPayload.Deployment deploymentEvent;

        try {
            deploymentEvent = GitHub.offline().parseEventPayload(new StringReader(event.getPayload()),
                    GHEventPayload.Deployment.class);
        } catch (IOException e) {
            LOGGER.warn("Received malformed Deployment: " + event.getPayload(), e);
            return;
        }
        String repoUrl = deploymentEvent.getDeployment().getRepositoryUrl().toExternalForm();
        LOGGER.info("Received Deployment for {} from {}", repoUrl, event.getOrigin());

        // repoUrl is of the form https://api.github.com/repos/starstableent/docker-test
        // change it to https://github.com/starstableent/docker-test to match expected
        // patterns in GitHubRepositoryName
        // TODO: open a bug against github-plugin
        final GitHubRepositoryName changedRepository = GitHubRepositoryName
                .create(repoUrl.replace("api.github.com/repos", "github.com"));

        if (changedRepository == null) {
            LOGGER.warn("Malformed repo url {}", repoUrl);
            return;
        }

        DeploymentTrigger.DescriptorImpl triggerDescriptor = (DeploymentTrigger.DescriptorImpl) Jenkins.get()
                .getDescriptor(DeploymentTrigger.class);
        LOGGER.info("......... DESCI: {}", triggerDescriptor);

        if (triggerDescriptor == null) {
            LOGGER.error("Unable to find DeploymentTrigger, this shouldn't happen.");
            return;
        }

        // lookup job
        WorkflowJob job = triggerDescriptor.getJob(changedRepository);
        LOGGER.info(".... JOB: {}", job);
       

        if (job == null) {
            LOGGER.debug("No job found matching key: {}", changedRepository);
        } else {
            java.util.Optional<DeploymentTrigger> matchingTrigger = job.getTriggersJobProperty()
                    .getTriggers()
                    .stream()
                    .filter(t -> t instanceof DeploymentTrigger)
                    .map(DeploymentTrigger.class::cast)
                    .filter(t -> triggerMatches(t, deploymentEvent, job))
                    .findAny();
        }


       
            // run in high privilege to see all the projects anonymous users don't see.
            // this is safe because when we actually schedule a build, it's a build that can
            // happen at some random time anyway.
            try (ACLContext _AclContext = ACL.as(ACL.SYSTEM)) {
                for (Item job1 : Jenkins.get().getAllItems(Item.class)) {
                    LOGGER.info("job: {}", job1);
                    if (job1 instanceof ParameterizedJobMixIn.ParameterizedJob) {
                        LOGGER.info("TRUE");
                        Trigger t = triggerFrom(job1, SCMTrigger.class);
                        LOGGER.info("TRIG: {}", t);
                    }
                    
                    /*
                    DeploymentTrigger trigger = new DeploymentTrigger();
                    String fullDisplayName = job.getFullDisplayName();
                    LOGGER.info("Considering to poke {}", fullDisplayName);
                    if (GitHubRepositoryNameContributor.parseAssociatedNames(job).contains(changedRepository)) {
                        LOGGER.info("Poked {}", fullDisplayName);
                        trigger.onPost(DeploymentTriggerEvent.create().withTimestamp(event.getTimestamp())
                                .withOrigin(event.getOrigin()).withTriggeredByUser("caulagi").build());
                    } else {
                        LOGGER.debug("Skipped {} because it doesn't have a matching repository.", fullDisplayName);
                    }
                    */
                }

            }
        
    }

    private boolean triggerMatches(DeploymentTrigger t, Deployment deploymentEvent, WorkflowJob job) {
        LOGGER.info("...... triggerMatches ++++++++++ ");
        LOGGER.info(toString());
        LOGGER.info(deploymentEvent.toString());
        LOGGER.info(job.toString());
		return false;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentEventSubscriber.class);
}
