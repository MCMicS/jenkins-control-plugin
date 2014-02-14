package org.codinjutsu.tools.jenkins.security;

import com.intellij.openapi.vfs.VirtualFile;

import java.net.URL;
import java.util.Map;

public class Http4BasicSecurityClient implements SecurityClient {
    @Override
    public void connect(URL jenkinsUrl) {

    }

    @Override
    public String execute(URL url) {
        return null;
    }

    @Override
    public void setFiles(Map<String, VirtualFile> files) {

    }

    @Override
    public void close() {

    }
}
