package org.codinjutsu.tools.jenkins;

import com.intellij.ide.plugins.CannotUnloadPluginException;
import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

public class JenkinsControlHandleUnloadVetoer implements DynamicPluginListener {
    private static final Logger LOG = Logger.getInstance(JenkinsTree.class);

    @Override
    public void checkUnloadPlugin(@NotNull IdeaPluginDescriptor pluginDescriptor) throws CannotUnloadPluginException {
        if (isJenkinsPlugin(pluginDescriptor)) {
            LOG.debug("check if Jenkins Control plugin could be unloaded");
        }
    }

    private static boolean isJenkinsPlugin(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        return pluginDescriptor.getPluginId().equals(PluginId.getId(Version.PLUGIN_ID));
    }
}
