package org.codinjutsu.tools.jenkins.view.extension;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.view.parameter.JobParameterComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.Optional;

public interface JobParameterRenderer {

    ExtensionPointName<JobParameterRenderer> EP_NAME = ExtensionPointName.create("Jenkins Control Plugin.buildParameterRenderer");

    @NotNull
    static Optional<JobParameterRenderer> findRenderer(@NotNull JobParameter jobParameter) {
        return JobParameterRenderer.EP_NAME.extensions()
                .filter(jobParameterRenderer -> jobParameterRenderer.isForJobParameter(jobParameter))
                .findFirst();
    }

    /**
     * If non-empty Optional return then a label will be shown.
     */
    @Nonnull
    default Optional<JLabel> createLabel(@NotNull JobParameter jobParameter) {
        final String name = jobParameter.getName();
        final JLabel label;
        if (StringUtils.isEmpty(name)) {
            label = JobParameterRenderers.createErrorLabel(JobParameterRenderers.MISSING_NAME_LABEL);
        } else {
            label = new JLabel(name);
        }
        //label.setHorizontalAlignment(SwingConstants.TRAILING);
        return Optional.of(label);
    }

    @SuppressWarnings({"rawtypes", "java:S3740"})
    @NotNull
    JobParameterComponent render(@NotNull JobParameter jobParameter);

    boolean isForJobParameter(@NotNull JobParameter jobParameter);

}
