package org.codinjutsu.tools.jenkins.model;

import com.github.cliftonlabs.json_simple.JsonObject;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

@Value
public class StringParameter implements RequestData {

    @NotNull
    private final String name;

    @NotNull
    private final String value;

    public StringParameter(@NotNull String name, @NotNull String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void toJson(Writer writable) throws IOException {
        final JsonObject json = new JsonObject();
        json.put("name", name);
        json.put("value", value);
        json.toJson(writable);
    }
}
