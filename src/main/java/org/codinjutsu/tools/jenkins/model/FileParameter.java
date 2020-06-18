package org.codinjutsu.tools.jenkins.model;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Supplier;

@Value
public class FileParameter implements RequestData {

    @NotNull
    private final String name;

    @NotNull
    private final VirtualFile file;

    @NotNull
    private final String fileName;

    public FileParameter(@NotNull String name, @NotNull VirtualFile file) {
        this(name, file, file::getName);
    }

    public FileParameter(@NotNull String name, @NotNull VirtualFile file, @NotNull Supplier<String> fileNameProvider) {
        this.name = name;
        this.file = file;
        this.fileName = fileNameProvider.get();
    }

    @Override
    public void toJson(Writer writable) throws IOException {
        final JsonObject json = new JsonObject();
        json.put("name", name);
        json.put("file", getFileName());
        json.toJson(writable);
    }
}
