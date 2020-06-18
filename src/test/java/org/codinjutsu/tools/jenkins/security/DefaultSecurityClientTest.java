package org.codinjutsu.tools.jenkins.security;

import com.github.cliftonlabs.json_simple.Jsoner;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.assertj.core.api.Assertions;
import org.codinjutsu.tools.jenkins.model.FileParameter;
import org.codinjutsu.tools.jenkins.model.RequestData;
import org.codinjutsu.tools.jenkins.model.StringParameter;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class DefaultSecurityClientTest {

    private final DefaultSecurityClient securityClient = new DefaultSecurityClient("Crumb", 10);

    @Test
    public void createPostForEmptyData() {
        final PostMethod post = securityClient.createPost("http://example.org", Collections.emptySet());
        Assertions.assertThat(post.getRequestEntity()).isNull();
    }

    @Test
    public void createPostWithOneFileAndOneStringParameter() throws IOException {
        final VirtualFile virtualFile = new MockVirtualFile("sampleFile.md");
        final PostMethod post = securityClient.createPost("http://example.org", List.of(new StringParameter("test", "Jenkins"),
                new FileParameter("fileParam", virtualFile)));
        Assertions.assertThat(post.getRequestEntity()).isNotNull();
        Assertions.assertThat(post.getRequestEntity()).isInstanceOf(MultipartRequestEntity.class);
        final MultipartRequestEntity multipartRequestEntity = (MultipartRequestEntity) post.getRequestEntity();
        final Part[] parts = Whitebox.getInternalState(multipartRequestEntity, Part[].class);
        Assertions.assertThat(parts).hasSize(2);
        Assertions.assertThat(parts[0]).isInstanceOf(FilePart.class);
        Assertions.assertThat(parts[0].getName()).isEqualTo("sampleFile.md");
        Assertions.assertThat(parts[1]).isInstanceOf(StringPart.class);
        final String expectedJson = "{\"parameter\":[{\"name\":\"fileParam\",\"file\":\"sampleFile.md\"},{\"name\":\"test\",\"value\":\"Jenkins\"}]}";
        final StringPart expected = new StringPart("json", expectedJson);
        Assertions.assertThat(parts[1].length()).isEqualTo(expected.length());
    }

    @Test
    public void createPostWithFileNameProvider() throws IOException {
        final VirtualFile virtualFile = new MockVirtualFile("sampleFile.md");
        final PostMethod post = securityClient.createPost("http://example.org", List.of(new StringParameter("test", "Jenkins"),
                new FileParameter("fileParam", virtualFile, () -> "file0")));
        Assertions.assertThat(post.getRequestEntity()).isNotNull();
        Assertions.assertThat(post.getRequestEntity()).isInstanceOf(MultipartRequestEntity.class);
        final MultipartRequestEntity multipartRequestEntity = (MultipartRequestEntity) post.getRequestEntity();
        final Part[] parts = Whitebox.getInternalState(multipartRequestEntity, Part[].class);
        Assertions.assertThat(parts).hasSize(2);
        Assertions.assertThat(parts[0]).isInstanceOf(FilePart.class);
        Assertions.assertThat(parts[0].getName()).isEqualTo("file0");
        Assertions.assertThat(parts[1]).isInstanceOf(StringPart.class);
        final String expectedJson = "{\"parameter\":[{\"name\":\"fileParam\",\"file\":\"file0\"},{\"name\":\"test\",\"value\":\"Jenkins\"}]}";
        final StringPart expected = new StringPart("json", expectedJson);
        Assertions.assertThat(parts[1].length()).isEqualTo(expected.length());
    }

    @Test
    public void createPostWithOTwoStringParameter() throws IOException {
        final VirtualFile virtualFile = new MockVirtualFile("sampleFile.md");
        final List<RequestData> requestData = List.of(new StringParameter("test", "Jenkins"),
                new StringParameter("second", "more"));
        final PostMethod post = securityClient.createPost("http://example.org", requestData);
        Assertions.assertThat(post.getRequestEntity()).isNotNull();
        Assertions.assertThat(post.getRequestEntity()).isInstanceOf(MultipartRequestEntity.class);
        final MultipartRequestEntity multipartRequestEntity = (MultipartRequestEntity) post.getRequestEntity();
        final Part[] parts = Whitebox.getInternalState(multipartRequestEntity, Part[].class);
        Assertions.assertThat(parts).hasSize(1);
        Assertions.assertThat(parts[0]).isInstanceOf(StringPart.class);
        Assertions.assertThat(parts[0].getName()).isEqualTo("json");
        final String expectedJson = "{\"parameter\":[{\"name\":\"test\",\"value\":\"Jenkins\"},{\"name\":\"second\",\"value\":\"more\"}]}";
        final StringPart expected = new StringPart("json", expectedJson);

        Assertions.assertThat(parts[0].length()).isEqualTo(expected.length());
    }
}
