package org.codinjutsu.tools.jenkins.view;

import lombok.Value;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.NotNull;

public interface JenkinsTreeNode {

    @NotNull
    String getUrl();

    void render(JenkinsTreeNodeVisitor treeNodeRenderer);

    @Value
    class BuildNode implements JenkinsTreeNode {

        private final Build build;

        @NotNull
        @Override
        public String getUrl() {
            return build.getUrl();
        }

        @Override
        public void render(JenkinsTreeNodeVisitor treeNodeRenderer) {
            treeNodeRenderer.visit(this);
        }
    }

    @Value
    class JobNode implements JenkinsTreeNode {

        private final Job job;

        @NotNull
        @Override
        public String getUrl() {
            return job.getUrl();
        }

        @Override
        public void render(JenkinsTreeNodeVisitor treeNodeRenderer) {
            treeNodeRenderer.visit(this);
        }
    }

    @Value
    class RootNode implements JenkinsTreeNode {

        private final Jenkins jenkins;

        @NotNull
        @Override
        public String getUrl() {
            return jenkins.getServerUrl();
        }

        @Override
        public void render(JenkinsTreeNodeVisitor treeNodeRenderer) {
            treeNodeRenderer.visit(this);
        }
    }
}
