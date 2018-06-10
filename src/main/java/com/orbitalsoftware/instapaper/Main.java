package com.orbitalsoftware.instapaper;

public class Main {

    private static final String username = "alex@alexpolson.com";
    private static final String password = "uiwVxeAEq7gAi3fN";

    private static final String O_AUTH_CONSUMER_TOKEN = "533e8f46e9c9473daecea3287c6a167d";
    private static final String O_AUTH_CONSUMER_SECRET = "fdda218ad197490a8814dce49d2bc393";

    private Main() {
    }

    public static final void main(String[] args) throws Exception {
        OAuth oAuth = new OAuth(O_AUTH_CONSUMER_TOKEN, O_AUTH_CONSUMER_SECRET);
        oAuth.getAccessToken(username, password);
    }
}
