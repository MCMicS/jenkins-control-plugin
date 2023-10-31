package org.codinjutsu.tools.jenkins.view;

import com.intellij.ide.util.treeView.NodeDescriptorProvidingKey;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildParameter;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface JenkinsTreeNode extends NodeDescriptorProvidingKey, NavigationItem, CopyTextProvider {

    @NotNull
    String getUrl();

    @NotNull
    @Override
    default NavigationItem getKey() {
        return this;
    }

    @Nullable
    @Override
    default String getName() {
        return getUrl();
    }

    @Nullable
    @Override
    default ItemPresentation getPresentation() {
        return null;
    }

    @Override
    default void navigate(boolean requestFocus) {
        // do nothing
    }

    @Override
    default boolean canNavigate() {
        return false;
    }

    @Override
    default boolean canNavigateToSource() {
        return false;
    }

    void render(JenkinsTreeNodeVisitor treeNodeRenderer);

    @Override
    default @NotNull Optional<String> getTextToCopy() {
        return Optional.empty();
    }

    record BuildParameterNode(@NotNull BuildParameter buildParameter) implements JenkinsTreeNode {
        @NotNull
        @Override
        public String getUrl() {
            return buildParameter.getBuildUrl();
        }

        @NotNull
        @Override
        public String getName() {
            return buildParameter.getName();
        }

        @Override
        public void render(JenkinsTreeNodeVisitor treeNodeRenderer) {
            treeNodeRenderer.visit(this);
        }

        @Override
        public @NotNull Optional<String> getTextToCopy() {
            return Optional.of(buildParameter.getNameToRender());
        }
    }

    record BuildNode(Build build) implements JenkinsTreeNode {

        @NotNull
        @Override
        public String getUrl() {
            return build.getUrl();
        }

        @Override
        public void render(JenkinsTreeNodeVisitor treeNodeRenderer) {
            treeNodeRenderer.visit(this);
        }

        @Override
        public @NotNull Optional<String> getTextToCopy() {
            return Optional.of(build.getNameToRenderWithDuration());
        }
    }

    record JobNode(Job job) implements JenkinsTreeNode {

        @NotNull
        @Override
        public String getUrl() {
            return job.getUrl();
        }

        @Override
        public void render(JenkinsTreeNodeVisitor treeNodeRenderer) {
            treeNodeRenderer.visit(this);
        }

        @Override
        public @NotNull Optional<String> getTextToCopy() {
            return Optional.of(job.getNameToRenderSingleJob());
        }
    }

    record RootNode(Jenkins jenkins) implements JenkinsTreeNode {

        @NotNull
        @Override
        public String getUrl() {
            return jenkins.getServerUrl();
        }

        @Override
        public void render(JenkinsTreeNodeVisitor treeNodeRenderer) {
            treeNodeRenderer.visit(this);
        }

        @Override
        public @NotNull Optional<String> getTextToCopy() {
            return Optional.of(jenkins.getNameToRender());
        }
    }
}
