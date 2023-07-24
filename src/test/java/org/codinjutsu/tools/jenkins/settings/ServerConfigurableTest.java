package org.codinjutsu.tools.jenkins.settings;

import com.intellij.credentialStore.CredentialStoreManager;
import com.intellij.credentialStore.ProviderType;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.impl.BasePasswordSafe;
import com.intellij.ide.passwordSafe.impl.TestPasswordSafeImpl;
import com.intellij.mock.MockApplication;
import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.logic.ConfigurationValidator;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ServerConfigurableTest {

    private static final @NotNull Disposable DO_NOTHING = () -> {
    };

    private final MockProject project = new MockProject(null, DO_NOTHING);
    private final ServerConfigurable serverConfigurable = new ServerConfigurable(project);
    private final ServerComponent serverComponent = new ServerComponent(serverSetting -> ConfigurationValidator.ValidationResult.builder().build());
    private final JenkinsAppSettings jenkinsAppSettings = new JenkinsAppSettings();
    private final JenkinsSettings jenkinsSettings = new JenkinsSettings();

    @Before
    public void setUp() {
        serverConfigurable.setServerComponent(serverComponent);

        project.registerService(JenkinsAppSettings.class, jenkinsAppSettings);
        project.registerService(JenkinsSettings.class, jenkinsSettings);

        final var application = MockApplication.setUp(DO_NOTHING);
        application.registerService(CredentialStoreManager.class, new MemoryOnlyCredentials());
        final BasePasswordSafe passwordSafe = new TestPasswordSafeImpl();
        application.registerService(PasswordSafe.class, passwordSafe);

        jenkinsAppSettings.setServerUrl("https://example.org/jenkins");
        jenkinsSettings.setJenkinsUrl("https://example.org:8443/jenkins");
        jenkinsSettings.setUsername("jenkins");
        jenkinsSettings.setPassword("<token>");
    }

    @Test
    public void getPreferredFocusedComponentWithoutServerComponent() {
        serverConfigurable.setServerComponent(null);
        final var preferred = serverConfigurable.getPreferredFocusedComponent();
        assertThat(preferred).isNull();
    }

    @Test
    public void getPreferredFocusedComponent() {
        final var preferred = serverConfigurable.getPreferredFocusedComponent();
        assertThat(preferred).isEqualTo(serverComponent.getServerUrlComponent());
    }

    @Test
    public void isUnmodifiedServerComponent() {
        serverConfigurable.reset();
        final var modified = serverConfigurable.isModified();
        assertThat(modified).isFalse();
    }

    @Test
    public void isModifiedForUnmodified() {
        final var serverSetting = createServerSetting();
        final var modified = ServerConfigurable.isModified(serverSetting, jenkinsAppSettings, jenkinsSettings);
        assertThat(modified).isFalse();
    }

    @Test
    public void isModifiedForModifiedUser() {
        final var serverSetting = createServerSettingBuilder().username("user").build();
        final var modified = ServerConfigurable.isModified(serverSetting, jenkinsAppSettings, jenkinsSettings);
        assertThat(modified).isTrue();
    }

    @Test
    public void isModifiedForModifiedToken() {
        final var serverSetting = createServerSettingBuilder().apiTokenModified(true).build();
        final var modified = ServerConfigurable.isModified(serverSetting, jenkinsAppSettings, jenkinsSettings);
        assertThat(modified).isTrue();
    }

    @Test
    public void isModifiedForTimeout() {
        final var serverSetting = createServerSettingBuilder().timeout(12).build();
        final var modified = ServerConfigurable.isModified(serverSetting, jenkinsAppSettings, jenkinsSettings);
        assertThat(modified).isTrue();
    }

    @Test
    public void isModifiedForServerUrl() {
        final var serverSetting = createServerSettingBuilder().url("https://second.jenkins.org/").build();
        final var modified = ServerConfigurable.isModified(serverSetting, jenkinsAppSettings, jenkinsSettings);
        assertThat(modified).isTrue();
    }

    @Test
    public void isModifiedForJenkinsUrl() {
        final var serverSetting = createServerSettingBuilder().jenkinsUrl("https://second.jenkins.org/").build();
        final var modified = ServerConfigurable.isModified(serverSetting, jenkinsAppSettings, jenkinsSettings);
        assertThat(modified).isTrue();
    }

    private ServerSetting createServerSetting() {
        return createServerSettingBuilder().build();
    }

    private ServerSetting.ServerSettingBuilder createServerSettingBuilder() {
        return ServerSetting.builder()
                .url(jenkinsAppSettings.getServerUrl())
                .jenkinsUrl(jenkinsSettings.getJenkinsUrl())
                .username(jenkinsSettings.getUsername())
                .apiToken(jenkinsSettings.getPassword())
                .timeout(jenkinsSettings.getConnectionTimeout())
                ;
    }

    @Test
    public void apply() throws ConfigurationException {
        final var oldServerSetting = createServerSetting();
        final var expectedServerSetting = ServerSetting.builder()
                .url("https://example.org:8443/jenkins")
                .jenkinsUrl("https://jenkins.example.org/")
                .username("user")
                .apiToken("newToken")
                .timeout(13)
                .build();
        serverComponent.setServerUrl(expectedServerSetting.getUrl());
        serverComponent.setJenkinsUrl(expectedServerSetting.getJenkinsUrl());
        serverComponent.setConnectionTimeout(expectedServerSetting.getTimeout());
        serverComponent.setUsername(expectedServerSetting.getUsername());
        serverComponent.setApiTokenValue(expectedServerSetting.getApiToken());

        serverConfigurable.apply();
        final var newServerSetting = createServerSetting();
        assertThat(newServerSetting).isNotEqualTo(oldServerSetting)
                .isEqualTo(expectedServerSetting);
    }

    @Test
    public void reset() {
        final var oldServerSetting = serverComponent.getServerSetting();
        final var expectedServerSetting = createServerSettingBuilder()
                .apiToken("")
                .build();
        serverConfigurable.reset();
        final var newServerSetting = serverComponent.getServerSetting();
        assertThat(newServerSetting).isNotEqualTo(oldServerSetting)
                .isEqualTo(expectedServerSetting);
    }

    private static class MemoryOnlyCredentials implements CredentialStoreManager {
        @Override
        public boolean isSupported(@NotNull ProviderType providerType) {
            return providerType == ProviderType.MEMORY_ONLY;
        }

        @NotNull
        @Override
        public ProviderType defaultProvider() {
            return ProviderType.MEMORY_ONLY;
        }

        @NotNull
        @Override
        public List<ProviderType> availableProviders() {
            return List.of(ProviderType.MEMORY_ONLY);
        }
    }
}
