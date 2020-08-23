package org.codinjutsu.tools.jenkins.view;

import com.intellij.ide.AboutPopupDescriptionProvider;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import org.codinjutsu.tools.jenkins.Version;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class JenkinsAbout implements AboutPopupDescriptionProvider {

    @Nullable
    @Override
    public String getDescription() {
        return Optional.ofNullable(PluginManagerCore.getPlugin(PluginId.getId(Version.PLUGIN_ID)))
                .map(PluginDescriptor::getVersion)
                .map(version -> String.format("%s %s", Version.PLUGIN_NAME, version))
                .orElse(null);
    }
}
