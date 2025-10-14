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
import java.util.Objects;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletRequest;
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
        System.out.println("AuthenticationMethodClaimProvider#getValue:");
        System.out.println("AuthenticationMethodClaimProvider#getValue context: " + context);

        if (Objects.nonNull(context)) {
            System.out.println("AuthenticationMethodClaimProvider#getValue context.getCurrentUser(): " + context.getCurrentUser());
            System.out.println("AuthenticationMethodClaimProvider#getValue context.getSpecialGroupUuids(): " + context.getSpecialGroupUuids());
            System.out.println("AuthenticationMethodClaimProvider#getValue context.getAuthenticationMethod(): " + context.getAuthenticationMethod());
        }
        System.out.println("AuthenticationMethodClaimProvider#getValue request: " + request);
        if (Objects.nonNull(request)) {
            request.getAttributeNames().asIterator().forEachRemaining(attribute -> {
                System.out.println("AuthenticationMethodClaimProvider#getValue request attribute " + attribute + ": " + request.getAttribute(attribute));
            });
        }

        String authenticationMethod = context.getAuthenticationMethod();
        if (Objects.isNull(authenticationMethod)) {
            System.out.println("AuthenticationMethodClaimProvider#getValue (context) authentication method is null. Get authentication method from authentication service.");
            
        }

        System.out.println("AuthenticationMethodClaimProvider#getValue (context) et authentication method from authentication service");
        authenticationMethod = authenticationService.getAuthenticationMethod(context, request);

        System.out.println("AuthenticationMethodClaimProvider#getValue return " + authenticationMethod);

        return authenticationMethod;
    }

    @Override
    public void parseClaim(final Context context, final HttpServletRequest request, final JWTClaimsSet jwtClaimsSet)
            throws SQLException {
        try {
            System.out.println("AuthenticationMethodClaimProvider#parseClaim " + jwtClaimsSet.getStringClaim(AUTHENTICATION_METHOD));
            context.setAuthenticationMethod(jwtClaimsSet.getStringClaim(AUTHENTICATION_METHOD));
        } catch (ParseException e) {
            log.error(e::getMessage, e);
        }
    }
}
