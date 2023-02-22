package org.codinjutsu.tools.jenkins.security;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.codinjutsu.tools.jenkins.exception.JenkinsPluginRuntimeException;
import org.codinjutsu.tools.jenkins.model.FileParameter;
import org.codinjutsu.tools.jenkins.model.RequestData;
import org.codinjutsu.tools.jenkins.model.VirtualFilePart;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

@SuppressWarnings("java:S110")
public class JenkinsPost extends HttpPost {

    public JenkinsPost(URI uri) {
        super(uri);
        init();
    }

    public JenkinsPost(String uri) {
        super(uri);
        init();
    }

    public void setData(@NotNull Collection<RequestData> data) {
        if (!data.isEmpty()) {
            final var multipartEntity = MultipartEntityBuilder.create();

            data.stream().filter(FileParameter.class::isInstance)
                    .map(FileParameter.class::cast)
                    .forEach(fileParameter -> addMultipartBinaryBody(fileParameter, multipartEntity));

            final var parameterJson = new JsonObject();
            parameterJson.put("parameter", data);
            multipartEntity.addTextBody("json", Jsoner.serialize(parameterJson), ContentType.APPLICATION_JSON)
                    .setCharset(DefaultSecurityClient.CHARSET);
            final var multipart = multipartEntity.build();
            this.setEntity(multipart);
        }
    }

    private void addMultipartBinaryBody(FileParameter fileParameter,
                                        MultipartEntityBuilder multipartEntityBuilder) {
        final var virtualFilePart = new VirtualFilePart(fileParameter.getFile());
        try {
            multipartEntityBuilder.addBinaryBody(fileParameter.getFileName(), virtualFilePart.createInputStream(),
                    ContentType.APPLICATION_OCTET_STREAM, virtualFilePart.getFileName());
        } catch (IOException e) {
            throw new JenkinsPluginRuntimeException(e.getMessage(), e);
        }
    }

    private void init() {
        this.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
    }
}
