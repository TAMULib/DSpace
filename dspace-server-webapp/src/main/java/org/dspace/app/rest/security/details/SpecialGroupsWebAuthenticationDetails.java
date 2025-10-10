/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

public abstract class SpecialGroupsWebAuthenticationDetails extends WebAuthenticationDetails {

    protected final Map<String, Object> details;

    public SpecialGroupsWebAuthenticationDetails(HttpServletRequest request, String authMethodName) {
        super(request);
        this.details = new HashMap<>();
        this.details.put(authMethodName + "-sg", request.getAttribute(authMethodName + "-sg"));
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
