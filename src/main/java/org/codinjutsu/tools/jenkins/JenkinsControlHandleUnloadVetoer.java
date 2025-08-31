package org.codinjutsu.tools.jenkins;

import com.intellij.ide.plugins.DynamicPluginVetoer;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JenkinsControlHandleUnloadVetoer implements DynamicPluginVetoer {
    private static final Logger LOG = Logger.getInstance(JenkinsTree.class);

    @Override
    public @Nullable String vetoPluginUnload(@NotNull IdeaPluginDescriptor ideaPluginDescriptor) {
        if (isJenkinsPlugin(ideaPluginDescriptor)) {
            LOG.debug("check if Jenkins Control plugin could be unloaded");
        }
        return null;
    }

    private static boolean isJenkinsPlugin(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        return pluginDescriptor.getPluginId().equals(PluginId.getId(Version.PLUGIN_ID));
    }
}
