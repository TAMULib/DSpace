/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import static org.dspace.authenticate.ShibAuthentication.SHIBBOLETH_AUTH_SG_ATTRIBUTE;

import jakarta.servlet.http.HttpServletRequest;

public class ShibbolethWebAuthenticationDetails extends SpecialGroupsWebAuthenticationDetails {

    public ShibbolethWebAuthenticationDetails(HttpServletRequest request, String authMethodName) {
        super(request, authMethodName);
    }

    public String getKey() {
        return SHIBBOLETH_AUTH_SG_ATTRIBUTE;
    }

}
