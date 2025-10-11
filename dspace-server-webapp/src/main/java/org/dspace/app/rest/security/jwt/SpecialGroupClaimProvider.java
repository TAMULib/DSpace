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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
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
        System.out.println("SpecialGroupClaimProvider#getValue:");
        System.out.println("SpecialGroupClaimProvider#getValue context: " + context);
        if (Objects.nonNull(context)) {
            System.out.println("SpecialGroupClaimProvider#getValue context.getCurrentUser(): " + context.getCurrentUser());
            System.out.println("SpecialGroupClaimProvider#getValue context.getCurrentUser(): " + context.getSpecialGroupUuids());
            System.out.println("SpecialGroupClaimProvider#getValue context.getCurrentUser(): " + context.getAuthenticationMethod());
        }
        System.out.println("SpecialGroupClaimProvider#getValue request: " + request);
        if (Objects.nonNull(request)) {
            request.getAttributeNames().asIterator().forEachRemaining(attribute -> {
                System.out.println("SpecialGroupClaimProvider#getValue request attribute " + attribute + ": " + request.getAttribute(attribute));
            });
        }

        try {
            List<Group> groups =  context.getSpecialGroups();

            if (Objects.isNull(groups)) {
                System.out.println("SpecialGroupClaimProvider#getValue (context) special groups is null. Getting special groups from authentication service.");
                groups = authenticationService.getSpecialGroups(context, request);
            }

             List<String> groupIds = groups.stream().map(group -> group.getID().toString()).collect(Collectors.toList());
             System.out.println("SpecialGroupClaimProvider#getValue return " + groupIds);
             return groupIds;

        } catch (SQLException e) {
            log.error("SQLException while retrieving special groups", e);
        }

       return Collections.emptyList();
    }

    @Override
    public void parseClaim(Context context, HttpServletRequest request, JWTClaimsSet jwtClaimsSet) {
        try {
            List<String> groupIds = jwtClaimsSet.getStringListClaim(SPECIAL_GROUPS);

            for (String groupId : CollectionUtils.emptyIfNull(groupIds)) {
                context.setSpecialGroup(UUID.fromString(groupId));
            }

            System.out.println("SpecialGroupClaimProvider#parseClaim " + groupIds);
        } catch (ParseException e) {
            log.error("Error while trying to access specialgroups from ClaimSet", e);
        }
    }

}
