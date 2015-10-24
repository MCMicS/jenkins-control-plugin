package org.codinjutsu.tools.jenkins.logic;

import com.intellij.util.messages.Topic;
import org.codinjutsu.tools.jenkins.model.Jenkins;

public interface AuthenticationNotifier {
    Topic<AuthenticationNotifier> USER_LOGGED_IN = Topic.create("User Logged In", AuthenticationNotifier.class);

    void emptyConfiguration();

    void afterLogin(Jenkins jenkinsWorkspace);

    void loginCancelled();

}
