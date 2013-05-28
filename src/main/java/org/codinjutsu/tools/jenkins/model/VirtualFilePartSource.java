package org.codinjutsu.tools.jenkins.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.httpclient.methods.multipart.PartSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Description
 *
 * @author Yuri Novitsky
 */
public class VirtualFilePartSource implements PartSource {

    private VirtualFile file;

    public VirtualFilePartSource(VirtualFile file) {
        this.file = file;
    }

    @Override
    public long getLength() {
        return file.getLength();
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public InputStream createInputStream() throws IOException {
        return file.getInputStream();
    }
}
