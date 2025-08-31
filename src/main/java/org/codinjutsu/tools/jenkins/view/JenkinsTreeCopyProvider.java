package org.codinjutsu.tools.jenkins.view;

import com.intellij.ide.CopyProvider;
import com.intellij.ide.TextCopyProvider;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.DataContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@Getter(AccessLevel.NONE)
public class JenkinsTreeCopyProvider implements CopyProvider {
    private final Supplier<Stream<DefaultMutableTreeNode>> treeNodeProvider;
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
        return treeNodeProvider.get()
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(CopyTextProvider.class::isInstance)
                .map(CopyTextProvider.class::cast)
                .map(CopyTextProvider::getTextLinesToCopy)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
