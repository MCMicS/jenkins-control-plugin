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
 *       "_class" : "org.jenkinsci.plugins.displayurlapi.actions.RunDisplayAction",
 *       "artifactsUrl" : "https://jenkins.mcmics.dev/blue/organizations/jenkins/DateParameter/detail/DateParameter/21/artifacts",
 *       "changesUrl" : "https://jenkins.mcmics.dev/blue/organizations/jenkins/DateParameter/detail/DateParameter/21/changes",
 *       "displayUrl" : "https://jenkins.mcmics.dev/blue/organizations/jenkins/DateParameter/detail/DateParameter/21/",
 *       "testsUrl" : "https://jenkins.mcmics.dev/blue/organizations/jenkins/DateParameter/detail/DateParameter/21/tests"
 *     }
 * </pre>
 */
@Builder(toBuilder = true)
@Data
public class RunDisplayAction implements DisplayUrlAction {
    @NonNls
    private static final String TYPE_CLASS = TYPE_CLASS_PREFIX + "RunDisplayAction";

    private final @Nullable String artifactsUrl;
    private final @Nullable String changesUrl;
    private final @Nullable String displayUrl;
    private final @Nullable String testsUrl;
}
