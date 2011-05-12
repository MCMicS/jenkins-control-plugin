package org.codinjutsu.tools.jenkins.logic;


public enum AuthenticationResult {

    SUCCESSFULL(DefaultJenkinsRequestManager.SUCCESS_ID, "Successful"),
    BAD_CREDENTIAL(DefaultJenkinsRequestManager.BAD_CREDENTIAL_ID, "Bad Credential"),
    BAD_URL(DefaultJenkinsRequestManager.BAD_URL_ID, "Bad URL");


    private final int id;
    private final String label;


    AuthenticationResult(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static AuthenticationResult find(int id) {
        for (AuthenticationResult authenticationResult : AuthenticationResult.values()) {
            if (authenticationResult.getId() == id) {
                return authenticationResult;
            }
        }

        return null;
    }
}
