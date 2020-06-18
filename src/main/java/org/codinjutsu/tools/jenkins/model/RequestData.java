package org.codinjutsu.tools.jenkins.model;

import com.github.cliftonlabs.json_simple.Jsonable;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;

public interface RequestData extends Jsonable {

    @Override
    default String toJson() {
        final StringWriter writable = new StringWriter();
        try {
            this.toJson(writable);
        } catch (final IOException caught) {
            Logger.getLogger(RequestData.class).error(caught.getMessage(), caught);
        }
        return writable.toString();
    }
}
