package org.dspace.app.rest.security;

import static org.dspace.authenticate.OidcAuthentication.OIDC_AUTH_METHOD_NAME;
import static org.dspace.authenticate.OrcidAuthentication.ORCID_AUTH_METHOD_NAME;
import static org.dspace.authenticate.PasswordAuthentication.PASSWORD_AUTH_METHOD_NAME;
import static org.dspace.authenticate.SamlAuthentication.SAML_AUTH_METHOD_NAME;
import static org.dspace.authenticate.ShibAuthentication.SHIBBOLETH_AUTH_METHOD_NAME;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.dspace.app.rest.security.details.OidcWebAuthenticationDetails;
import org.dspace.app.rest.security.details.OrcidWebAuthenticationDetails;
import org.dspace.app.rest.security.details.PasswordWebAuthenticationDetails;
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
    OIDC (OIDC_AUTH_METHOD_NAME, GET.name(), "/api/authn/oidc", request -> new OidcLoginFilter(request)),
    ORCID (ORCID_AUTH_METHOD_NAME, GET.name(), "/api/authn/orcid", request -> new OrcidLoginFilter(request)),
    PASSWORD (PASSWORD_AUTH_METHOD_NAME, POST.name(), "/api/authn/login", request -> new PasswordLoginFilter(request)),
    SAML (SAML_AUTH_METHOD_NAME, GET.name(), "/api/authn/saml", request -> new SamlLoginFilter(request)),
    SHIBBOLETH (SHIBBOLETH_AUTH_METHOD_NAME, GET.name(), "/api/authn/shibboleth", request -> new ShibbolethLoginFilter(request));

    // not supporting ip, x509, ldap

    // not specyfing basic, form, cert, digest

    private final String authMethodName;
    private final String httpMethodName;
    private final String url;
    private final Function<StatelessAuthRequest, StatelessLoginFilter<?>> filter;

    private static final
    Map<String, Function<StatelessAuthRequest, StatelessLoginFilter<?>>> frames
        = new HashMap<>();

    private static final
    Map<String, String> mapping
        = new HashMap<>();

    private static final
    Map<String, Function<HttpServletRequest, WebAuthenticationDetails>> observetory
        = new HashMap<>();

    static {
        for (StatelessAuthDetailsFactory factory : values()) {
            frames.put(factory.authMethodName, factory.filter);
            mapping.put(factory.url, factory.authMethodName);
            observetory.put(factory.authMethodName, request -> {
                switch (factory) {
                    case OIDC: return new OidcWebAuthenticationDetails(request, factory.authMethodName);
                    case ORCID: return new OrcidWebAuthenticationDetails(request, factory.authMethodName);
                    case PASSWORD: return new PasswordWebAuthenticationDetails(request, factory.authMethodName);
                    case SAML: return new SamlWebAuthenticationDetails(request, factory.authMethodName);
                    case SHIBBOLETH: return new ShibbolethWebAuthenticationDetails(request, factory.authMethodName);
                    default: return new StatelessWebAuthenticationDetails(request, factory.authMethodName);
                }
            });
        }
    }

    StatelessAuthDetailsFactory(
        String authMethodName,
        String httpMethodName,
        String url,
        Function<StatelessAuthRequest, StatelessLoginFilter<?>> filter
    ) {
        this.authMethodName = authMethodName;
        this.httpMethodName = httpMethodName;
        this.url = url;
        this.filter = filter;
    }

    public StatelessLoginFilter<?> getLoginFilter(
        AuthenticationManager authenticationManager,
        RestAuthenticationService restAuthenticationService,
        String url
    ) {
        final StatelessLoginFilter<?> filter = frames.get(authMethodName)
            .apply(StatelessAuthRequest.create(
                url,
                authMethodName,
                httpMethodName,
                authenticationManager,
                restAuthenticationService
            ));

        filter.setAuthenticationDetailsSource(
            new AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails>() {
                @Override
                public WebAuthenticationDetails buildDetails(HttpServletRequest request) {
                    return observetory.get(authMethodName)
                        .apply(request);
                }
            }
        );

        return filter;
    }

    public String getAuthMethodName() {
        return authMethodName;
    }

    public String getHttpMethodName() {
        return httpMethodName;
    }

    public String getUrl() {
        return url;
    }

    public static String getAuthMethodNameByUrl(String url) {
        return mapping.get(url);
    }

}
