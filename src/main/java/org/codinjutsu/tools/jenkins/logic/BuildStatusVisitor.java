package org.codinjutsu.tools.jenkins.logic;


public interface BuildStatusVisitor {
    void visitFailed();

    void visitSuccess();

    void visitUnkown();

    void visitAborted();

}
