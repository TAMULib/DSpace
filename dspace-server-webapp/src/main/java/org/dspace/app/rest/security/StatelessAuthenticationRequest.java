package org.dspace.app.rest.security;

import org.springframework.security.authentication.AuthenticationManager;

public class StatelessAuthenticationRequest {

    private final String url;
    private final String authMethodName;
    private final String httpMethodName;
    private final AuthenticationManager authenticationManager;
    private final RestAuthenticationService restAuthenticationService;

    private StatelessAuthenticationRequest(
            final String url,
            final String authMethodName,
            final String httpMethodName,
            final AuthenticationManager authenticationManager,
            final RestAuthenticationService restAuthenticationService) {
        this.url = url;
        this.authMethodName = authMethodName;
        this.httpMethodName = httpMethodName;
        this.authenticationManager = authenticationManager;
        this.restAuthenticationService = restAuthenticationService;
    }

    String getUrl() {
        return url;
    }

    String getAuthMethodName() {
        return authMethodName;
    }

    String getHttpMethodName() {
        return httpMethodName;
    }

    AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    RestAuthenticationService getRestAuthenticationService() {
        return restAuthenticationService;
    }

    public static StatelessAuthenticationRequest create(
            final String url,
            final String authMethodName,
            final String httpMethodName,
            final AuthenticationManager authenticationManager,
            final RestAuthenticationService restAuthenticationService) {

        return new StatelessAuthenticationRequest(
                url,
                authMethodName,
                httpMethodName,
                authenticationManager,
                restAuthenticationService);
    }

}
