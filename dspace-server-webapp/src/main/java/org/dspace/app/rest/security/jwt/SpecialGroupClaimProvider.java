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
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
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
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.GroupService;
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

    protected GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();

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
            groups = authenticationService.getSpecialGroups(context, request);
        } catch (SQLException e) {
            log.error("SQLException while retrieving special groups", e);
            return null;
        }

        if (groups.isEmpty()) {
            Enumeration<String> attNames = request.getAttributeNames();

            while (attNames.hasMoreElements()) {
                String attName = attNames.nextElement();
                if (attName.endsWith(SPECIAL_GROUPS)) {
                    Set<String> groupNames = (Set<String>) request.getAttribute(attName);
                    for (String groupName : groupNames) {
                        if (groupName == null || groupName.isEmpty()) {
                            continue;
                        }

                        try {
                            log.debug("Looking up special group {}", groupName);
                            Group group = groupService.findByName(context, groupName);
                            if (group == null) {
                                log.warn("Group {} does not exist", groupName);
                            } else {
                                log.debug("Found special group {}", groupName);
                                groups.add(group);
                            }
                        } catch (SQLException ex) {
                            // ignoring database error
                        }
                    }
                }
            }
        }

        return groups.stream()
            .map(group -> group.getID().toString())
            .collect(Collectors.toList());
    }

    @Override
    public void parseClaim(Context context, HttpServletRequest request, JWTClaimsSet jwtClaimsSet) {
        try {
            List<String> groupIds = jwtClaimsSet.getStringListClaim(SPECIAL_GROUPS);

            for (String groupId : CollectionUtils.emptyIfNull(groupIds)) {
                context.setSpecialGroup(UUID.fromString(groupId));
            }

            log.debug("Parsed group ids claim {}", groupIds);

        } catch (ParseException e) {
            log.error("Error while trying to access specialgroups from ClaimSet", e);
        }
    }

}
