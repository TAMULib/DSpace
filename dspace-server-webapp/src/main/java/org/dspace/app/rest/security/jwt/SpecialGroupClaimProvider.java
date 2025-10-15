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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authenticate.service.AuthenticationService;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JWT claim provider to read and set the special groups of an eperson on a JWT token
 *
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 * @author Tom Desair (tom dot desair at atmire dot com)
 */
@Component
public class SpecialGroupClaimProvider implements JWTClaimProvider {

    private static final Logger log = LogManager.getLogger();

    public static final String SPECIAL_GROUPS = "sg";

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public String getKey() {
        return SPECIAL_GROUPS;
    }

    @Override
    public Object getValue(Context context, HttpServletRequest request) {
        List<Group> groups = new ArrayList<>();
        try {

            final String servletPath = request.getServletPath();

            threadRequestSystemOut(context, request, "SGCP: Request servlet path: " + servletPath);
            String authMethod = null;

            switch (servletPath) {
                case "/api/authn/login":
                    String user = request.getParameter("user");
                    String password = request.getParameter("password");

                    if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {
                        authMethod = "password"; // new PasswordAuthentication().getName()
                        threadRequestSystemOut(context, request, "SGCP: Password Authentication");
                    } else {
                        authMethod = null;
                    }
                    break;
                case "/api/authn/shibboleth":
                    authMethod = "shib"; // new ShibAuthentication().getName()
                    threadRequestSystemOut(context, request, "SGCP: Shibboleth Authentication");
                    break;
                case "/api/authn/orcid":
                    authMethod = "orcid"; // new OrcidAuthentication().getName()
                    threadRequestSystemOut(context, request, "SGCP: Orcid Authentication");
                    break;
                case "/api/authn/oidc":
                    authMethod = "oidc"; // new OidcAuthentication().getName()
                    threadRequestSystemOut(context, request, "SGCP: OIDC Authentication");
                    break;
                case "/api/authn/saml":
                    authMethod = "saml"; // new SamlAuthentication().getName()
                    threadRequestSystemOut(context, request, "SGCP: SAML Authentication");
                    break;
                default:
                    break;
            }

            if (StringUtils.isNotEmpty(authMethod)) {
                threadRequestSystemOut(context, request, "SGCP: Setting auth method " + authMethod + " on context from request URL matching login filter");
                context.setAuthenticationMethod(authMethod);
            } else {
                threadRequestSystemOut(context, request, "SGCP: Auth method not known yet. Checking request attribute am");
                authMethod = (String) request.getAttribute("am");
                
                if (StringUtils.isNotEmpty(authMethod)) {
                    threadRequestSystemOut(context, request, "SGCP: Setting auth method " + authMethod + " on context from request attribute am");
                    context.setAuthenticationMethod(authMethod);
                } else {
                    threadRequestSystemOut(context, request, "SGCP: Request attribute am not defined");
                }
            }

            threadRequestSystemOut(context, request, "SGCP: Context: " + context);
            threadRequestSystemOut(context, request, "SGCP: Context authentication method: " + context.getAuthenticationMethod());
            threadRequestSystemOut(context, request, "SGCP: Context special groups: " + context.getSpecialGroupUuids());

            List<Group> lazyGroups = authenticationService.getSpecialGroups(context, request);

            groups.addAll(lazyGroups);

            threadRequestSystemOut(context, request, "SGCP: Special groups from authentication service: ");
            groups.stream().forEach(sg -> {
                threadRequestSystemOut(context, request, "SGCP: \t" + sg.getName() + " (" + sg.getID() + ")");
            });
        } catch (SQLException e) {
            log.error("SQLException while retrieving special groups", e);
            return null;
        }
        List<String> groupIds = groups.stream().map(group -> group.getID().toString()).collect(Collectors.toList());
        return groupIds;
    }

    @Override
    public void parseClaim(Context context, HttpServletRequest request, JWTClaimsSet jwtClaimsSet) {
        try {
            List<String> groupIds = jwtClaimsSet.getStringListClaim(SPECIAL_GROUPS);

            for (String groupId : CollectionUtils.emptyIfNull(groupIds)) {
                context.setSpecialGroup(UUID.fromString(groupId));
            }
        } catch (ParseException e) {
            log.error("Error while trying to access specialgroups from ClaimSet", e);
        }
    }

    private void threadRequestSystemOut(Context context, HttpServletRequest request, String message) {
        System.out.println("Context " + context.hashCode() + " - thread " + Thread.currentThread().getId() + " - request " + request.getRequestId() + ": " + message);
    }

}
