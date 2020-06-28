package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.project.Project;
import org.assertj.core.api.Assertions;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Optional;

public class ConfigurationValidatorTest {

    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String WRONG_HOST_SERVER_URL = "http://127.0.0.1:8080/";
    private static final String WRONG_PORT_SERVER_URL = "http://localhost:8081/";
    private static final String WRONG_PROTOCOL_SERVER_URL = "https://localhost:8080/";
    private static final String WRONG_PATH_SERVER_URL = "http://localhost:8080/jenkins";
    private final JenkinsAppSettings configuration = new JenkinsAppSettings();
    private final Project project = Mockito.mock(Project.class);
    private final ConfigurationValidator configurationValidator = new ConfigurationValidator(project);

    @Test
    public void validateInvalidPort() {
        Jenkins jenkins = new Jenkins("", WRONG_PORT_SERVER_URL);
        final String expectedMessage = "Jenkins Server Port Mismatch: expected='8080' - actual='8081'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuration, jenkins);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidProtcol() {
        Jenkins jenkins = new Jenkins("", WRONG_PROTOCOL_SERVER_URL);
        final String expectedMessage = "Jenkins Server Protocol Mismatch: expected='http' - actual='https'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuration, jenkins);
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidPortIfNoPortIsExplicitSet() {
        Jenkins jenkins = new Jenkins("", "http://localhost/");
        String expectedMessage = "Jenkins Server Port Mismatch: expected='8080' - actual='unset'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuration, jenkins);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);

        configuration.setServerUrl("http://localhost/");
        jenkins = new Jenkins("", "http://localhost:8080/");
        expectedMessage = "Jenkins Server Port Mismatch: expected='unset' - actual='8080'. Look at the value of 'Jenkins URL' at http://localhost/configure";
        validationResult = configurationValidator.validate(configuration, jenkins);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidUrl() {
        Jenkins jenkins = new Jenkins("", WRONG_HOST_SERVER_URL);
        final String expectedMessage = "Jenkins Server Host Mismatch: expected='localhost' - actual='127.0.0.1'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuration, jenkins);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateInvalidPath() {
        Jenkins jenkins = new Jenkins("", WRONG_PATH_SERVER_URL);
        final String expectedMessage = "Jenkins Server Path Mismatch: expected='/' - actual='/jenkins'. Look at the value of 'Jenkins URL' at http://localhost:8080/configure";
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuration, jenkins);
        Assertions.assertThat(validationResult.isValid()).isFalse();
        Assertions.assertThat(validationResult.getErrors()).containsOnly(expectedMessage);
    }

    @Test
    public void validateMultipleErrors() {
        Jenkins jenkins = new Jenkins("", "https://127.0.0.1:8081/jenkins");
        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuration, jenkins);

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
        Jenkins jenkins = new Jenkins("", SERVER_URL);

        final ConfigurationValidator.ValidationResult validationResult = configurationValidator.validate(configuration, jenkins);
        Assertions.assertThat(validationResult.isValid()).isTrue();
    }

    @Before
    public void setUp() {
        configuration.setServerUrl(SERVER_URL);
    }
}
