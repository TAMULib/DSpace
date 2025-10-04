package org.dspace.app.rest.security;

import org.springframework.security.authentication.AuthenticationManager;

/**
 * Everything required to process an HttpServletRequest through
 * StatelessLoginFilter.
 */
public class StatelessAuthRequest {

    private final String url;
    private final String httpMethod;
    private final AuthenticationManager authenticationManager;
    private final RestAuthenticationService restAuthenticationService;

    private StatelessAuthRequest(
            String url,
            String httpMethod,
            AuthenticationManager authenticationManager,
            RestAuthenticationService restAuthenticationService) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.authenticationManager = authenticationManager;
        this.restAuthenticationService = restAuthenticationService;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public RestAuthenticationService getRestAuthenticationService() {
        return restAuthenticationService;
    }

    public static StatelessAuthRequest create(
            String url,
            String httpMethod,
            AuthenticationManager authenticationManager,
            RestAuthenticationService restAuthenticationService) {

        return new StatelessAuthRequest(
                url,
                httpMethod,
                authenticationManager,
                restAuthenticationService);
    }

}
