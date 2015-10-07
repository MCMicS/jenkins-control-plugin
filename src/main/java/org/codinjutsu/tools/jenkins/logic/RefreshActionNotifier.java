/**
 * Created by marcin on 06.10.15.
 */
package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface RefreshActionNotifier {
    Topic<RefreshActionNotifier> USER_LOGGED_IN = Topic.create("User Logged In", RefreshActionNotifier.class);

    void afterLogin(Project project);
}
