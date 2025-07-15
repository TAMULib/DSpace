/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import static org.dspace.authenticate.ShibAuthentication.SHIBBOLETH_AUTH_SG_ATTRIBUTE;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This holds the Shibboleth web authentication details and which request attribute
 * should have the details.
 */
public class ShibbolethWebAuthenticationDetails extends SpecialGroupsWebAuthenticationDetails<Set<String>> {

    public ShibbolethWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
    }

    public String getKey() {
        return SHIBBOLETH_AUTH_SG_ATTRIBUTE;
    }

}
