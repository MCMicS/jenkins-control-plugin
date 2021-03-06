package org.codinjutsu.tools.jenkins.security;

public enum JenkinsVersion {
    VERSION_1 {
        @Override
        public String getCrumbName() {
            return ".crumb";
        }
    }, VERSION_2 {
        @Override
        public String getCrumbName() {
            return "Jenkins-Crumb";
        }
    };

    public abstract String getCrumbName();
}
