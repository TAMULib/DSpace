/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security.details;

import static org.dspace.authenticate.OidcAuthenticationBean.OIDC_AUTH_SG_ATTRIBUTE;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extended authentication details for OpenID Connect (OIDC) authentication.
 * <p>
 * This class extends Spring Security's {@link WebAuthenticationDetails} to capture
 * additional OIDC-specific information from the authentication request. It retrieves
 * and stores special group or authentication attributes that are set during the OIDC
 * authentication process.
 * </p>
 * <p>
 * The OIDC-specific details are extracted from the request attributes, where they
 * are set by {@link org.dspace.authenticate.OidcAuthenticationBean#authenticate}
 * during the authentication flow.
 * </p>
 *
 * @see WebAuthenticationDetails
 * @see org.dspace.authenticate.OidcAuthenticationBean
 */
public class OidcWebAuthenticationDetails extends WebAuthenticationDetails {

    /**
     * The OIDC-specific authentication details retrieved from the request.
     * This typically contains special group attributes or other OIDC-related
     * information set during the authentication process.
     */
    private final Object details;

    /**
     * Constructs a new OidcWebAuthenticationDetails instance.
     * <p>
     * Extracts the OIDC authentication details from the request attributes.
     * The attribute is identified by {@code OIDC_AUTH_SG_ATTRIBUTE} (value: "oidc-sg")
     * and is set by {@link org.dspace.authenticate.OidcAuthenticationBean#authenticate}
     * during the OIDC authentication flow.
     * </p>
     *
     * @param request the HttpServletRequest containing the authentication details
     *                in its attributes
     */
    public OidcWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.details = request.getAttribute(OIDC_AUTH_SG_ATTRIBUTE);
    }

    /**
     * Returns the OIDC-specific authentication details.
     * <p>
     * These details typically include special group attributes or other
     * OIDC-related information that was extracted from the authentication
     * request and stored during the authentication process.
     * </p>
     *
     * @return the OIDC authentication details, or {@code null} if no
     *         OIDC-specific details were present in the request
     */
    public Object getDetails() {
        return details;
    }
}
