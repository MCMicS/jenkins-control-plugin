package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchParameterRenderer implements JobParameterRenderer {

    static final JobParameterType TYPE = new JobParameterType("PatchParameterDefinition",
            "org.jenkinsci.plugins.patch.PatchParameterDefinition");

    @NotNull
    @Override
    public JobParameterComponent<VirtualFile> render(@NotNull JobParameter jobParameter, @Nullable ProjectJob projectJob) {
        return JobParameterRenderers.createFileUpload(jobParameter, jobParameter.getDefaultValue());
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return TYPE.equals(jobParameter.getJobParameterType());
    }
}
