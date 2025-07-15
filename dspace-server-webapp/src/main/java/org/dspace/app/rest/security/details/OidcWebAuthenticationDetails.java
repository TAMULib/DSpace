/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import static org.dspace.authenticate.OidcAuthentication.OIDC_AUTH_SG_ATTRIBUTE;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This holds the OIDC web authentication details and which request attribute
 * should have the details.
 * 
 * @note OIDC is the only authentication method that utilizes web authentication details.
 */
public class OidcWebAuthenticationDetails extends SpecialGroupsWebAuthenticationDetails<Set<String>> {

    public OidcWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
    }

    public String getKey() {
        return OIDC_AUTH_SG_ATTRIBUTE;
    }

}
