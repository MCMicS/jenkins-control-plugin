package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import javax.swing.*;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.codinjutsu.tools.jenkins.view.parameter.NodeParameterRenderer.NODE_PARAMETER;

public class NodeParameterRendererTest implements JobParameterTest {

    private final NodeParameterRenderer jobParameterRenderer = new NodeParameterRenderer();
    private final JobParameterType labelParameter = new JobParameterType("LabelParameterDefinition",
            "org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition");
    private final JobParameterType nodeParameter = NODE_PARAMETER;

    @Test
    public void renderLabel() {
        final JobParameter jobParameter = createJobParameter(labelParameter);
        JobParameterComponent jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void renderNode() {
        final JobParameter jobParameter = createJobParameter(nodeParameter, "Test", "default", "master");
        JobParameterComponent jobParameterComponent = jobParameterRenderer.render(jobParameter);
        final JComponent viewElement = jobParameterComponent.getViewElement();
        assertThat(viewElement).isInstanceOf(JComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);
        @SuppressWarnings("unchecked")
        final JComboBox<String> nodes = (JComboBox<String>) viewElement;
        assertThat(nodes.getSelectedItem()).isEqualTo("default");
        assertThat(nodes.getItemCount()).isEqualTo(3);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(labelParameter)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(nodeParameter)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType("ParameterSeparatorDefinition",
                "org.jvnet.jenkins.plugins.nodelabelparameter.invalid.LabelParameterDefinition"))))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition)))
                .isFalse();
    }
}
