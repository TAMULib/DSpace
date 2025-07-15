/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import static org.dspace.authenticate.SamlAuthentication.SAML_AUTH_SG_ATTRIBUTE;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This holds the SAML web authentication details and which request attribute
 * should have the details.
 */
public class SamlWebAuthenticationDetails extends SpecialGroupsWebAuthenticationDetails<Set<String>> {

    public SamlWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
    }

    public String getKey() {
        return SAML_AUTH_SG_ATTRIBUTE;
    }

}
