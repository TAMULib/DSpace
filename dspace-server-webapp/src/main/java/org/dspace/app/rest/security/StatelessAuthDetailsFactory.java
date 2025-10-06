package org.dspace.app.rest.security;

import static org.dspace.authenticate.OidcAuthentication.OIDC_AUTH_METHOD_NAME;
import static org.dspace.authenticate.OrcidAuthentication.ORCID_AUTH_METHOD_NAME;
import static org.dspace.authenticate.SamlAuthentication.SAML_AUTH_METHOD_NAME;
import static org.dspace.authenticate.ShibAuthentication.SHIBBOLETH_AUTH_METHOD_NAME;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

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
public enum StatelessAuthDetailsFactory {
    PASSWORD ("password", POST.name(),
        request -> new StatelessLoginFilter<>(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    OIDC (OIDC_AUTH_METHOD_NAME, GET.name(),
        request -> new OidcLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    ORCID (ORCID_AUTH_METHOD_NAME, GET.name(),
        request -> new OrcidLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    SAML (SAML_AUTH_METHOD_NAME, GET.name(),
        request -> new SamlLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    SHIBBOLETH (SHIBBOLETH_AUTH_METHOD_NAME, GET.name(), 
        request -> new ShibbolethLoginFilter(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService())),
    STATELESS ("stateless", POST.name(),
        request -> new StatelessLoginFilter<>(
            request.getUrl(),
            request.getHttpMethod(),
            request.getAuthenticationManager(),
            request.getRestAuthenticationService()));

    // not supporting ip, x509, ldap

    // not specyfing basic, form, cert, digest

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
