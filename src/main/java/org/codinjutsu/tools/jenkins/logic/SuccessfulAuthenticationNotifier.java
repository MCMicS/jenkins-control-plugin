/**
 * Created by marcin on 06.10.15.
 */
package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import org.codinjutsu.tools.jenkins.model.Jenkins;

public interface SuccessfulAuthenticationNotifier {
    Topic<SuccessfulAuthenticationNotifier> USER_LOGGED_IN = Topic.create("User Logged In", SuccessfulAuthenticationNotifier.class);

    void afterLogin(Jenkins jenkinsWorkspace);

    void loginCancelled();

}
