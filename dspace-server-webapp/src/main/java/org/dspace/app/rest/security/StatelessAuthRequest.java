package org.dspace.app.rest.security;

import org.springframework.security.authentication.AuthenticationManager;

/**
 * Everything required to process an HttpServletRequest through
 * StatelessLoginFilter.
 */
public class StatelessAuthRequest {

    private final String url;
    private final String authMethodName;
    private final String httpMethodName;
    private final AuthenticationManager authenticationManager;
    private final RestAuthenticationService restAuthenticationService;

    private StatelessAuthRequest(
            String url,
            String authMethodName,
            String httpMethodName,
            AuthenticationManager authenticationManager,
            RestAuthenticationService restAuthenticationService) {
        this.url = url;
        this.authMethodName = authMethodName;
        this.httpMethodName = httpMethodName;
        this.authenticationManager = authenticationManager;
        this.restAuthenticationService = restAuthenticationService;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthMethodName() {
        return authMethodName;
    }

    public String getHttpMethodName() {
        return httpMethodName;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public RestAuthenticationService getRestAuthenticationService() {
        return restAuthenticationService;
    }

    public static StatelessAuthRequest create(
            String url,
            String authMethodName,
            String httpMethodName,
            AuthenticationManager authenticationManager,
            RestAuthenticationService restAuthenticationService) {

        return new StatelessAuthRequest(
                url,
                authMethodName,
                httpMethodName,
                authenticationManager,
                restAuthenticationService);
    }

}
