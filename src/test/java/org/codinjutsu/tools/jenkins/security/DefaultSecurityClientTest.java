package org.codinjutsu.tools.jenkins.security;

import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.codinjutsu.tools.jenkins.model.FileParameter;
import org.codinjutsu.tools.jenkins.model.RequestData;
import org.codinjutsu.tools.jenkins.model.StringParameter;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class DefaultSecurityClientTest {

    private final DefaultSecurityClient securityClient;

    public DefaultSecurityClientTest() throws NoSuchAlgorithmException {
        securityClient = new DefaultSecurityClient("Crumb", 10, SSLContext.getDefault(), false);
    }


    @Test
    public void createPostForEmptyData() {
        final var post = securityClient.createPost("http://example.org", Collections.emptySet());
        Assertions.assertThat(post.getEntity()).isNull();
    }

    @Test
    public void createPostWithOneFileAndOneStringParameter() throws IOException {
        final VirtualFile virtualFile = new MockVirtualFileWithInputStream("sampleFile.md");
        final var post = securityClient.createPost("http://example.org", Lists.list(new StringParameter("test", "Jenkins"),
                new FileParameter("fileParam", virtualFile)));
        Assertions.assertThat(post.getEntity()).isNotNull();
        final List<FormBodyPart> parts = assertMultipartFormEntity(post.getEntity());
        Assertions.assertThat(parts).hasSize(2);
        final FormBodyPart firstPart = parts.get(0);
        final FormBodyPart secondPart = parts.get(1);
        Assertions.assertThat(firstPart.getName()).isEqualTo("sampleFile.md");
        Assertions.assertThat(secondPart.getName()).isEqualTo("json");
        Assertions.assertThat(firstPart.getBody()).isInstanceOf(InputStreamBody.class);
        Assertions.assertThat(secondPart.getBody()).isInstanceOf(StringBody.class);
        final String expectedJson = "{\"parameter\":[{\"name\":\"fileParam\",\"file\":\"sampleFile.md\"},{\"name\":\"test\",\"value\":\"Jenkins\"}]}";
        Assertions.assertThat(secondPart.getBody().getContentLength()).isEqualTo(expectedJson.length());
    }

    @Test
    public void createPostWithFileNameProvider() throws IOException {
        final VirtualFile virtualFile = new MockVirtualFileWithInputStream("sampleFile.md");
        final var post = securityClient.createPost("http://example.org", Lists.list(new StringParameter("test", "Jenkins"),
                new FileParameter("fileParam", virtualFile, () -> "file0")));
        Assertions.assertThat(post.getEntity()).isNotNull();
        final var parts = assertMultipartFormEntity(post.getEntity());
        Assertions.assertThat(parts).hasSize(2);
        final FormBodyPart firstPart = parts.get(0);
        final FormBodyPart secondPart = parts.get(1);
        Assertions.assertThat(firstPart.getName()).isEqualTo("file0");
        Assertions.assertThat(secondPart.getName()).isEqualTo("json");
        Assertions.assertThat(firstPart.getBody()).isInstanceOf(InputStreamBody.class);
        Assertions.assertThat(secondPart.getBody()).isInstanceOf(StringBody.class);
        final String expectedJson = "{\"parameter\":[{\"name\":\"fileParam\",\"file\":\"file0\"},{\"name\":\"test\",\"value\":\"Jenkins\"}]}";
        Assertions.assertThat(secondPart.getBody().getContentLength()).isEqualTo(expectedJson.length());
    }

    @Test
    public void createPostWithTwoStringParameter() throws IOException {
        final List<RequestData> requestData = Lists.list(new StringParameter("test", "Jenkins"),
                new StringParameter("second", "more"));
        final var post = securityClient.createPost("http://example.org", requestData);
        Assertions.assertThat(post.getEntity()).isNotNull();
        final var parts = assertMultipartFormEntity(post.getEntity());
        Assertions.assertThat(parts).hasSize(1);
        final FormBodyPart firstPart = parts.get(0);
        Assertions.assertThat(firstPart.getName()).isEqualTo("json");
        Assertions.assertThat(firstPart.getBody()).isInstanceOf(StringBody.class);
        final String expectedJson = "{\"parameter\":[{\"name\":\"test\",\"value\":\"Jenkins\"},{\"name\":\"second\",\"value\":\"more\"}]}";
        Assertions.assertThat(firstPart.getBody().getContentLength()).isEqualTo(expectedJson.length());
    }

    @Test
    public void createPostWithParameterIsEncodedAsUTF8() throws IOException {
        final List<RequestData> requestData = Lists.list(new StringParameter("first", "İstanbul"),
                new StringParameter("second", "İÖÇŞĞ"));
        final var post = securityClient.createPost("http://example.org", requestData);
        Assertions.assertThat(post.getEntity()).isNotNull();

        final var parts = assertMultipartFormEntity(post.getEntity());
        Assertions.assertThat(parts).hasSize(1);
        final FormBodyPart firstPart = parts.get(0);
        Assertions.assertThat(firstPart.getName()).isEqualTo("json");
        Assertions.assertThat(firstPart.getBody()).isInstanceOf(StringBody.class);
        final String expectedJson = "{\"parameter\":[{\"name\":\"first\",\"value\":\"İstanbul\"},{\"name\":\"second\",\"value\":\"İÖÇŞĞ\"}]}";
        Assertions.assertThat(firstPart.getBody().getContentLength())
                .isEqualTo(expectedJson.getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     * @see org.apache.http.entity.mime.MultipartFormEntity
     * @see org.apache.http.entity.mime.AbstractMultipartForm
     * @see org.apache.http.entity.mime.AbstractMultipartForm#getBodyParts()
     */
    @SneakyThrows
    private List<FormBodyPart> assertMultipartFormEntity(HttpEntity httpEntity) {
        final var multipartFormEntityClass = Class.forName("org.apache.http.entity.mime.MultipartFormEntity");
        final var abstractMultipartFormClass = Class.forName("org.apache.http.entity.mime.AbstractMultipartForm");
        Assertions.assertThat(httpEntity.getClass()).isEqualTo(multipartFormEntityClass);
        Assertions.assertThat(httpEntity.getClass().getName()).isEqualTo(multipartFormEntityClass.getName());

        final var multipartField = ReflectionSupport.streamFields(multipartFormEntityClass,//
                field -> field.getName().equals("multipart"), HierarchyTraversalMode.TOP_DOWN).findFirst();
        final var getBodyPartsMethod = ReflectionSupport.findMethod(abstractMultipartFormClass,"getBodyParts");
        Assertions.assertThat(multipartField).isPresent();
        Assertions.assertThat(getBodyPartsMethod).isPresent();
        final Field field = multipartField.get();
        field.setAccessible(true);
        final var abstractMultipartForm = field.get(httpEntity);
        //noinspection unchecked
        return (List<FormBodyPart>) ReflectionSupport.invokeMethod(getBodyPartsMethod.get(), abstractMultipartForm);
    }

    private class MockVirtualFileWithInputStream extends MockVirtualFile {

        public MockVirtualFileWithInputStream(String name) {
            super(name);
        }

        @Override
        public @NotNull InputStream getInputStream() {
            return new ByteArrayInputStream(getName().getBytes(StandardCharsets.UTF_8));
        }
    }
}
