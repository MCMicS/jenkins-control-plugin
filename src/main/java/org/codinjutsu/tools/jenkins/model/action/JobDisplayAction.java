package org.codinjutsu.tools.jenkins.model.action;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @see <a href="https://github.com/jenkinsci/display-url-api-plugin/blob/master/src/main/java/org/jenkinsci/plugins/displayurlapi/actions/RunDisplayAction.java">org.jenkinsci.plugins.displayurlapi.actions.JobDisplayAction</a>
 *
 * <pre>
 *     {
 *       "_class" : "org.jenkinsci.plugins.displayurlapi.actions.JobDisplayAction",
 *       "displayUrl" : "https://jenkins.mcmics.dev/blue/organizations/jenkins/DateParameter/"
 *     }
 * </pre>
 */
@Builder(toBuilder = true)
@Data
public class JobDisplayAction implements DisplayUrlAction {
    @NonNls
    private static final String TYPE_CLASS = TYPE_CLASS_PREFIX + "JobDisplayAction";

    private final @Nullable String displayUrl;
}
