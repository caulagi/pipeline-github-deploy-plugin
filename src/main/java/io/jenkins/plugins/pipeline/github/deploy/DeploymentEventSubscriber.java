package io.jenkins.plugins.pipeline.github.deploy;

import static com.google.common.collect.Sets.immutableEnumSet;
import static org.jenkinsci.plugins.github.util.JobInfoHelpers.withTrigger;
import static org.kohsuke.github.GHEvent.DEPLOYMENT;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import com.cloudbees.jenkins.GitHubPushTrigger;
import com.cloudbees.jenkins.GitHubRepositoryName;
import com.cloudbees.jenkins.GitHubRepositoryNameContributor;

import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;


@Extension
public class DeploymentEventSubscriber extends GHEventsSubscriber {

    @Override
    protected boolean isApplicable(Item project) {
        return withTrigger(GitHubPushTrigger.class).apply(project);
    }

    /**
     * @return set with only push event
     */
    @Override
    protected Set<GHEvent> events() {
        return immutableEnumSet(DEPLOYMENT);
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

        // run in high privilege to see all the projects anonymous users don't see.
        // this is safe because when we actually schedule a build, it's a build that can
        // happen at some random time anyway.
        try (ACLContext _AclContext = ACL.as(ACL.SYSTEM)) {
            for (WorkflowJob job : Jenkins.get().getAllItems(WorkflowJob.class)) {
                String fullDisplayName = job.getFullDisplayName();
                LOGGER.info("Considering to poke {}", fullDisplayName);
                if (GitHubRepositoryNameContributor.parseAssociatedNames(job.asItem()).contains(changedRepository)) {
                    LOGGER.info("Poked {}", fullDisplayName);
                    DeploymentTrigger t = new DeploymentTrigger(deploymentEvent.getDeployment());
                    t.start(job, false);
                    t.onPost(DeploymentTriggerEvent.create().withTimestamp(event.getTimestamp()).withOrigin(event.getOrigin()).withTriggeredByUser("caulagi").build());
                } else {
                    LOGGER.debug("Skipped {} because it doesn't have a matching repository.", fullDisplayName);
                }

            }

        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentEventSubscriber.class);
}
