package org.dspace.app.rest.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.dspace.app.rest.security.details.OidcWebAuthenticationDetails;
import org.dspace.app.rest.security.details.OrcidWebAuthenticationDetails;
import org.dspace.app.rest.security.details.SamlWebAuthenticationDetails;
import org.dspace.app.rest.security.details.ShibbolethWebAuthenticationDetails;
import org.dspace.app.rest.security.details.StatelessWebAuthenticationDetails;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Enumeration factory for stateless authentication filters with details.
 */
public enum StatelessAuthDetailsFactory { // use constants
    PASSWORD ("password", "POST",
        request -> new StatelessLoginFilter<>(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    OIDC ("oidc", "GET",
        request -> new OidcLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    ORCID  ("orcid", "GET",
        request -> new OrcidLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    SAML ("saml", "GET",
        request -> new SamlLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    SHIBBOLETH ("shibboleth", "GET", 
        request -> new ShibbolethLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    STATELESS ("stateless", "POST",
        request -> new StatelessLoginFilter<>(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService()));

    // ip, x509, ldap

    // basic, form, cert, digest

    private final String name;
    private final String method;
    private final Function<StatelessAuthRequest, StatelessLoginFilter<?, ?>> filter;

    private static final
    Map<String, Function<StatelessAuthRequest, StatelessLoginFilter<?,?>>> frames
        = new HashMap<>();
    private static final
    Map<String, Function<HttpServletRequest, WebAuthenticationDetails>> observetory
        = new HashMap<>();

    static {
        for (StatelessAuthDetailsFactory factory : values()) {
            frames.put(factory.name, factory.filter);
            observetory.put(factory.name, request -> {
                switch (factory) {
                    case OIDC: return new OidcWebAuthenticationDetails(request);
                    case ORCID: return new OrcidWebAuthenticationDetails(request);
                    case SAML: return new SamlWebAuthenticationDetails(request);
                    case SHIBBOLETH: return new ShibbolethWebAuthenticationDetails(request);
                    case STATELESS:
                    case PASSWORD:
                    default: return new StatelessWebAuthenticationDetails(request);
                }
            });
        }
    }

    StatelessAuthDetailsFactory(
        String name,
        String method,
        Function<StatelessAuthRequest, StatelessLoginFilter<?, ?>> filter
    ) {
        this.name = name;
        this.method = method;
        this.filter = filter;
    }

    public StatelessLoginFilter<?, ?> getLoginFilter(
        AuthenticationManager authenticationManager,
        RestAuthenticationService restAuthenticationService,
        String url
    ) {
        final String name = this.name;

        final StatelessLoginFilter<?, ?> filter = frames.get(name)
            .apply(StatelessAuthRequest.create(
                url,
                method,
                authenticationManager,
                restAuthenticationService
            ));

        filter.setAuthenticationDetailsSource(
            new AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails>() {
                @Override
                public WebAuthenticationDetails buildDetails(HttpServletRequest request) {
                    return observetory.get(name)
                        .apply(request);
                }
            }
        );

        return filter;
    }

}
