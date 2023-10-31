package org.codinjutsu.tools.jenkins.view;

import com.intellij.ide.CopyProvider;
import com.intellij.ide.TextCopyProvider;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.DataContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Delegate;
import org.codinjutsu.tools.jenkins.JenkinsTree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;
import java.util.stream.Collectors;

@Value
@Getter(AccessLevel.NONE)
public class JenkinsTreeCopyProvider implements CopyProvider {
    private final JenkinsTree tree;
    @Delegate
    private final CopyProvider textCopyProvider = new TextCopyProvider() {
        @Override
        public @NotNull Collection<String> getTextLinesToCopy() {
            return JenkinsTreeCopyProvider.this.getTextLinesToCopy();
        }

        @Override
        public boolean isCopyEnabled(@NotNull DataContext dataContext) {
            final var textLinesToCopy = this.getTextLinesToCopy();
            return !textLinesToCopy.isEmpty();
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    };

    @NotNull Collection<String> getTextLinesToCopy() {
        return tree.getSelectedPathComponents()
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(CopyTextProvider.class::isInstance)
                .map(CopyTextProvider.class::cast)
                .map(CopyTextProvider::getTextLinesToCopy)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
