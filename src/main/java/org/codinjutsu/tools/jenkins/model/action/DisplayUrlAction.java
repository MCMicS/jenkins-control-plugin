package org.codinjutsu.tools.jenkins.model.action;

import org.jetbrains.annotations.NonNls;

/**
 * It is also possible to call URLs with following suffix to get redirected: `/display/redirect/`.
 * But we read configured URL to prevent unnecessary redirects or missing Urls error.
 *
 */
public interface DisplayUrlAction extends Action {

    @NonNls
    String TYPE_CLASS_PREFIX = "org.jenkinsci.plugins.displayurlapi.actions.";

}
