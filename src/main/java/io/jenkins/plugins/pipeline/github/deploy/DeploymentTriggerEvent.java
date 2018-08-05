package io.jenkins.plugins.pipeline.github.deploy;

import javax.servlet.http.HttpServletRequest;
import jenkins.scm.api.SCMEvent;


public class DeploymentTriggerEvent {

    /**
     * The timestamp of the event (or if unavailable when the event was received)
     */
    private final long timestamp;
    /**
     * The origin of the event (see {@link SCMEvent#originOf(HttpServletRequest)})
     */
    private final String origin;
    /**
     * The user that the event was provided by.
     */
    private final String triggeredByUser;

    private DeploymentTriggerEvent(long timestamp, String origin, String triggeredByUser) {
        this.timestamp = timestamp;
        this.origin = origin;
        this.triggeredByUser = triggeredByUser;
    }

    public static Builder create() {
        return new Builder();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTriggeredByUser() {
        return triggeredByUser;
    }

    @Override
    public String toString() {
        return "DeploymentTriggerEvent{"
                + "timestamp=" + timestamp
                + ", origin='" + origin + '\''
                + ", triggeredByUser='" + triggeredByUser + '\''
                + '}';
    }

    /**
     * Builder for {@link DeploymentTriggerEvent} instances..
     */
    public static class Builder {
        private long timestamp;
        private String origin;
        private String triggeredByUser;

        private Builder() {
            timestamp = System.currentTimeMillis();
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withOrigin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder withTriggeredByUser(String triggeredByUser) {
            this.triggeredByUser = triggeredByUser;
            return this;
        }

        public DeploymentTriggerEvent build() {
            return new DeploymentTriggerEvent(timestamp, origin, triggeredByUser);
        }

        @Override
        public String toString() {
            return "DeploymentTriggerEvent.Builder{"
                    + "timestamp=" + timestamp
                    + ", origin='" + origin + '\''
                    + ", triggeredByUser='" + triggeredByUser + '\''
                    + '}';
        }
    }
}
