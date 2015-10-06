/**
 * Created by marcin on 06.10.15.
 */
package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface LoginActionNotifier {
    Topic<LoginActionNotifier> USER_LOGGED_IN = Topic.create("User Logged In", LoginActionNotifier.class);

    void afterLogin(Project project);
}
