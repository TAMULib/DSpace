/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This holds the stateless web authentication details and which request attribute
 * should have the details.
 *
 * Currently only a single key and only from the request attributes.
 *
 * @note All web authentications are currently being added after AuthenticationManager#authenticate
 *       in StatelessLoginFilter#attemptAuthentication.
 */
public class StatelessWebAuthenticationDetails extends SpecialGroupsWebAuthenticationDetails<Set<String>> {

    public StatelessWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
    }

    public String getKey() {
        return "stateless-sg";
    }

}
