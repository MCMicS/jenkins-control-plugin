package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.JenkinsTree;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JenkinsTreeCopyProviderTest {

    private final JenkinsTree tree = mock();
    private final JenkinsTreeCopyProvider copyProvider = new JenkinsTreeCopyProvider(tree::getSelectedPathComponents);

    @Test
    public void getTextLinesToCopyNoSelection() {
        when(tree.getSelectedPathComponents()).thenReturn(Stream.empty());
        final var textLinesToCopy = copyProvider.getTextLinesToCopy();
        assertThat(textLinesToCopy).isEmpty();
    }

    @Test
    public void getTextLinesToCopyNoCopyTextProvider() {
        when(tree.getSelectedPathComponents()).thenReturn(Stream.of(new DefaultMutableTreeNode("Test")));
        final var textLinesToCopy = copyProvider.getTextLinesToCopy();
        assertThat(textLinesToCopy).isEmpty();
    }

    @Test
    public void getTextLinesToCopySingleSelection() {
        when(tree.getSelectedPathComponents()).then(answer("Test"));
        final var textLinesToCopy = copyProvider.getTextLinesToCopy();
        assertThat(textLinesToCopy).hasSize(1)
                .containsOnly("Test");
    }

    @Test
    public void getTextLinesToCopy() {
        when(tree.getSelectedPathComponents()).then(answer("Test", "Second"));
        final var textLinesToCopy = copyProvider.getTextLinesToCopy();
        assertThat(textLinesToCopy)
                .hasSize(2)
                .containsOnly("Test", "Second");
    }

    private static Answer<Stream<DefaultMutableTreeNode>> answer(String... values) {
        if (values == null) {
            return invocation -> Stream.empty();
        }

        return invocation -> Arrays.stream(values).map(TestTreeNode::new);
    }

    private static class TestTreeNode extends DefaultMutableTreeNode {

        public TestTreeNode(String value) {
            super(new TestNodeObject(value));
        }
    }

    private record TestNodeObject(String value) implements CopyTextProvider {

        @Override
        public @NotNull Optional<String> getTextToCopy() {
            return Optional.ofNullable(value);
        }
    }
}
