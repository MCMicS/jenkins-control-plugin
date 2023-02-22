package org.codinjutsu.tools.jenkins.security;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

@Value
@RequiredArgsConstructor
public class JenkinsRedirectStrategy implements RedirectStrategy {

    private final @NotNull DefaultRedirectStrategy delegate;

    public JenkinsRedirectStrategy() {
        this(new LaxRedirectStrategy());
    }

    @Override
    public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws ProtocolException {
        return delegate.isRedirected(httpRequest, httpResponse, httpContext);
    }

    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException {
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
            HttpPost post = (HttpPost) request;
            final var jenkinsPost = new JenkinsPost(delegate.getLocationURI(request, response, context));
            jenkinsPost.setEntity(post.getEntity());
            return jenkinsPost;
        } else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
            return new HttpDelete(delegate.getLocationURI(request, response, context));
        } else {
            return delegate.getRedirect(request, response, context);
        }
    }
}
