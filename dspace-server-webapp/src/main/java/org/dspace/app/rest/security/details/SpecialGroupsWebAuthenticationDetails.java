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
import java.util.Set;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

public abstract class SpecialGroupsWebAuthenticationDetails extends WebAuthenticationDetails {

    private final Map<String, Object> details;

    @SuppressWarnings("unchecked")
    public SpecialGroupsWebAuthenticationDetails(HttpServletRequest request, String authMethodName) {
        super(request);
        this.details = new HashMap<>();
        this.details.put(this.getKey(), (Set<String>) request.getAttribute(this.getKey()));
        this.details.put("am", authMethodName);
    }

    public abstract String getKey();

    public Map<String, Object> getDetails() {
        return details;
    }
}
