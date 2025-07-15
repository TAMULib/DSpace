/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This holds the special groups web authentication details and which request attribute should
 * have the details.
 * 
 * Override and type the web authentication details for authentication request typings.
 * 
 * @note All web authentications are currently being added after AuthenticationManager#authenticate
 * in StatelessLoginFilter#attemptAuthentication.
 */
public abstract class SpecialGroupsWebAuthenticationDetails<T> extends WebAuthenticationDetails {

    private final T details;

     @SuppressWarnings("unchecked")
     public SpecialGroupsWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.details = (T) request.getAttribute(this.getKey());
    }

    public abstract String getKey();

    public T getDetails() {
        return details;
    }
}
