package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.project.Project;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.picocontainer.PicoContainer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationValidatorTest {

    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String WRONG_HOST_SERVER_URL = "http://127.0.0.1:8080/";
    private static final String WRONG_PORT_SERVER_URL = "http://localhost:8081/";
    private static final String WRONG_PROTOCOL_SERVER_URL = "https://localhost:8080/";
    private static final String WRONG_PATH_SERVER_URL = "http://localhost:8080/jenkins";
    private final String configuredServerUrl = SERVER_URL;
    private final Project project = mockProject();
    private final ConfigurationValidator configurationValidator = new ConfigurationValidator(project);

    @Test
    public void validateInvalidPort() {
        final String expectedMessage = "Jenkins Server Port Mismatch: expected='8080' - actual='8081'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuredServerUrl, WRONG_PORT_SERVER_URL);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidProtcol() {
        final String expectedMessage = "Jenkins Server Protocol Mismatch: expected='http' - actual='https'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuredServerUrl, WRONG_PROTOCOL_SERVER_URL);
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidPortIfNoPortIsExplicitSet() {
        String expectedMessage = "Jenkins Server Port Mismatch: expected='8080' - actual='unset'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuredServerUrl, "http://localhost/");
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);

        expectedMessage = "Jenkins Server Port Mismatch: expected='unset' - actual='8080'. Look at the value of 'Jenkins URL' at http://localhost/configure";
        validationResult = configurationValidator.validate("http://localhost/", "http://localhost:8080/");
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidUrl() {
        final String expectedMessage = "Jenkins Server Host Mismatch: expected='localhost' - actual='127.0.0.1'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuredServerUrl, WRONG_HOST_SERVER_URL);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidPath() {
        final String expectedMessage = "Jenkins Server Path Mismatch: expected='/' - actual='/jenkins'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuredServerUrl, WRONG_PATH_SERVER_URL);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateTrailingSlash() {
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate("http://localhost:8080", "http://localhost:8080/");
        Assertions.assertThat(validationResult.isValid()).isTrue();
    }

    @Test
    public void validateMultipleErrors() {
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuredServerUrl, "https://127.0.0.1:8081/jenkins");

        final String wrongHostMessage = "Jenkins Server Host Mismatch: expected='localhost' - actual='127.0.0.1'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final String wrongPortMessage = "Jenkins Server Port Mismatch: expected='8080' - actual='8081'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final String wrongProtocolMessage = "Jenkins Server Protocol Mismatch: expected='http' - actual='https'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final String wrongPathMessage = "Jenkins Server Path Mismatch: expected='/' - actual='/jenkins'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";

        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).hasSize(4);
        Assertions.assertThat(validationResult.getErrors()).containsOnly(wrongHostMessage, wrongPortMessage,
                wrongProtocolMessage, wrongPathMessage);
    }

    @Test
    public void validate() {
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuredServerUrl, SERVER_URL);
        Assertions.assertThat(validationResult.isValid()).isTrue();
    }

    private static Project mockProject() {
        final Project project = mock(Project.class);
        final PicoContainer container = mock(PicoContainer.class);
        when(project.getPicoContainer()).thenReturn(container);
        when(container.getComponentInstance(UrlBuilder.class.getName())).thenReturn(new UrlBuilder());
        return project;
    }
}
