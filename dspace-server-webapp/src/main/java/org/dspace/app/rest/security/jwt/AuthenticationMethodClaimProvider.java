/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.jwt;

import java.sql.SQLException;
import java.text.ParseException;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authenticate.service.AuthenticationService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides a claim for a JSON Web Token, this claim is responsible for adding the authentication method to it
 */
@Component
public class AuthenticationMethodClaimProvider implements JWTClaimProvider {

    public static final String AUTHENTICATION_METHOD = "authenticationMethod";

    private static final Logger log = LogManager.getLogger();

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public String getKey() {
        return AUTHENTICATION_METHOD;
    }

    @Override
    public Object getValue(final Context context, final HttpServletRequest request) {

        final String servletPath = request.getServletPath();

        threadRequestSystemOut(context, request, "AMCP: Request servlet path: " + servletPath);
        String authMethod = null;

        switch (servletPath) {
            case "/api/authn/login":
                String user = request.getParameter("user");
                String password = request.getParameter("password");

                if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {
                    authMethod = "password"; // new PasswordAuthentication().getName()
                    threadRequestSystemOut(context, request, "AMCP: Password Authentication");
                } else {
                    authMethod = null;
                }
                break;
            case "/api/authn/shibboleth":
                authMethod = "shib"; // new ShibAuthentication().getName()
                threadRequestSystemOut(context, request, "AMCP: Shibboleth Authentication");
                break;
            case "/api/authn/orcid":
                authMethod = "orcid"; // new OrcidAuthentication().getName()
                threadRequestSystemOut(context, request, "AMCP: Orcid Authentication");
                break;
            case "/api/authn/oidc":
                authMethod = "oidc"; // new OidcAuthentication().getName()
                threadRequestSystemOut(context, request, "AMCP: OIDC Authentication");
                break;
            case "/api/authn/saml":
                authMethod = "saml"; // new SamlAuthentication().getName()
                threadRequestSystemOut(context, request, "AMCP: SAML Authentication");
                break;
            default:
                break;
        }

        if (StringUtils.isNotEmpty(authMethod)) {
            threadRequestSystemOut(context, request, "AMCP: Setting auth method " + authMethod + " on context from request URL matching login filter");
            context.setAuthenticationMethod(authMethod);
        } else {
            threadRequestSystemOut(context, request, "AMCP: Auth method not known yet. Checking request attribute am");
            authMethod = (String) request.getAttribute("am");
            
            if (StringUtils.isNotEmpty(authMethod)) {
                threadRequestSystemOut(context, request, "AMCP: Setting auth method " + authMethod + " on context from request attribute am");
                context.setAuthenticationMethod(authMethod);
            } else {
                threadRequestSystemOut(context, request, "AMCP: Request attribute am not defined");
            }
        }
    
        String lazyAuthMethod = authenticationService.getAuthenticationMethod(context, request);

        if (StringUtils.isNotEmpty(authMethod)) {
            context.setAuthenticationMethod(lazyAuthMethod);
        } else {
            if (lazyAuthMethod.equals(authMethod)) {
                threadRequestSystemOut(context, request, "AMCP: Authentication service returned same auth method as on the context");
            } else {
                threadRequestSystemOut(context, request, "AMCP: Authentication service returned a different auth method as on the context");
                threadRequestSystemOut(context, request, "AMCP: Authentication service auth method " + lazyAuthMethod);
                threadRequestSystemOut(context, request, "AMCP: Context auth method " + authMethod);
            }
        }

        return context.getAuthenticationMethod();
    }

    @Override
    public void parseClaim(final Context context, final HttpServletRequest request, final JWTClaimsSet jwtClaimsSet)
            throws SQLException {
        try {
            String authMethod = jwtClaimsSet.getStringClaim(AUTHENTICATION_METHOD);
            context.setAuthenticationMethod(authMethod);
            request.setAttribute("am", authMethod);
            threadRequestSystemOut(context, request, "AMCP: parsed auth method " + authMethod + " from stateless token");
        } catch (ParseException e) {
            log.error(e::getMessage, e);
        }
    }

    private void threadRequestSystemOut(Context context, HttpServletRequest request, String message) {
        System.out.println(
            String.format(
                "Context %12s - thread %4s - request %4s: %s",
                context.hashCode(),
                Thread.currentThread().getId(),
                request.getRequestId(), message
            )
        );
    }
}
