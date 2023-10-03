package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBPasswordField;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.codinjutsu.tools.jenkins.view.util.SpringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Arrays;
import java.util.function.Function;

@RequiredArgsConstructor
public class PasswordComponent {

    private final Model model;
    private final View view;

    @NotNull
    public static PasswordComponent create() {
        return new PasswordComponent(new Model(), new View());
    }

    @NotNull
    public static Function<JPasswordField, String> readPassword() {
        return passwordField -> {
            final char[] password = passwordField.getPassword();
            final String result = new String(password);
            Arrays.fill(password, '\0');
            return result;
        };
    }

    public void init() {
        model.setPassword(null);
        //view.getPasswordField().getEmptyText().setText("use Default Value"); //Concealed
        view.getPasswordField().setEnabled(false);
        view.getPasswordField().setVisible(false);
        view.getChangePasswordButton().setVisible(true);
        view.getChangePasswordButton().setToolTipText("Click here to use a custom password instead the default one.");

        view.getPasswordField().addActionListener(e -> changePassword());
        view.getChangePasswordButton().addActionListener(e -> enablePasswordOverride());
        view.getPasswordField().getDocument().addDocumentListener((View.PasswordChangeListener)
                this::changePassword);
    }

    public void enablePasswordOverride() {
        view.getPasswordField().setEnabled(true);
        view.getPasswordField().setVisible(true);
        model.setPassword(org.codinjutsu.tools.jenkins.util.StringUtil.EMPTY);
        view.getPasswordField().requestFocus();
        view.getChangePasswordButton().setVisible(false);
    }

    public void changePassword() {
        model.setPassword(readPassword().apply(view.getPasswordField()));
    }

    @Nullable
    public String getValue() {
        return model.getPassword();
    }

    public void setValue(@Nullable String defaultValue) {
        model.setPassword(defaultValue);
        view.getPasswordField().setPasswordIsStored(true);
        view.getPasswordField().setEnabled(false);
        view.getPasswordField().setVisible(false);
        view.getChangePasswordButton().setVisible(true);
    }

    @NotNull
    public JComponent asComponent() {
        return view.asComponent();
    }

    @Data
    static class Model {

        private String password = null;

    }

    static class View {

        private final JPanel panel = new JPanel(new SpringLayout());

        @Getter
        private final JBPasswordField passwordField = new JBPasswordField();

        @Getter
        private final JButton changePasswordButton = new JButton("Change Password", AllIcons.Ide.Readonly);

        public View() {
            panel.add(passwordField);
            panel.add(changePasswordButton);
            SpringUtilities.makeCompactGrid(panel, 1, 1, 0, 0, 0, 0);
        }

        @NotNull
        public JComponent asComponent() {
            return panel;
        }

        @FunctionalInterface
        public interface PasswordChangeListener extends DocumentListener {

            @Override
            default void insertUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            default void removeUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            default void changedUpdate(DocumentEvent e) {
                onChange();
            }

            void onChange();
        }
    }
}
