package org.codinjutsu.tools.jenkins.view.parameter;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
//@Value
public class JobParameterComponent<T> {

    @NotNull
    private JobParameter jobParameter;

    @NotNull
    private JComponent viewElement;

    @NotNull
    private Function<JComponent, T> valueProvider;

    @NotNull
    private BooleanSupplier validator;

    private boolean isVisible;

    public <C extends JComponent> JobParameterComponent(@NotNull JobParameter jobParameter, @NotNull C viewElement) {
        this(jobParameter, viewElement, () -> false);
    }

    public <C extends JComponent> JobParameterComponent(@NotNull JobParameter jobParameter, @NotNull C viewElement,
                                                        boolean isVisible) {
        this(jobParameter, viewElement, () -> false, isVisible);
    }

    public <C extends JComponent> JobParameterComponent(@NotNull JobParameter jobParameter, @NotNull C viewElement,
                                                        @NotNull BooleanSupplier validator) {
        this(jobParameter, viewElement, component -> null, validator);
    }

    public <C extends JComponent> JobParameterComponent(@NotNull JobParameter jobParameter, @NotNull C viewElement,
                                                        @NotNull BooleanSupplier validator,
                                                        boolean isVisible) {
        this(jobParameter, viewElement, component -> null, validator, isVisible);
    }

    public <C extends JComponent> JobParameterComponent(@NotNull JobParameter jobParameter,
                                                        @NotNull C viewElement,
                                                        @NotNull Function<C, T> valueProvider) {
        this(jobParameter, viewElement, valueProvider, () -> false);
    }

    public <C extends JComponent> JobParameterComponent(@NotNull JobParameter jobParameter,
                                                        @NotNull C viewElement,
                                                        @NotNull Function<C, T> valueProvider,
                                                        @NotNull BooleanSupplier validator) {
        this(jobParameter, viewElement, valueProvider, validator, true);
    }

    @SuppressWarnings("unchecked")
    public <C extends JComponent> JobParameterComponent(@NotNull JobParameter jobParameter,
                                                        @NotNull C viewElement,
                                                        @NotNull Function<C, T> valueProvider,
                                                        @NotNull BooleanSupplier validator,
                                                        boolean isVisible) {
        this.jobParameter = jobParameter;
        this.viewElement = viewElement;
        this.valueProvider = (Function<JComponent, T>) valueProvider;
        this.validator = validator;
        this.isVisible = isVisible;
    }

    public void ifHasValue(Consumer<T> valueConsumer) {
        Optional.ofNullable(valueProvider.apply(getViewElement())).ifPresent(valueConsumer);
    }

    public boolean hasError() {
        return validator.getAsBoolean();
    }
}
