package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.StatusText;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PasswordComponentTest {

    private final PasswordComponent.Model model = new PasswordComponent.Model();

    private final JBPasswordField passwordField = mock(JBPasswordField.class);
    private final Document document = mock(Document.class);

    private final StatusText statusText = mock(StatusText.class);

    private final JButton changePasswordButton = mock(JButton.class);

    private final PasswordComponent.View view = mock(PasswordComponent.View.class, Answers.RETURNS_DEEP_STUBS);

    private final PasswordComponent passwordComponent = new PasswordComponent(model, view);

    @Test
    public void checkInitiallyBehaviour() {
        passwordComponent.init();

        assertThat(model.getPassword()).isNull();
        //verify(statusText).setText("use Default Value");
        verify(passwordField).setEnabled(false);
        verify(passwordField).setVisible(false);
        verify(changePasswordButton).setVisible(true);
    }

    @Test
    public void modelIsUpdatedAfterChangePassword() {
        passwordComponent.init();
        final ArgumentCaptor<ActionListener> onClickButton = ArgumentCaptor.forClass(ActionListener.class);
        final ArgumentCaptor<ActionListener> onChangePassword = ArgumentCaptor.forClass(ActionListener.class);
        final String newPassword = "Sample";
        when(passwordField.getPassword()).thenReturn(newPassword.toCharArray());

        verify(passwordField).addActionListener(onChangePassword.capture());
        verify(changePasswordButton).addActionListener(onClickButton.capture());
        final ActionEvent dummyEvent = new ActionEvent(this, 0, "dummy");
        onClickButton.getValue().actionPerformed(dummyEvent);
        onChangePassword.getValue().actionPerformed(dummyEvent);

        assertThat(model.getPassword()).isEqualTo(newPassword);
    }

    @Test
    public void registerActionHandler() {
        passwordComponent.init();

        verify(passwordField).addActionListener(ArgumentMatchers.any());
        verify(document).addDocumentListener(ArgumentMatchers.any(PasswordComponent.View.PasswordChangeListener.class));
        verify(changePasswordButton).addActionListener(ArgumentMatchers.any());
    }

    @Test
    public void setValue() {
        passwordComponent.setValue("Default");
        assertThat(model.getPassword()).isEqualTo("Default");
        verify(passwordField).setEnabled(false);
        verify(passwordField).setVisible(false);
        verify(passwordField).setPasswordIsStored(true);
        verify(changePasswordButton).setVisible(true);
    }

    @Test
    public void afterClickOnChangePasswordButton() {
        passwordComponent.enablePasswordOverride();
        verify(passwordField).setEnabled(true);
        verify(passwordField).setVisible(true);
        verify(changePasswordButton).setVisible(false);
        assertThat(model.getPassword()).isEqualTo(org.codinjutsu.tools.jenkins.util.StringUtil.EMPTY);
    }

    @Before
    public void createDummyView() {
        when(view.getChangePasswordButton()).thenReturn(changePasswordButton);
        when(view.getPasswordField()).thenReturn(passwordField);
        when(passwordField.getDocument()).thenReturn(document);
        when(passwordField.getEmptyText()).thenReturn(statusText);
    }

}
