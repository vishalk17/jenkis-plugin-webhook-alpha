import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.util.logging.Logger;

@Extension
public class GitWebhookTrigger extends Trigger<WorkflowJob> {

    private static final Logger LOGGER = Logger.getLogger(GitWebhookTrigger.class.getName());

    @Override
    public void run() {
        try {
            // Get the Jenkins job associated with this trigger
            WorkflowJob job = this.job;

            if (job == null) {
                LOGGER.warning("No associated Jenkins job found.");
                return;
            }

            // Check if the 'is_build_needed' environment variable is set to 'y'
            if (isBuildNeeded(job)) {
                // Trigger the Jenkins job
                triggerJob(job);
            } else {
                LOGGER.info("Job '" + job.getName() + "' will not be triggered as 'is_build_needed' is not set.");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to trigger Jenkins job due to exception", e);
        }
    }

    private boolean isBuildNeeded(WorkflowJob job) {
        // Check if the 'is_build_needed' environment variable is set to 'y' for the specified Jenkins job
        try {
            String isBuildNeeded = job.getLastBuild().getEnvironment().get("is_build_needed");
            return "y".equals(isBuildNeeded);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error checking 'is_build_needed' environment variable: " + e.getMessage(), e);
            return false;
        }
    }

    private void triggerJob(WorkflowJob job) {
        // Trigger the Jenkins job
        try {
            LOGGER.info("Triggering job: " + job.getName());
            WorkflowRun run = job.scheduleBuild2(0);
            LOGGER.info("Job triggered successfully. Build number: " + run.getNumber());
        } catch (IOException e) {
            LOGGER.error("Failed to trigger Jenkins job due to exception", e);
        }
    }

    @Override
    public TriggerDescriptor getDescriptor() {
        return new GitWebhookTriggerDescriptor();
    }

    public static class GitWebhookTriggerDescriptor extends TriggerDescriptor {

        @Override
        public String getDisplayName() {
            return "Git Webhook Trigger";
        }

        @Override
        public boolean isApplicable(ItemGroup itemGroup) {
            return itemGroup instanceof TopLevelItem;
        }
    }
}
